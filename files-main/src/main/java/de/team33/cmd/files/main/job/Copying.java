package de.team33.cmd.files.main.job;

import de.team33.cmd.files.main.common.Output;
import de.team33.cmd.files.main.common.RequestException;
import de.team33.cmd.files.main.finder.Pattern;
import de.team33.patterns.enums.alpha.Values;
import de.team33.patterns.io.alpha.FileEntry;
import de.team33.patterns.io.alpha.FileIndex;
import de.team33.patterns.io.alpha.FilePolicy;

import java.nio.file.Path;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static de.team33.cmd.files.main.job.Util.cmdLine;
import static de.team33.cmd.files.main.job.Util.cmdName;
import static java.util.Objects.requireNonNullElse;

class Copying implements Runnable {

    static final String EXCERPT = "Copy files and their relative file structure.";

    private final Output out;
    private final Set<Strategy> strategies;
    private final Path source;
    private final Path target;

    public Copying(final Output out, final Set<Strategy> strategies, final Path source, final Path target) {
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
            final Set<Strategy> strategies = Strategy.of(args.get(2));
            final Path source = Path.of(args.get(3));
            final Path target = Path.of(args.get(4));
            return new Copying(out, strategies, source, target);
        }
        throw RequestException.format(Listing.class, "Copying.txt", cmdLine(args), cmdName(args));
    }

    @Override
    public final void run() {
        throw new UnsupportedOperationException("not yet implemented");
    }

    private enum Strategy {
        C,
        U,
        R,
        D;

        private static final Values<Strategy> VALUES = Values.of(Strategy.class);
        private static final Supplier<EnumSet<Strategy>> NEW_SET = () -> EnumSet.noneOf(Strategy.class);

        private static Set<Strategy> of(final String combo) {
            final String upperCombo = combo.toUpperCase();
            return VALUES.findAll(value -> upperCombo.contains(value.name()))
                         .collect(Collectors.toCollection(NEW_SET));
        }
    }
}
