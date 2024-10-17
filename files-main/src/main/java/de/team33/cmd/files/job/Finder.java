package de.team33.cmd.files.job;

import de.team33.cmd.files.cleaning.DirDeletion;
import de.team33.cmd.files.common.Counter;
import de.team33.cmd.files.common.Output;
import de.team33.cmd.files.common.RequestException;
import de.team33.cmd.files.common.StatsTotal;
import de.team33.cmd.files.matching.NameMatcher;
import de.team33.cmd.files.stats.Aggregat;
import de.team33.patterns.io.alpha.FileEntry;
import de.team33.patterns.io.alpha.FileIndex;
import de.team33.patterns.io.alpha.FileType;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static de.team33.cmd.files.job.Util.cmdLine;
import static de.team33.cmd.files.job.Util.cmdName;

class Finder implements Runnable {

    static final String EXCERPT = "Find files whose names match a pattern.";

    private final Output out;
    private final NameMatcher nameMatcher;
    private final FileIndex index;
    private final Stats_ stats;

    private Finder(final Output out, final String expression, final List<Path> paths) {
        this.out = out;
        this.nameMatcher = NameMatcher.parse(expression);
        this.index = FileIndex.of(paths);
        this.stats = new Stats_();
    }

    public static Runnable job(final Output out, final List<String> args) throws RequestException {
        assert 1 < args.size();
        assert Regular.FIND.name().equalsIgnoreCase(args.get(1));
        // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
        if (3 < args.size()) {
            final String expression = args.get(2);
            final List<Path> paths = args.stream().skip(3).map(Path::of).toList();
            return new Finder(out, expression, paths);
        }
        throw RequestException.format(Finder.class, "Finder.txt", cmdLine(args), cmdName(args));
    }

    @Override
    public final void run() {
        stats.reset();
        index.entries()
             .peek(stats::addTotal)
             .filter(nameMatcher::matches)
             .peek(stats::addFound)
             .forEach(entry -> out.printf("%s%n", entry.path()));
        stats.lines()
             .forEach(line -> out.printf("%s%n", line));
        out.printf("%n");
    }

    private static class Stats_ {

        private final Aggregat<FileEntry> aggregat = Aggregat.headEmpty()
                                                             .add();

        final void reset() {
            throw new UnsupportedOperationException("not yet implemented");
        }

        final void addTotal(final FileEntry entry) {
            throw new UnsupportedOperationException("not yet implemented");
        }

        final void addFound(final FileEntry entry) {
            throw new UnsupportedOperationException("not yet implemented");
        }

        final Stream<String> lines() {
            throw new UnsupportedOperationException("not yet implemented");
        }
    }

    private static class Stats {

        private final StatsTotal total = new StatsTotal();
        private final Counter foundCounter = new Counter();
        private final Map<FileType, Counter> foundTypeCounters = new TreeMap<>();

        final void reset() {
            total.reset();
            foundCounter.reset();
            foundTypeCounters.clear();
        }

        final void addTotal(final FileEntry entry) {
            total.addTotal(entry);
        }

        final void addFound(final FileEntry entry) {
            foundCounter.increment();
            foundTypeCounters.computeIfAbsent(entry.type(), any -> new Counter()).increment();
        }

        final Stream<String> lines() {
            return Stream.concat(total.lines(), localLines());
        }

        private Stream<String> localLines() {
            final List<Supplier<String>> head = List.of(() -> "",
                                                        () -> "%,12d entries found".formatted(foundCounter.value()));
            final Stream<Supplier<String>> tail = foundTypeCounters.entrySet()
                                                                   .stream()
                                                                   .map(entry -> () -> line(entry));
            return Stream.concat(head.stream(), tail).map(Supplier::get);
        }

        private String line(final Map.Entry<FileType, Counter> entry) {
            return "%,16d of type %s".formatted(entry.getValue().value(), entry.getKey());
        }
    }
}
