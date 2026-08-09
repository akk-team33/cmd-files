package de.team33.cmd.files.job;

import de.team33.cmd.files.cleaning.Cleaner;
import de.team33.cmd.files.common.Args;
import de.team33.cmd.files.common.Option;
import de.team33.cmd.files.common.Output;
import de.team33.cmd.files.common.RequestException;
import de.team33.cmd.files.listing.PathQuery;
import de.team33.cmd.files.moving.Resolver;
import de.team33.patterns.files.pluto.FileEntry;
import de.team33.patterns.functions.alpha.Predicates;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

import static de.team33.cmd.files.job.Util.cmdLine;
import static de.team33.cmd.files.job.Util.cmdName;

class Moving implements Runnable {

    static final String EXCERPT = "Relocate regular files located in a given directory.";

    private static final Set<Option> OPTIONS = EnumSet.of(Option.N, Option.X);
    private static final Function<List<String>, Args> ARGS = Args.stage(4, OPTIONS);

    private final Set<Path> createDir = new HashSet<>();
    private final Output out;
    private final PathQuery query;
    private final Resolver resolver;
    private final Predicate<FileEntry> filter;
    private final Stats stats;
    private final Cleaner cleaner;

    private Moving(final Output out, final PathQuery query, final Resolver resolver, final Predicate<FileEntry> filter) {
        this.out = out;
        this.query = query;
        this.resolver = resolver;
        this.filter = filter;
        this.stats = new Stats();
        this.cleaner = new Cleaner(out, stats);
    }

    static Moving job(final Context context) throws RequestException {
        return job(context.out(), context.config(), context.args());
    }

    private static Moving job(final Output out, final Config config, final List<String> args) throws RequestException {
        try {
            final MovingConfig movingConfig = Optional.ofNullable(config.move())
                                                      .orElse(MovingConfig.EMPTY);
            return job(out, movingConfig, ARGS.apply(args));
        } catch (final IllegalArgumentException e) {
            throw RequestException.format(Moving.class).apply(cmdLine(args), cmdName(args))
                                  .causedBy(e);
        }
    }

    private static MovingConfig join(final MovingConfig config, final Args args) {
        return config.addNamePattern(args.get(Option.N))
                     .addExcludePattern(args.get(Option.X));
    }

    private static Moving job(final Output out, final MovingConfig config, final Args args) {
        final MovingConfig joined = join(config, args);
        final PathQuery query = PathQuery.parse(args.get(2));
        final Resolver resolver = Resolver.parse(args.get(3));
        final Predicate<FileEntry> nameInclusion = joined.nameInclusion();
        final Predicate<FileEntry> nameExclusion = joined.nameExclusion();
        final Predicate<FileEntry> entryFilter = Predicates.and(nameInclusion, nameExclusion);
        return new Moving(out, query, resolver, entryFilter);
    }

    @Override
    public void run() {
        stats.reset();
        query.stream()
             .filter(FileEntry::isRegularFile)
             //.filter(Guard::unprotected)
             .filter(filter)
             .forEach(this::move);
        cleaner.clean(query.baseEntry());
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
        final Path mainPath = query.baseEntry().path();
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
