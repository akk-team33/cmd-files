package de.team33.cmd.files.job;

import de.team33.cmd.files.common.*;
import de.team33.cmd.files.listing.Depth;
import de.team33.cmd.files.listing.Option;
import de.team33.cmd.files.matching.NameMatcher;
import de.team33.cmd.files.matching.TypeFilter;
import de.team33.cmd.files.sorting.Order;
import de.team33.patterns.io.adrastea.FileEntry;

import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static de.team33.cmd.files.job.Util.cmdLine;
import static de.team33.cmd.files.job.Util.cmdName;

class Lister implements Runnable {

    static final String EXCERPT = "List files that meet certain criteria.";

    private static final Set<Option> OPTIONS = EnumSet.allOf(Option.class);
    private static final Function<List<String>, Args> ARGS = Args.stage(3, OPTIONS);
    private static final Predicate<FileEntry> POSITIVE = Filter.positive();

    private final Output out;
    private final FileEntry entry;
    private final Depth depth;
    private final Predicate<FileEntry> filter;
    private final Comparator<FileEntry> order;

    private Lister(final Output out, final Path path, final Depth depth, final Predicate<FileEntry> filter, final Comparator<FileEntry> order) {
        this.out = out;
        this.entry = FileEntry.original(path);
        this.depth = depth;
        this.filter = filter;
        this.order = order; // nullable!
    }

    static Runnable job(final Output out, final List<String> args) throws RequestException {
        try {
            return job(out, ARGS.apply(args));
        } catch (final IllegalArgumentException e) {
            throw RequestException.format(Lister.class, "Lister.txt", cmdLine(args), cmdName(args));
        }
    }

    private static Runnable job(final Output out, final Args args) {
        final Path path = Path.of(args.get(2));
        final Depth depth = args.get(Option.D)
                                .map(String::toUpperCase)
                                .map(Depth::valueOf)
                                .orElse(Depth.DEEP);
        final Predicate<FileEntry> nameFilter = args.get(Option.N)
                                                    .map(NameMatcher::parse)
                                                    .map(NameMatcher::toFileEntryFilter)
                                                    .orElse(null);
        final Predicate<FileEntry> nameXFilter = args.get(Option.X)
                                                     .map(NameMatcher::parse)
                                                     .map(NameMatcher::toFileEntryFilter)
                                                     .map(Predicate::negate)
                                                     .orElse(null);
        final Predicate<FileEntry> typeFilter = args.get(Option.T)
                                                    .map(TypeFilter::parse)
                                                    .orElse(null);
        final Predicate<FileEntry> entryFilter = Stream.of(nameFilter, nameXFilter, typeFilter)
                                                       .filter(Objects::nonNull)
                                                       .reduce(Predicate::and)
                                                       .orElse(POSITIVE);
        final Comparator<FileEntry> order = args.get(Option.O)
                                                .map(Order::parse)
                                                .orElse(null);
        return new Lister(out, path, depth, entryFilter, order);
    }

    @Override
    public final void run() {
        final Stats stats = new Stats(depth);
        final Stream<FileEntry> stage = depth.stream(entry)
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

    private static class Stats {

        private final Depth depth;
        private final Counter totalCounter = new Counter();
        private final Counter totalDirCounter = new Counter();
        private final Counter foundCounter = new Counter();
        private final Map<FileEntry.Type, Counter> foundTypeCounters = new TreeMap<>();

        private Stats(final Depth depth) {
            this.depth = depth;
        }

        private void addTotal(final FileEntry entry) {
            totalCounter.increment();
            if (Depth.DEEP == depth && entry.isDirectory()) {
                totalDirCounter.increment();
            }
        }

        private void addFound(final FileEntry entry) {
            foundCounter.increment();
            foundTypeCounters.computeIfAbsent(entry.type(), type -> new Counter()).increment();
        }

        private void print(final Output out) {
            final String aTotalOf = (Depth.FLAT == depth) ? "           A total of%n"
                                                          : "%1$,12d directories and a total of%n";
            out.printf("%n" +
                       aTotalOf +
                       "%2$,12d entries examined.%n%n" +
                       "%3$,12d entries found%n",
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
