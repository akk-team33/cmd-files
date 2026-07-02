package de.team33.cmd.files.job;

import de.team33.cmd.files.cleaning.Cleaner;
import de.team33.cmd.files.common.Args;
import de.team33.cmd.files.common.Filter;
import de.team33.cmd.files.common.Output;
import de.team33.cmd.files.common.RequestException;
import de.team33.cmd.files.listing.Depth;
import de.team33.cmd.files.listing.Option;
import de.team33.cmd.files.matching.NameMatcher;
import de.team33.cmd.files.moving.Guard;
import de.team33.patterns.hashing.pandia.Algorithm;
import de.team33.patterns.hashing.pandia.Hash;
import de.team33.patterns.io.iocaste.FileEntry;
import de.team33.patterns.io.iocaste.LinkHandling;
import de.team33.tools.io.Hashing;
import de.team33.tools.io.Registry;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static de.team33.cmd.files.job.Util.cmdLine;
import static de.team33.cmd.files.job.Util.cmdName;

class Registrar implements Runnable {

    static final String EXCERPT = "Register unique regular files to a registry and relocate duplicates.";

    private static final Set<Option> OPTIONS = EnumSet.of(Option.D, Option.N, Option.X);
    private static final Function<List<String>, Args> ARGS = Args.stage(5, OPTIONS);
    private static final Predicate<FileEntry> POSITIVE = Filter.positive();
    private static final FileEntry.Lister LISTER = FileEntry.lister(LinkHandling.ORIGINAL);
    private static final FileEntry.Streamer STREAMER = FileEntry.streamer(LISTER);
    private static final String DIGITS = "0123456789abcdefghijklmnopqrstuvwxyz";
    private static final Pattern PATTERN = Pattern.compile("\\[#[" + DIGITS + "]+\\]",
                                                           Pattern.CASE_INSENSITIVE);

    private final Output out;
    private final FileEntry mainEntry;
    private final Path regPath;
    private final int keepOriginalName;
    private final Depth depth;
    private final Predicate<FileEntry> filter;
    private final Stats stats;
    private final Cleaner cleaner;
    private final Path trashPath;

    private Registrar(final Output out, final Path path, final Path regPath, final int keepOriginalName,
                      final Depth depth, final Predicate<FileEntry> filter) {
        this.out = out;
        this.mainEntry = FileEntry.original(path);
        this.trashPath = Path.of(mainEntry.path().toString() + ".trash");
        this.regPath = regPath;
        this.keepOriginalName = keepOriginalName;
        this.depth = depth;
        this.filter = filter;
        this.stats = new Stats();
        this.cleaner = new Cleaner(out, stats);
    }

    static Registrar job(final Output out, final List<String> args) throws RequestException {
        try {
            return job(out, ARGS.apply(args));
        } catch (final IllegalArgumentException e) {
            throw RequestException.format(Registrar.class, "Registrar.txt", cmdLine(args), cmdName(args));
        }
    }

    private static Registrar job(final Output out, final Args args) {
        final Path path = Path.of(args.get(2));
        final Path registry = Path.of(args.get(3));
        final int keep = Integer.parseInt(args.get(4));
        final Depth depth = args.get(Option.D)
                                .map(String::toUpperCase)
                                .map(Depth::valueOf)
                                .orElse(Depth.DEEP);
        final Predicate<FileEntry> nameFilter = args.get(Option.N)
                                                    .map(NameMatcher::parse)
                                                    .map(NameMatcher::toFileEntryFilter)
                                                    .orElse(null);
        final Predicate<FileEntry> nameXFilter = args.get(Option.X)
                                                     .map(NameMatcher::parse)
                                                     .map(NameMatcher::toFileEntryFilter)
                                                     .map(Predicate::negate)
                                                     .orElse(null);
        final Predicate<FileEntry> filter = Stream.of(nameFilter, nameXFilter)
                                                  .filter(Objects::nonNull)
                                                  .reduce(Predicate::and)
                                                  .orElse(POSITIVE);
        return new Registrar(out, path, registry, keep, depth, filter);
    }

    private Stream<FileEntry> stream() {
        return switch (depth) {
            case FLAT -> LISTER.list(mainEntry)
                               .stream();
            case DEEP -> STREAMER.stream(mainEntry)
                                 .skip(1);
        };
    }

