package de.team33.cmd.files.job;

import java.nio.file.Path;
import java.util.function.Supplier;

class IOFault extends IllegalStateException  {

    IOFault(final String messageIntro, final Path path, final Throwable cause) {
        super(("%s:%n" +
                "    Path : %s%n" +
                "    Message : %s%n" +
                "    Exception : %s%n").formatted(messageIntro, path, cause.getCause(), cause.getClass().getName()),
              cause);
    }

    static IOFault by(final String messageIntro, final Path path, final Throwable cause) {
        return new IOFault(messageIntro, path, cause);
    }

    static Supplier<IOFault> newIOFault(final String messageIntro, final Path path, final Throwable cause) {
        return () -> new IOFault(messageIntro, path, cause);
    }
}
