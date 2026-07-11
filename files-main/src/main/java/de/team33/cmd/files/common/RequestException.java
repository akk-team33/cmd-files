package de.team33.cmd.files.common;

import de.team33.patterns.io.deimos.TextIO;

public class RequestException extends Exception {

    public RequestException(final String message) {
        super(message);
    }

    public static RequestException read(final Class<?> referringClass, final String resourceName) {
        return new RequestException(TextIO.read(referringClass, resourceName));
    }

    public static Function format(final Class<?> referringClass) {
        return format(referringClass, referringClass.getSimpleName() + ".txt");
    }

    public static Function format(final Class<?> referringClass, final String resourceName) {
        return args -> new RequestException(TextIO.read(referringClass, resourceName).formatted(args));
    }

    public final RequestException causedBy(final Throwable cause) {
        initCause(cause);
        return this;
    }

    @FunctionalInterface
    public interface Function {
        RequestException apply(Object... args);
    }
}
