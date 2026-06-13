package de.team33.cmd.files.job;

import de.team33.cmd.files.cleaning.DirDeletion;
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
import de.team33.patterns.io.adrastea.FileEntry;
import de.team33.patterns.io.adrastea.LinkHandling;
import de.team33.tools.io.Registry;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
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

    private final Set<Path> createDir = new HashSet<>();
    private final Output out;
    private final FileEntry cwdEntry;
    private final Path regPath;
    private final int keepOriginalName;
    private final Depth depth;
    private final Predicate<FileEntry> filter;
    private final Stats stats;
    private final DirDeletion deletion;
    private final Path trashPath;

    private Registrar(final Output out, final Path path, final Path regPath, final int keepOriginalName,
                      final Depth depth, final Predicate<FileEntry> filter) {
        this.out = out;
        this.cwdEntry = FileEntry.original(path);
        this.trashPath = Path.of(cwdEntry.path().toString() + ".trash");
        this.regPath = regPath;
        this.keepOriginalName = keepOriginalName;
        this.depth = depth;
        this.filter = filter;
        this.stats = new Stats();
        this.deletion = new DirDeletion(out, cwdEntry.path(), stats);
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

    private static void confirm(final Registry registry, final Hash hash, final String name) {
        registry.confirm(hash, name);
    }

    private Stream<FileEntry> stream() {
        return switch (depth) {
            case FLAT -> LISTER.list(cwdEntry)
                               .stream();
            case DEEP -> STREAMER.stream(cwdEntry)
                                 .skip(1);
        };
    }

    private List<FileEntry> entries() {
        return switch (depth) {
            case FLAT -> List.of();
            case DEEP -> LISTER.list(cwdEntry);
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
        deletion.clean(entries());
        out.printf("%n" +
                   "%12d files moved%n" +
                   "%12d files skipped%n" +
                   "%12d moves failed%n%n" +
                   "%12d empty directories deleted%n" +
                   "%12d deletions failed%n%n",
                   stats.moved, stats.skipped, stats.moveFailed, stats.deleted, stats.deleteFailed);
    }

    private void register(final FileEntry entry, final Registry registry) {
        oldHashOf(entry).ifPresentOrElse(oldHash -> confirm(registry, oldHash, entry.name()),
                                         () -> registerNew(registry, entry));
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
        final Path relative = cwdEntry.path().relativize(entry.path());
        try {
            final Path target = trashPath.resolve(relative);
            Files.createDirectories(target.getParent());
            Files.move(entry.path(), target);
        } catch (final IOException e) {
            // TODO: no Exception at this point!
            throw new IllegalStateException(Optional.ofNullable(e.getMessage()).orElseGet(e::toString), e);
        }
    }

    private void rename(final FileEntry entry, final String newName) {
        try {
            Files.move(entry.path(), entry.path().getParent().resolve(newName));
        } catch (final IOException e) {
            // TODO: no Exception at this point!
            throw new IllegalStateException(Optional.ofNullable(e.getMessage()).orElseGet(e::toString), e);
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
        return Algorithm.SHA_1.hash(entry.path());
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

    private void move(final FileEntry entry) {
        final Path path = entry.path();
        final Path mainPath = cwdEntry.path();
        out.printf("%s ...%n", mainPath.relativize(path));
        final Path newPath = null; //mainPath.resolve(resolver.resolve(mainPath, entry)).normalize();
        out.printf("--> %s ... ", mainPath.relativize(newPath));

        if (path.equals(newPath)) {
            out.printf("nothing to do%n");
            stats.incSkipped();
            return;
        }

        try {
            final FileTime lastModifiedTime = Files.getLastModifiedTime(path, LinkOption.NOFOLLOW_LINKS);
            final Path parent = newPath.getParent();
            if (createDir.add(parent)) {
                Files.createDirectories(parent);
            }
            Files.move(path, newPath);
            Files.setLastModifiedTime(newPath, lastModifiedTime);
            out.printf("moved%n");
            stats.incMoved();
        } catch (final IOException e) {
            out.printf("failed:%n" +
                       "    Message   : %s%n" +
                       "    Exception : %s%n", e.getMessage(), e.getClass().getCanonicalName());
            stats.incMoveFailed();
        }
    }

    private static class Stats implements DirDeletion.Stats {

        private int skipped;
        private int moved;
        private int moveFailed;
        private int deleted;
        private int deleteFailed;

        final void reset() {
            skipped = 0;
            moved = 0;
            moveFailed = 0;
            deleted = 0;
            deleteFailed = 0;
        }

        final void incSkipped() {
            this.skipped += 1;
        }

        final void incMoved() {
            this.moved += 1;
        }

        final void incMoveFailed() {
            this.moveFailed += 1;
        }

        public final void incDeleted() {
            this.deleted += 1;
        }

        public final void incDeleteFailed() {
            this.deleteFailed += 1;
        }
    }
}
