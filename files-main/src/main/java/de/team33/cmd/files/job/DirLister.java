package de.team33.cmd.files.job;

import de.team33.cmd.files.common.*;
import de.team33.cmd.files.listing.PathQuery;
import de.team33.cmd.files.listing.Recursion;
import de.team33.cmd.files.listing.Report;
import de.team33.cmd.files.matching.NameMatcher;
import de.team33.cmd.files.matching.TypeFilter;
import de.team33.patterns.files.pluto.FileEntry;
import de.team33.patterns.functions.alpha.Predicates;

import java.nio.file.Path;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static de.team33.cmd.files.job.Util.cmdLine;
import static de.team33.cmd.files.job.Util.cmdName;

class DirLister implements Runnable {

    static final String EXCERPT = "List directories containing files that meet certain criteria.";

    private static final Set<Option> OPTIONS = EnumSet.of(Option.N, Option.X, Option.T);
    private static final Function<List<String>, Args> ARGS = Args.stage(3, OPTIONS);
    private static final Predicate<FileEntry> ACCEPT = Predicates.accept();

    private final Output out;
    private final PathQuery query;
    private final Predicate<FileEntry> filter;

    private DirLister(final Output out, final PathQuery query, final Predicate<FileEntry> filter) {
        this.out = out;
        this.query = query;
        this.filter = filter;
    }

    static DirLister job(final Context context) throws RequestException {
        return job(context.out(), context.args());
    }

    private static DirLister job(final Output out, final List<String> args) throws RequestException {
        try {
            return job(out, ARGS.apply(args));
        } catch (final IllegalArgumentException e) {
            throw RequestException.format(DirLister.class).apply(cmdLine(args), cmdName(args))
                                  .causedBy(e);
        }
    }

    private static DirLister job(final Output out, final Args args) {
        final PathQuery query = PathQuery.parse(args.get(2));
        final Predicate<FileEntry> nameFilter = args.getOptional(Option.N)
                                                    .map(NameMatcher::parse)
                                                    .map(NameMatcher::toFileEntryFilter)
                                                    .orElse(null);
        final Predicate<FileEntry> nameXFilter = args.getOptional(Option.X)
                                                     .map(NameMatcher::parse)
                                                     .map(NameMatcher::toFileEntryFilter)
                                                     .map(Predicate::negate)
                                                     .orElse(null);
        final Predicate<FileEntry> typeFilter = args.getOptional(Option.T)
                                                    .map(TypeFilter::parse)
                                                    .orElseGet(() -> TypeFilter.parse("F"));
        final Predicate<FileEntry> entryFilter = Stream.of(nameFilter, nameXFilter, typeFilter)
                                                       .filter(Objects::nonNull)
                                                       .reduce(Predicate::and)
                                                       .orElse(ACCEPT);
        return new DirLister(out, query, entryFilter);
    }

    @Override
    public final void run() {
        final Stats stats = new Stats(query.recursion());
        query.reporting(stats)
             .stream()
             .filter(entry -> filter.test(entry))
             .map(entry -> entry.path().getParent())
             .distinct()
             .peek(stats::addFound)
             .forEach(path -> out.printf("%s%n", path));
        stats.print(out);
    }

    private static class Stats implements Report {

        private final Recursion recursion;
        private final Counter totalCounter = new Counter();
        private final Counter totalDirCounter = new Counter();
        private final Counter foundCounter = new Counter();

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

        private void addFound(final Path path) {
            foundCounter.increment();
        }

        private void print(final Output out) {
            final String aTotalOf = (Recursion.FLAT == recursion) ? "           A total of%n"
                                                                  : "%1$,12d directories and a total of%n";
            out.printf("%n" +
                       aTotalOf +
                       "%2$,12d entries examined.%n%n" +
                       "%3$,12d directories found.%n",
                       totalDirCounter.value(), totalCounter.value(), foundCounter.value());
            out.printf("%n");
        }
    }
}
