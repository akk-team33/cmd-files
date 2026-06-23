package de.team33.cmd.files.common;

import de.team33.patterns.io.deimos.TextIO;

import java.util.Optional;
import java.util.function.Function;

public class RequestException extends Exception {

    public RequestException(final String message) {
        super(message);
    }

    public RequestException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public static RequestException read(final Class<?> referringClass, final String resourceName) {
        return new RequestException(TextIO.read(referringClass, resourceName));
    }

    public static RequestException format(final Class<?> referringClass,
                                          final String resourceName,
                                          final Object... args) {
        return new RequestException(String.format(TextIO.read(referringClass, resourceName), args));
    }

    public static Function<Throwable, RequestException> stage(final Class<?> referringClass, final Object... args) {
        final String message = TextIO.read(referringClass, referringClass.getSimpleName() + ".txt")
                                     .formatted(args);
        return cause -> new RequestException(composed(cause, message), cause);
    }

    private static String composed(final Throwable cause, final String messageBody) {
        return Optional.ofNullable(cause)
                       .map(Throwable::getMessage)
                       .map(message -> message.indent(4).trim())
                       .map("Problem:%n%n    %s%n%n"::formatted)
                       .orElse("") + messageBody;
    }
}