    @Override
    public void run() {
        stats.reset();
        try (final Registry registry = new Registry(regPath)) {
            stream().filter(FileEntry::isRegularFile)
                    .filter(Guard::unprotected)
                    .filter(filter)
                    .forEach(entry -> register(entry, registry));
        }
        out.printf("%ncleaning ...%n");
        cleaner.clean(mainEntry);
        out.printf("%n" +
                   "%12d unique files confirmed%n" +
                   "%12d unique files registered%n" +
                   "%12d duplicate files trashed%n" +
                   "%12d register attempts failed%n" +
                   "%12d trash attempts failed%n" +
                   "%12d empty directories deleted%n" +
                   "%12d deletions failed%n%n",
                   stats.confirmed, stats.registered, stats.trashed, stats.registerFailed, stats.trashFailed,
                   stats.deleted, stats.deleteFailed);
    }

    private void register(final FileEntry entry, final Registry registry) {
        out.printf("%s ... ", entry.path());
        oldHashOf(entry).ifPresentOrElse(oldHash -> confirm(registry, oldHash, entry.name()),
                                         () -> registerNew(registry, entry));
    }

    private void confirm(final Registry registry, final Hash hash, final String name) {
        registry.confirm(hash, name);
        stats.incConfirmed();
        out.printf("confirmed%n");
    }

    private void registerNew(final Registry registry, final FileEntry entry) {
        final Hash newHash = newHashOf(entry);
        final String newName = newNameOf(newHash, entry.name());
        if (registry.register(newHash, newName)) {
            rename(entry, newName);
        } else {
            moveToTrash(entry);
        }
    }

    private void moveToTrash(final FileEntry entry) {
        final Path relative = mainEntry.path().relativize(entry.path());
        try {
            final Path target = trashPath.resolve(relative);
            Files.createDirectories(target.getParent());
            Files.move(entry.path(), target);
            stats.incTrashed();
            out.printf("trashed%n");
        } catch (final IOException e) {
            stats.incTrashFailed();
            out.printf("failed: %s%n", e);
        }
    }

    private void rename(final FileEntry entry, final String newName) {
        try {
            Files.move(entry.path(), entry.path().getParent().resolve(newName));
            stats.incRegistered();
            out.printf("registered (%s)%n", newName);
        } catch (final IOException e) {
            stats.incRegisterFailed();
            out.printf("failed: %s%n", e);
        }
    }

    private String newNameOf(final Hash hash, final String name) {
        final String[] parts = name.split("\\.", 2);
        final String part0 = parts[0];
        final int keep = Integer.min(part0.length(), keepOriginalName);
        final String head = part0.substring(0, keep);
        if (parts.length == 2) {
            return "%s[#%s].%s".formatted(head, hash.toString(DIGITS), parts[1]);
        } else {
            return "%s[#%s]".formatted(head, hash.toString(DIGITS));
        }
    }

    private Hash newHashOf(final FileEntry entry) {
        return Hashing.oldHashByName(entry.name())
                      .orElseGet(() -> Algorithm.SHA_1.hash(entry.path()));
    }

    private Optional<Hash> oldHashOf(final FileEntry entry) {
        final String name = entry.name();
        return PATTERN.matcher(name)
                      .results()
                      .findAny()
                      .map(match -> name.substring(match.start() + 2, match.end() - 1))
                      .map(String::toLowerCase)
                      .map(hash -> Algorithm.SHA_1.parse(hash, DIGITS));
    }

    private static class Stats implements Cleaner.Stats {

        private int deleted;
        private int deleteFailed;
        private int confirmed;
        private int registered;
        private int trashed;
        private int registerFailed;
        private int trashFailed;

        final void reset() {
            deleted = 0;
            deleteFailed = 0;
            confirmed = 0;
            registered = 0;
            registerFailed = 0;
            trashed = 0;
            trashFailed = 0;
        }

        public final void incDeleted() {
            this.deleted += 1;
        }

        public final void incDeleteFailed() {
            this.deleteFailed += 1;
        }

        public final void incConfirmed() {
            this.confirmed += 1;
        }

        public final void incRegistered() {
            this.registered += 1;
        }

        public void incTrashed() {
            this.trashed += 1;
        }

        public void incRegisterFailed() {
            this.registerFailed += 1;
        }

        public void incTrashFailed() {
            this.trashFailed += 1;
        }
    }
}
