package de.team33.cmd.files.job;

import de.team33.cmd.files.common.*;
import de.team33.cmd.files.matching.NameMatcher;
import de.team33.cmd.files.matching.TypeFilter;
import de.team33.cmd.files.sorting.Order;
import de.team33.patterns.io.adrastea.FileEntry;
import de.team33.patterns.io.adrastea.LinkHandling;

import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static de.team33.cmd.files.job.Util.cmdLine;
import static de.team33.cmd.files.job.Util.cmdName;
import static de.team33.patterns.io.adrastea.LinkHandling.ORIGINAL;

class Finder implements Runnable {

    static final String EXCERPT = "List files that meet certain criteria.";

    private static final FileEntry.Streamer STREAMER = FileEntry.streamer(LinkHandling.ORIGINAL);
    private static final Set<Option> OPTIONS = EnumSet.allOf(Option.class);
    private static final Function<List<String>, Args> ARGS = Args.stage(3, OPTIONS);
    private static final Predicate<FileEntry> POSITIVE = Filter.positive();

    private final Output out;
    private final FileEntry entry;
    private final Predicate<FileEntry> filter;
    private final Comparator<FileEntry> order;

    private Finder(final Output out, final Path path, final Predicate<FileEntry> filter, final Comparator<FileEntry> order) {
        this.out = out;
        this.entry = FileEntry.of(path, ORIGINAL);
        this.filter = filter;
        this.order = order; // nullable!
    }

    public static Runnable job(final Output out, final List<String> args) throws RequestException {
        try {
            return job(out, ARGS.apply(args));
        } catch (final IllegalArgumentException e) {
            throw RequestException.format(Finder.class, "Finder.txt", cmdLine(args), cmdName(args));
        }
    }

    private static Runnable job(final Output out, final Args args) {
        final Path path = Path.of(args.get(2));
        final Predicate<FileEntry> nameFilter = args.get(Option.N)
                                                    .map(NameMatcher::parse)
                                                    .map(NameMatcher::toFileEntryFilter)
                                                    .orElse(POSITIVE);
        final Predicate<FileEntry> typeFilter = args.get(Option.T)
                                                    .map(TypeFilter::parse)
                                                    .orElse(POSITIVE);
        final Predicate<FileEntry> entryFilter = (typeFilter == POSITIVE) ? nameFilter : nameFilter.and(typeFilter);
        final Comparator<FileEntry> order = args.get(Option.O)
                                                .map(Order::parse)
                                                .orElse(null);
        return new Finder(out, path, entryFilter, order);
    }

    @Override
    public final void run() {
        final Stats stats = new Stats();
        final Stream<FileEntry> stage = STREAMER.stream(entry)
                                                .peek(stats::addTotal)
                                                .filter(filter);
        //noinspection DataFlowIssue
        Optional.ofNullable(order)
                .map(stage::sorted)
                .orElse(stage)
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
