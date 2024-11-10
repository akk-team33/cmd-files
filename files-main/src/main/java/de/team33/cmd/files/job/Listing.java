package de.team33.cmd.files.job;

import de.team33.cmd.files.common.Counter;
import de.team33.cmd.files.common.Output;
import de.team33.cmd.files.common.RequestException;
import de.team33.patterns.io.phobos.FileEntry;
import de.team33.patterns.io.phobos.FileIndex;
import de.team33.patterns.io.phobos.FileType;

import java.nio.file.Path;
import java.util.*;

import static de.team33.cmd.files.job.Util.cmdLine;
import static de.team33.cmd.files.job.Util.cmdName;

class Listing implements Runnable {

    static final String EXCERPT = "Get a list of the files in given directories.";

    private final Output out;
    private final List<Path> paths;

    private Listing(final Output out, final List<Path> paths) {
        this.out = out;
        this.paths = paths;
    }

    static Runnable job(final Output out, final List<String> args) throws RequestException {
        assert 1 < args.size();
        assert Regular.LIST.name().equalsIgnoreCase(args.get(1));
        // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
        final int size = args.size();
        if (2 < size) {
            final List<Path> paths = args.stream().skip(2).map(Path::of).toList();
            return new Listing(out, paths);
        }
        throw RequestException.format(Listing.class, "Listing.txt", cmdLine(args), cmdName(args));
    }

    @Override
    public final void run() {
        final Stats stats = new Stats();
        FileIndex.of(paths)
                 .entries()
                 .peek(stats::incTotal)
                 .peek(stats::incFound)
                 .forEach(this::println);
        out.printf("%n" +
                           "%,12d directories and a total of%n" +
                           "%,12d entries examined.%n%n" +
                           "%,12d entries listed%n",
                   stats.totalDir, stats.total, stats.found);
        stats.foundTypeCounters.forEach(
                (fileType, counter) -> out.printf("    %,12d of type %s%n", counter.value(), fileType));
        out.printf("%n");
    }

    private void println(final FileEntry entry) {
        out.printf("%s%n", entry.path());
    }

    private static class Stats {

        private int total = 0;
        private int totalDir = 0;
        private int found = 0;
        private final Map<FileType, Counter> foundTypeCounters = new TreeMap<>();

        private void incTotal(final FileEntry entry) {
            total += 1;
            if (entry.isDirectory()) {
                totalDir += 1;
            }
        }

        private void incFound(final FileEntry entry) {
            found += 1;
            foundTypeCounters.computeIfAbsent(entry.type(), any -> new Counter()).increment();
        }
    }
}
