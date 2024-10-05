package de.team33.cmd.files.job;

import de.team33.cmd.files.cleaning.DirDeletion;
import de.team33.cmd.files.common.Output;
import de.team33.cmd.files.common.RequestException;
import de.team33.cmd.files.moving.Guard;
import de.team33.cmd.files.moving.Resolver;
import de.team33.patterns.io.alpha.FileEntry;
import de.team33.patterns.io.alpha.FileIndex;
import de.team33.patterns.io.alpha.FilePolicy;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

class Moving implements Runnable {

    static final String EXCERPT = "Relocate regular files located in a given directory.";
    private static final FilePolicy POLICY = FilePolicy.DISTINCT_SYMLINKS;

    private final Set<Path> createDir = new HashSet<>();
    private final Output out;
    private final Mode mode;
    private final Path mainPath;
    private final Resolver resolver;
    private final Stats stats;
    private final DirDeletion deletion;

    private Moving(final Output out, final Mode mode, final Path mainPath,
                   final Resolver resolver) {
        this.out = out;
        this.mode = mode;
        this.mainPath = mainPath;
        this.resolver = resolver;
        this.stats = new Stats();
        this.deletion = new DirDeletion(out, mainPath, stats);
    }

    static Moving job(final Output out, final List<String> args) throws RequestException {
        assert 1 < args.size();
        assert Regular.MOVE.name().equalsIgnoreCase(args.get(1));
        // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
        if (args.stream().skip(2).findFirst().map("-r"::equalsIgnoreCase).orElse(false)) {
            return job(out, Mode.DEEP, args, 3);
        } else {
            return job(out, Mode.FLAT, args, 2);
        }
    }

    private static Moving job(final Output out, final Mode mode,
                              final List<String> args, final int nextArg) throws RequestException {
        if ((nextArg + 2) == args.size()) {
            final Path mainPath = Path.of(args.get(nextArg));
            final Resolver resolver = Resolver.parse(args.get(nextArg + 1));
            return new Moving(out, mode, mainPath, resolver);
        }
        throw RequestException.format(Moving.class, "Moving.txt", Util.cmdLine(args), Util.cmdName(args));
    }

    private Stream<FileEntry> stream() {
        return switch (mode) {
            case FLAT -> FileEntry.of(mainPath, POLICY)
                                  .entries()
                                  .stream();
            case DEEP -> FileIndex.of(mainPath, POLICY)
                                  .entries()
                                  .skip(1);
        };
    }

    private List<FileEntry> list() {
        return switch (mode) {
            case FLAT -> List.of();
            case DEEP -> FileEntry.of(mainPath, POLICY)
                                  .entries();
        };
    }

    @Override
    public void run() {
        stats.reset();
        stream().filter(FileEntry::isRegularFile)
                .filter(Guard::unprotected)
                .forEach(this::move);
        deletion.clean(list());
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
        out.printf("%s ...%n", mainPath.relativize(path));
        final Path newPath = mainPath.resolve(resolver.resolve(mainPath, entry)).normalize();
        out.printf("--> %s ... ", mainPath.relativize(newPath));

        if (path.equals(newPath)) {
            out.printf("nothing to do%n");
            stats.incSkipped();
            return;
        }

        try {
            final Path parent = newPath.getParent();
            if (createDir.add(parent)) {
                Files.createDirectories(parent);
            }
            Files.move(path, newPath);
            out.printf("moved%n");
            stats.incMoved();
        } catch (final IOException e) {
            out.printf("failed:%n" +
                       "    Message   : %s%n" +
                       "    Exception : %s%n", e.getMessage(), e.getClass().getCanonicalName());
            stats.incMoveFailed();
        }
    }

    private enum Mode {
        FLAT,
        DEEP;
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
