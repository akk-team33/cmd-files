package de.team33.cmd.files.job;

import de.team33.cmd.files.balancing.Relative;
import de.team33.cmd.files.balancing.Relatives;
import de.team33.cmd.files.balancing.State;
import de.team33.cmd.files.common.Condition;
import de.team33.cmd.files.common.Counter;
import de.team33.cmd.files.common.Output;
import de.team33.cmd.files.common.RequestException;

import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.function.Supplier;

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

    public static Runnable job(final Condition condition) throws RequestException {
        return Optional.of(condition.args())
                       .filter(args -> 4 == args.size())
                       .map(args -> new Comparing(condition.out(),
                                                  Path.of(args.get(2)),
                                                  Path.of(args.get(3))))
                       .orElseThrow(condition.toRequestException(Comparing.class));
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
