package de.team33.cmd.files.job;

import de.team33.cmd.files.common.Output;
import de.team33.cmd.files.common.RequestException;
import de.team33.cmd.files.matching.NameMatcher;
import de.team33.patterns.io.phobos.FileEntry;
import de.team33.patterns.io.phobos.FileIndex;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static de.team33.cmd.files.job.Util.cmdLine;
import static de.team33.cmd.files.job.Util.cmdName;

class Deletion implements Runnable {

    static final String EXCERPT = "Delete files whose names match a pattern.";

    private final Output out;
    private final NameMatcher nameMatcher;
    private final FileIndex index;
    private final Stats stats = new Stats();

    private Deletion(final Output out, final String expression, final List<Path> paths) {
        this.out = out;
        this.nameMatcher = NameMatcher.parse(expression);
        this.index = FileIndex.of(paths);
    }

    public static Runnable job(final Output out, final List<String> args) throws RequestException {
        assert 1 < args.size();
        assert Regular.DELETE.name().equalsIgnoreCase(args.get(1));
        // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
        if (3 < args.size()) {
            final String expression = args.get(2);
            final List<Path> paths = args.stream().skip(3).map(Path::of).toList();
            return new Deletion(out, expression, paths);
        }
        throw RequestException.format(Deletion.class, "Deletion.txt", cmdLine(args), cmdName(args));
    }

    @Override
    public final void run() {
        index.entries()
             .filter(nameMatcher::matches)
             .forEach(entry -> delete(entry, Cause.EXPLICIT));
        out.printf("%n" +
                   "%,12d entries deleted explicit%n" +
                   "%,12d entries deleted implicit%n" +
                   "%,12d entries failed%n",
                   stats.explicit, stats.implicit, stats.failed);
        out.printf("%n");
    }

    private void delete(final FileEntry entry, final Cause cause) {
        if (stats.addCandidate(entry.path())) {
            if (entry.isDirectory()) {
                delete(entry.entries());
            }
            out.printf("%s ...", entry.path());
            try {
                Files.delete(entry.path());
                out.printf(" deleted%n");
                stats.addDeleted(cause);
            } catch (final IOException e) {
                out.printf(" failed:%n" +
                           "    Message: %s%n" +
                           "    Exception: %s%n", e.getMessage(), e.getClass().getCanonicalName());
                stats.addFailed();
            }
        }
    }

    private void delete(final Stream<FileEntry> entries) {
        entries.forEach(entry -> delete(entry, Cause.IMPLICIT));
    }

    private enum Cause {
        EXPLICIT,
        IMPLICIT
    }

    private static class Stats {

        private final Set<Path> candidates = new HashSet<>();
        private int explicit = 0;
        private int implicit = 0;
        private int failed = 0;

        private boolean addCandidate(final Path path) {
            return candidates.add(path);
        }

        private void addFailed() {
            failed += 1;
        }

        private void addDeleted(final Cause cause) {
            switch (cause) {
                case IMPLICIT -> implicit += 1;
                case EXPLICIT -> explicit += 1;
            }
        }
    }
}
