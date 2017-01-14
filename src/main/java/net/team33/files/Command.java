package net.team33.files;

import net.team33.files.commands.Help;

import java.util.function.Function;
import java.util.stream.Stream;

public enum Command {

    HELP(Help::new),
    HASH(Help::new),
    SIEVE(Help::new);

    private final Function<String[], Runnable> factory;

    Command(final Function<String[], Runnable> factory) {
        this.factory = factory;
    }

    public static Command from(final String[] args) {
        final String value = (0 < args.length) ? args[0] : null;
        return Stream.of(values())
                .filter(c -> c.name().equalsIgnoreCase(value))
                .findFirst()
                .orElse(HELP);
    }

    public final Runnable job(final String[] args) {
        return factory.apply(args);
    }
}
