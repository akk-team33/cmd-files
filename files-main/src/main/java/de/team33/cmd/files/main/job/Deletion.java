package de.team33.cmd.files.main.job;

import de.team33.cmd.files.main.common.Counter;
import de.team33.cmd.files.main.common.Output;
import de.team33.cmd.files.main.common.RequestException;
import de.team33.cmd.files.main.finder.Pattern;
import de.team33.patterns.io.alpha.FileEntry;
import de.team33.patterns.io.alpha.FileIndex;
import de.team33.patterns.io.alpha.FilePolicy;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;

import static de.team33.cmd.files.main.job.Util.cmdLine;
import static de.team33.cmd.files.main.job.Util.cmdName;

class Deletion implements Runnable {

    static final String EXCERPT = "Delete files whose names match a pattern.";

    private final Output out;
    private final Pattern pattern;
    private final FileIndex index;

    private Deletion(final Output out, final String expression, final List<Path> paths) {
        this.out = out;
        this.pattern = Pattern.parse(expression);
        this.index = FileIndex.of(paths, FilePolicy.DISTINCT_SYMLINKS);
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
        final Stats stats = new Stats();
        final List<FileEntry> postponed = new LinkedList<>();
        index.entries()
             .peek(stats::addTotal)
             .filter(pattern.matcher())
             .forEach(entry -> {
                 if (entry.isDirectory()) {
                     postponed.add(0, entry);
                 } else {
                     delete(entry, stats);
                 }
             });
        postponed.forEach(entry -> delete(entry, stats));
        out.printf("%n" +
                   "%,12d directories and a total of%n" +
                   "%,12d entries examined.%n%n" +
                   "%,12d entries deleted%n" +
                   "%,12d entries failed%n",
                   stats.totalDirCounter.value(), stats.totalCounter.value(),
                   stats.deletedCounter.value(), stats.failedCounter.value());
        out.printf("%n");
    }

    private void delete(final FileEntry entry, final Stats stats) {
        out.printf("%s ...", entry.path());
        try {
            Files.delete(entry.path());
            out.printf(" deleted%n");
            stats.addDeleted();
        } catch (final IOException e) {
            out.printf(" failed:%n" +
                               "    Message: %s%n" +
                               "    Exception: %s%n", e.getMessage(), e.getClass().getCanonicalName());
            stats.addFailed();
        }
    }

    private static class Stats {

        private final Counter totalCounter = new Counter();
        private final Counter totalDirCounter = new Counter();
        private final Counter deletedCounter = new Counter();
        private final Counter failedCounter = new Counter();

        private void addTotal(final FileEntry entry) {
            totalCounter.increment();
            if (entry.isDirectory()) {
                totalDirCounter.increment();
            }
        }

        private void addDeleted() {
            deletedCounter.increment();
        }

        private void addFailed() {
            failedCounter.increment();
        }
    }
}
