package de.team33.cmd.files.job;

import de.team33.cmd.files.common.*;
import de.team33.cmd.files.matching.NameMatcher;
import de.team33.patterns.io.adrastea.FileEntry;
import de.team33.patterns.io.adrastea.LinkHandling;

import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

import static de.team33.cmd.files.job.Util.cmdLine;
import static de.team33.cmd.files.job.Util.cmdName;
import static de.team33.patterns.io.adrastea.LinkHandling.ORIGINAL;

class Finder implements Runnable {

    static final String EXCERPT = "List files that meet certain criteria.";

    private static final FileEntry.Streamer STREAMER = FileEntry.streamer(LinkHandling.ORIGINAL);
    private static final Set<Option> OPTIONS = EnumSet.allOf(Option.class);
    private static final Function<List<String>, Args> ARGS = Args.stage(3, OPTIONS);

    private final Output out;
    private final FileEntry entry;
    private static final Predicate<FileEntry> POSITIVE = Filter.positive();
    private final Predicate<FileEntry> nameFilter;

    public static Runnable job(final Output out, final List<String> args) throws RequestException {
        try {
            return job(out, ARGS.apply(args));
        } catch (final IllegalArgumentException e) {
            throw RequestException.format(Finder.class, "Finder.txt", cmdLine(args), cmdName(args));
        }
    }

    private Finder(final Output out, final Path path, final Predicate<FileEntry> nameFilter) {
        this.out = out;
        this.entry = FileEntry.of(path, ORIGINAL);
        this.nameFilter = nameFilter;
    }

    private static Runnable job(final Output out, final Args args) {
        final Path path = Path.of(args.get(2));
        final Predicate<FileEntry> nameFilter = args.get(Option.N)
                                                    .map(NameMatcher::parse)
                                                    .map(NameMatcher::toFileEntryFilter)
                                                    .orElse(POSITIVE);
        return new Finder(out, path, nameFilter);
    }

    @Override
    public final void run() {
        final Stats stats = new Stats();
        STREAMER.stream(entry)
                .peek(stats::addTotal)
                .filter(nameFilter)
                .peek(stats::addFound)
                .forEach(entry -> out.printf("%s%n", entry.path()));
        stats.print(out);
    }

    private enum Option implements Args.Key {
        N,
        T,
        O
    }

    private static class Stats {

        private final Counter totalCounter = new Counter();
        private final Counter totalDirCounter = new Counter();
        private final Counter foundCounter = new Counter();
        private final Map<FileEntry.Type, Counter> foundTypeCounters = new TreeMap<>();

        private void addTotal(final FileEntry entry) {
            totalCounter.increment();
            if (entry.isDirectory()) {
                totalDirCounter.increment();
            }
        }

        private void addFound(final FileEntry entry) {
            foundCounter.increment();
            foundTypeCounters.computeIfAbsent(entry.type(), type -> new Counter()).increment();
        }

        private void print(final Output out) {
            out.printf("%n" +
                       "%,12d directories and a total of%n" +
                       "%,12d entries examined.%n%n" +
                       "%,12d entries found%n",
                       totalDirCounter.value(), totalCounter.value(), foundCounter.value());
            for (final Map.Entry<FileEntry.Type, Counter> entry : foundTypeCounters.entrySet()) {
                FileEntry.Type fileType = entry.getKey();
                Counter counter = entry.getValue();
                out.printf("    %,12d of type %s%n", counter.value(), fileType);
            }
            out.printf("%n");
        }
    }
}
