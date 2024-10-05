package de.team33.cmd.files.job;

import de.team33.cmd.files.balancing.Relative;
import de.team33.cmd.files.balancing.Relatives;
import de.team33.cmd.files.balancing.State;
import de.team33.cmd.files.common.Counter;
import de.team33.cmd.files.common.Output;
import de.team33.cmd.files.common.RequestException;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static de.team33.cmd.files.job.Util.cmdLine;
import static de.team33.cmd.files.job.Util.cmdName;

class Comparing implements Runnable {

    static final String EXCERPT = "Compare files and their relative file structure.";

    private final Output out;
    private final Path source;
    private final Path target;
    private final Stats stats = new Stats();

    private Comparing(final Output out, final Path source, final Path target) {
        this.out = out;
        this.source = source;
        this.target = target;
    }

    public static Runnable job(final Output out, final List<String> args) throws RequestException {
        assert 1 < args.size();
        assert Regular.CMP.name().equalsIgnoreCase(args.get(1));
        // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
        if (4 == args.size()) {
            final Path source = Path.of(args.get(2));
            final Path target = Path.of(args.get(3));
            return new Comparing(out, source, target);
        }
        throw RequestException.format(Listing.class, "Comparing.txt", cmdLine(args), cmdName(args));
    }

    @Override
    public final void run() {
        stats.reset();
        Relatives.stream(source, target)
                 .forEach(this::process);
        out.printf("%n%16s ...%n", "States");
        stats.stateCounters.forEach(
                (state, counter) -> out.printf("%16d %s%n", counter.value(), state));
        out.printf("%n%16d entries processed in total.%n%n", stats.totalCounter.value());
    }

    private void process(final Relative relative) {
        final State state = relative.state();
        stats.add(state);
        out.printf("%s - %s%n", relative.path(), state);
    }

    private static class Stats {

        private final Counter totalCounter = new Counter();
        private final Map<State, Counter> stateCounters = new TreeMap<>();

        private synchronized void add(final State state) {
            totalCounter.increment();
            stateCounters.computeIfAbsent(state, any -> new Counter())
                         .increment();
        }

        private void reset() {
            totalCounter.reset();
            stateCounters.clear();
        }
    }
}
