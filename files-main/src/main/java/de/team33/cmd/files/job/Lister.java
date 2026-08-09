package de.team33.cmd.files.job;

import de.team33.cmd.files.common.*;
import de.team33.cmd.files.listing.PathQuery;
import de.team33.cmd.files.listing.Recursion;
import de.team33.cmd.files.listing.Report;
import de.team33.patterns.files.pluto.FileEntry;
import de.team33.patterns.files.pluto.FileType;
import de.team33.patterns.functions.alpha.Predicates;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static de.team33.cmd.files.job.Util.cmdLine;
import static de.team33.cmd.files.job.Util.cmdName;

class Lister implements Runnable {

    static final String EXCERPT = "List files that meet certain criteria.";

    private static final Set<Option> OPTIONS = EnumSet.of(Option.N, Option.X, Option.T, Option.O);
    private static final Function<List<String>, Args> ARGS = Args.stage(3, OPTIONS);

    private final Output out;
    private final PathQuery query;
    private final Predicate<FileEntry> filter;
    private final Comparator<FileEntry> order;

    private Lister(final Output out, final PathQuery query,
                   final Predicate<FileEntry> filter, final Comparator<FileEntry> order) {
        this.out = out;
        this.query = query;
        this.filter = filter;
        this.order = order; // nullable!
    }

    static Lister job(final Context context) throws RequestException {
        return job(context.out(), context.config(), context.args());
    }

    private static Lister job(final Output out, final Config config, final List<String> args) throws RequestException {
        try {
            final ListerConfig listerConfig = Optional.ofNullable(config.list())
                                                      .orElse(ListerConfig.EMPTY);
            return job(out, listerConfig, ARGS.apply(args));
        } catch (final IllegalArgumentException e) {
            throw RequestException.format(Lister.class)
                                  .apply(cmdLine(args), cmdName(args))
                                  .causedBy(e);
        }
    }

    private static ListerConfig join(final ListerConfig config, final Args args) {
        return config.addNamePattern(args.get(Option.N))
                     .addExcludePattern(args.get(Option.X))
                     .setTypes(args.get(Option.T))
                     .setOrder(args.get(Option.O));
    }

    private static Lister job(final Output out, final ListerConfig config, final Args args) {
        final ListerConfig joined = join(config, args);
        final PathQuery query = PathQuery.parse(args.get(2));
        final Predicate<FileEntry> nameInclusion = joined.nameInclusion();
        final Predicate<FileEntry> nameExclusion = joined.nameExclusion();
        final Predicate<FileEntry> typeInclusion = joined.typeInclusion();
        final Predicate<FileEntry> entryFilter = Predicates.and(nameInclusion, nameExclusion, typeInclusion);
        final Comparator<FileEntry> entryOrder = joined.entryOrder();
        return new Lister(out, query, entryFilter, entryOrder);
    }

    @Override
    public final void run() {
        final Stats stats = new Stats(query.recursion());
        final Stream<FileEntry> stage = query.reporting(stats)
                                             .stream()
                                             .filter(filter);
        //noinspection DataFlowIssue
        Optional.ofNullable(order)
                .map(stage::sorted)
                .orElse(stage)
                .peek(stats::addFound)
                .forEach(entry -> out.printf("%s%n", entry.path()));
        stats.print(out);
    }

    private static class Stats implements Report {

        private final Recursion recursion;
        private final Counter totalCounter = new Counter();
        private final Counter totalDirCounter = new Counter();
        private final Counter foundCounter = new Counter();
        private final Map<FileType, Counter> foundTypeCounters = new TreeMap<>();

        private Stats(final Recursion recursion) {
            this.recursion = recursion;
        }

        @Override
        public final void addTotal(final FileEntry entry) {
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
            final String aTotalOf = (Recursion.FLAT == recursion) ? "           A total of%n"
                                                                  : "%1$,12d directories and a total of%n";
            out.printf("%n" +
                       aTotalOf +
                       "%2$,12d entries examined.%n%n" +
                       "%3$,12d entries found%n",
                       totalDirCounter.value(), totalCounter.value(), foundCounter.value());
            for (final Map.Entry<FileType, Counter> entry : foundTypeCounters.entrySet()) {
                FileType fileType = entry.getKey();
                Counter counter = entry.getValue();
                out.printf("    %,12d of type %s%n", counter.value(), fileType);
            }
            out.printf("%n");
        }
    }
}
