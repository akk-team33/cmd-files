package de.team33.cmd.files.job;

import de.team33.cmd.files.common.Counter;
import de.team33.cmd.files.common.Output;
import de.team33.cmd.files.common.RequestException;
import de.team33.cmd.files.matching.NameMatcher;
import de.team33.cmd.files.spike.Args;
import de.team33.patterns.io.adrastea.FileEntry;
import de.team33.patterns.io.adrastea.LinkHandling;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static de.team33.cmd.files.job.Util.cmdLine;
import static de.team33.cmd.files.job.Util.cmdName;
import static de.team33.patterns.io.adrastea.LinkHandling.ORIGINAL;

class Finder implements Runnable {

    static final String EXCERPT = "List files that meet certain criteria.";

    private static final FileEntry.Streamer STREAMER = FileEntry.streamer(LinkHandling.ORIGINAL);

    private final Output out;
    private final FileEntry entry;
    private final NameMatcher nameMatcher;

    private Finder(final Output out, final Path path, final String expression) {
        this.out = out;
        this.entry = FileEntry.of(path, ORIGINAL);
        this.nameMatcher = NameMatcher.parse(expression);
    }

    public static Runnable job(final Output out, final List<String> args) throws RequestException {
        try {
            return job(out, Args.stage(3, Option.class).apply(args));
        } catch (final IllegalArgumentException e) {
            throw RequestException.format(Finder.class, "Finder.txt", cmdLine(args), cmdName(args));
        }
    }

    private static Runnable job(final Output out, final Args<Option> args) {
        final Path path = Path.of(args.get(2));
        final String expression = args.get(Option.N)
                                      .orElse("*"); // TODO?
        return new Finder(out, path, expression);
    }

    @Override
    public final void run() {
        final Stats stats = new Stats();
        STREAMER.stream(entry)
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

    private enum Option {
        N,
        T,
        O
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
            foundTypeCounters.computeIfAbsent(FileType.of(entry), type -> new Counter()).increment();
        }
    }
}
