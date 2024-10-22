package de.team33.cmd.files.job;

import de.team33.cmd.files.common.Condition;
import de.team33.cmd.files.common.Counter;
import de.team33.cmd.files.common.Output;
import de.team33.cmd.files.common.RequestException;
import de.team33.cmd.files.matching.NameMatcher;
import de.team33.patterns.io.alpha.FileEntry;
import de.team33.patterns.io.alpha.FileIndex;
import de.team33.patterns.io.alpha.FileType;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

class Finder implements Runnable {

    static final String EXCERPT = "Find files whose names match a pattern.";

    private final Output out;
    private final NameMatcher nameMatcher;
    private final FileIndex index;

    private Finder(final Output out, final String expression, final List<Path> paths) {
        this.out = out;
        this.nameMatcher = NameMatcher.parse(expression);
        this.index = FileIndex.of(paths);
    }

    public static Runnable job(final Condition condition) throws RequestException {
        return Optional.of(condition.args())
                       .filter(args -> 3 < args.size())
                       .map(args -> new Finder(condition.out(),
                                               args.get(2),
                                               args.stream()
                                                   .skip(3)
                                                   .map(Path::of)
                                                   .toList()))
                       .orElseThrow(condition.toRequestException(Finder.class));
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
}
