package de.team33.cmd.files.main.moving;

public class ResolverException extends RuntimeException {

    ResolverException(String message) {
        super(message);
    }

    ResolverException(final Throwable cause) {
        super(cause.getMessage(), cause);
    }
}