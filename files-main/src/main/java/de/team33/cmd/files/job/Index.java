package de.team33.cmd.files.job;

import de.team33.cmd.files.common.Counter;
import de.team33.cmd.files.common.Output;
import de.team33.cmd.files.common.RequestException;
import de.team33.cmd.files.matching.NameMatcher;
import de.team33.patterns.enums.alpha.Values;
import de.team33.patterns.io.alpha.FileEntry;
import de.team33.patterns.io.alpha.FileIndex;
import de.team33.patterns.io.alpha.FileType;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static de.team33.cmd.files.job.Util.cmdLine;
import static de.team33.cmd.files.job.Util.cmdName;

class Index implements Runnable {

    static final String EXCERPT = "Indexing files whose names match a pattern.";

    private final Output out;
    private final Mode mode;
    private final NameMatcher nameMatcher;
    private final FileIndex index;

    private Index(final Output out, final Mode mode, final String expression, final List<Path> paths) {
        this.out = out;
        this.mode = mode;
        this.nameMatcher = NameMatcher.parse(expression);
        this.index = FileIndex.of(paths);
    }

    public static Runnable job(final Output out, final List<String> args) throws RequestException {
        assert 1 < args.size();
        assert Regular.INDEX.name().equalsIgnoreCase(args.get(1));
        // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
        if (4 < args.size()) {
            final Mode mode = Mode.of(args.get(2));
            final String expression = args.get(3);
            final List<Path> paths = args.stream().skip(4).map(Path::of).toList();
            return new Index(out, mode, expression, paths);
        }
        throw RequestException.format(Index.class, "Index.txt", cmdLine(args), cmdName(args));
    }

    @Override
    public final void run() {
        final Stats stats = new Stats();
        index.entries()
             .peek(stats::addTotal)
             .filter(nameMatcher::matches)
             .peek(stats::addFound)
             .forEach(entry -> out.printf("%s%n", entry.path()));
        out.printf("%n" +
                   "%,12d directories and a total of%n" +
                   "%,12d entries examined.%n%n" +
                   "%,12d entries found%n",
                   stats.totalDirCounter.value(), stats.totalCounter.value(), stats.foundCounter.value());
        stats.foundTypeCounters.forEach(
                (fileType, counter) -> out.printf("    %,12d of type %s%n", counter.value(), fileType));
        out.printf("%n");
    }

    private static class Stats {

        private final Counter totalCounter = new Counter();
        private final Counter totalDirCounter = new Counter();
        private final Counter foundCounter = new Counter();
        private final Map<FileType, Counter> foundTypeCounters = new TreeMap<>();

        private void addTotal(final FileEntry entry) {
            totalCounter.increment();
            if (entry.isDirectory()) {
                totalDirCounter.increment();
            }
        }

        private void addFound(final FileEntry entry) {
            foundCounter.increment();
            foundTypeCounters.computeIfAbsent(entry.type(), any -> new Counter()).increment();
        }
    }

    private enum Mode {
        ADD, UPDATE, REPORT;

        private static final Values<Mode> VALUES = Values.of(Mode.class);
        private static final String FAULT = "mode \"%s\" not specified!";

        static Mode of(final String name) {
            return VALUES.findAny(value -> value.name().equalsIgnoreCase(name))
                         .orElseThrow(() -> new IllegalArgumentException(FAULT.formatted(name)));
        }
    }
}
