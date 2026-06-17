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
import de.team33.cmd.files.moving.Resolver;
import de.team33.patterns.io.adrastea.FileEntry;
import de.team33.patterns.io.adrastea.LinkHandling;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static de.team33.cmd.files.job.Util.cmdLine;
import static de.team33.cmd.files.job.Util.cmdName;

class Moving implements Runnable {

    static final String EXCERPT = "Relocate regular files located in a given directory.";

    private static final Set<Option> OPTIONS = EnumSet.of(Option.D, Option.N, Option.X);
    private static final Function<List<String>, Args> ARGS = Args.stage(4, OPTIONS);
    private static final Predicate<FileEntry> POSITIVE = Filter.positive();
    private static final FileEntry.Lister LISTER = FileEntry.lister(LinkHandling.ORIGINAL);
    private static final FileEntry.Streamer STREAMER = FileEntry.streamer(LISTER);

    private final Set<Path> createDir = new HashSet<>();
    private final Output out;
    private final FileEntry mainEntry;
    private final Resolver resolver;
    private final Depth depth;
    private final Predicate<FileEntry> filter;
    private final Stats stats;
    private final Cleaner cleaner;

    public Moving(final Output out, final Path path, final Resolver resolver, final Depth depth, final Predicate<FileEntry> filter) {
        this.out = out;
        this.mainEntry = FileEntry.original(path);
        this.resolver = resolver;
        this.depth = depth;
        this.filter = filter;
        this.stats = new Stats();
        this.cleaner = new Cleaner(out, stats);
    }

    static Moving job(final Output out, final List<String> args) throws RequestException {
        try {
            return job(out, ARGS.apply(args));
        } catch (final IllegalArgumentException e) {
            throw RequestException.format(Moving.class, "Moving.txt", cmdLine(args), cmdName(args));
        }
    }

    private static Moving job(final Output out, final Args args) {
        final Path path = Path.of(args.get(2));
        final Resolver resolver = Resolver.parse(args.get(3));
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
        return new Moving(out, path, resolver, depth, filter);
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
        stream().filter(FileEntry::isRegularFile)
                .filter(Guard::unprotected)
                .filter(filter)
                .forEach(this::move);
        cleaner.clean(mainEntry);
        out.printf("%n" +
                   "%12d files moved%n" +
                   "%12d files skipped%n" +
                   "%12d moves failed%n%n" +
                   "%12d empty directories deleted%n" +
                   "%12d deletions failed%n%n",
                   stats.moved, stats.skipped, stats.moveFailed, stats.deleted, stats.deleteFailed);
    }

    private void move(final FileEntry entry) {
        final Path path = entry.path();
        final Path mainPath = this.mainEntry.path();
        out.printf("%s ...%n", mainPath.relativize(path));
        final Path newPath = mainPath.resolve(resolver.resolve(mainPath, entry)).normalize();
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

    private static class Stats implements Cleaner.Stats {

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
