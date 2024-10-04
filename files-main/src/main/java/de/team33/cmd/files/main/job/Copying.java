package de.team33.cmd.files.main.job;

import de.team33.cmd.files.main.balancing.Relative;
import de.team33.cmd.files.main.balancing.Relatives;
import de.team33.cmd.files.main.balancing.State;
import de.team33.cmd.files.main.common.Counter;
import de.team33.cmd.files.main.common.Output;
import de.team33.cmd.files.main.common.RequestException;
import de.team33.patterns.enums.alpha.Values;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileTime;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static de.team33.cmd.files.main.job.Util.cmdLine;
import static de.team33.cmd.files.main.job.Util.cmdName;

class Copying implements Runnable {

    static final String EXCERPT = "Copy files and their relative file structure.";
    private static final Action NO_ACTION = relative -> "ignored (" + relative.state() + ")";

    private final Output out;
    private final Set<Strategy> strategies;
    private final Path source;
    private final Path target;
    private final Stats stats = new Stats();
    private final Set<Path> targetDirs = new HashSet<>();

    private Copying(final Output out, final Set<Strategy> strategies, final Path source, final Path target) {
        this.out = out;
        this.strategies = strategies;
        this.source = source;
        this.target = target;
    }

    public static Runnable job(final Output out, final List<String> args) throws RequestException {
        assert 1 < args.size();
        assert Regular.COPY.name().equalsIgnoreCase(args.get(1));
        // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
        if (5 == args.size()) {
            final Set<Strategy> strategies = Strategy.parse(args.get(2));
            final Path source = Path.of(args.get(3));
            final Path target = Path.of(args.get(4));
            return new Copying(out, strategies, source, target);
        }
        throw RequestException.format(Copying.class, "Copying.txt", cmdLine(args), cmdName(args));
    }

    @Override
    public final void run() {
        stats.reset();
        Relatives.stream(source, target)
                 .forEach(this::process);
        out.printf("%n%16s ...%n", "States");
        stats.stateCounters.forEach(
                (state, counter) -> out.printf("%16d %s%n", counter.value(), state));
        out.printf("%n%16s ...%n", "Results");
        stats.resultCounters.forEach(
                (result, counter) -> out.printf("%16d %s%n", counter.value(), result));
        out.printf("%n%16d entries processed in total.%n%n", stats.totalCounter.value());
    }

    private void process(final Relative relative) {
        out.printf("%s ...", relative.path());
        final State state = relative.state();
        final Action action = availableAction(state);
        final String result = action.run(relative);
        stats.add(state, result);
        out.printf(" %s%n", result);
    }

    private Action availableAction(final State state) {
        return strategies.stream()
                         .filter(strategy -> strategy.supports(state))
                         .findAny()
                         .map(strategy -> strategy.toAction(this))
                         .orElse(NO_ACTION);
    }

    private String create(final Relative relative) {
        return copy(relative, "created");
    }

    private String update(final Relative relative) {
        return copy(relative, "updated");
    }

    private String override(final Relative relative) {
        return copy(relative, "balanced");
    }

    private String revert(final Relative relative) {
        return copy(relative, "reverted");
    }

    private String delete(final Relative relative) {
        try {
            Files.delete(relative.target().path());
            return " deleted";
        } catch (final IOException e) {
            //problems.put(relative.path(), e);
            return String.format("failed deletion:%n" +
                                 "    Message   : %s%n" +
                                 "    Exception : %s", e.getMessage(), e.getClass().getCanonicalName());
        }
    }

    private String copy(final Relative relative, final String okText) {
        try {
            final Path targetDir = relative.target().path().getParent();
            if (targetDirs.add(targetDir)) {
                Files.createDirectories(targetDir);
            }
            Files.copy(relative.source().path(), relative.target().path(), StandardCopyOption.REPLACE_EXISTING);
            Files.setLastModifiedTime(relative.target().path(), FileTime.from(relative.source().lastModified()));
            return okText;
        } catch (final IOException e) {
            //problems.put(relative.path(), e);
            return String.format("failed copying:%n" +
                                 "    Message   : %s%n" +
                                 "    Exception : %s", e.getMessage(), e.getClass().getCanonicalName());
        }
    }

    private enum Strategy {
        C(copying -> copying::create, State.TARGET_IS_MISSING),
        U(copying -> copying::update, State.SOURCE_IS_MORE_RECENT),
        O(copying -> copying::override, State.AMBIGUOUS),
        R(copying -> copying::revert, State.AMBIGUOUS, State.TARGET_IS_MORE_RECENT),
        D(copying -> copying::delete, State.SOURCE_IS_MISSING);

        private static final Values<Strategy> VALUES = Values.of(Strategy.class);
        private static final Supplier<EnumSet<Strategy>> NEW_SET = () -> EnumSet.noneOf(Strategy.class);

        private final Set<State> supported;
        private final Function<Copying, Action> toAction;

        Strategy(final Function<Copying, Action> toAction, final State... supported) {
            this.toAction = toAction;
            this.supported = Set.of(supported);
        }

        private static Set<Strategy> parse(final String strategy) {
            final String upperStrategy = strategy.toUpperCase();
            return VALUES.findAll(value -> upperStrategy.contains(value.name()))
                         .collect(Collectors.toCollection(NEW_SET));
        }

        private Action toAction(final Copying copying) {
            return toAction.apply(copying);
        }

        private boolean supports(final State state) {
            return supported.contains(state);
        }
    }

    private interface Action {
        String run(Relative relative);
    }

    private static class Stats {

        private final Counter totalCounter = new Counter();
        private final Map<State, Counter> stateCounters = new TreeMap<>();
        private final Map<String, Counter> resultCounters = new TreeMap<>();

        private synchronized void add(final State state, final String result) {
            totalCounter.increment();
            stateCounters.computeIfAbsent(state, any -> new Counter())
                         .increment();
            resultCounters.computeIfAbsent(result, any -> new Counter())
                          .increment();
        }

        private void reset() {
            totalCounter.reset();
            stateCounters.clear();
            resultCounters.clear();
        }
    }
}
