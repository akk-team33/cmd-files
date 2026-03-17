package de.team33.cmd.files.job;

import de.team33.patterns.io.delta.IOProblem;

import java.util.function.Supplier;

import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.Logger.Level.WARNING;

class Problems {

    private static final System.Logger LOGGER = System.getLogger(Problems.class.getCanonicalName());

    static void log(final IOProblem problem) {
        final Supplier<String> msgSupplier = () -> "Cannot access <%s>".formatted(problem.path());
        LOGGER.log(WARNING, msgSupplier);
        LOGGER.log(DEBUG, msgSupplier, problem.cause());
    }
}
