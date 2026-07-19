package de.team33.patterns.io.gamma;

import de.team33.patterns.exceptional.dione.XFunction;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;

@FunctionalInterface
public interface Reading {

    static Reading by(final Reading reading) {
        return reading;
    }

    static Reading by(final Path path, final OpenOption... options) {
        return () -> Files.newInputStream(path, options);
    }

    static Reading by(final Class<?> refClass, final String resourceName) {
        return () -> refClass.getResourceAsStream(resourceName);
    }

    InputStream newInputStream() throws IOException;

    default <T> Input<T> input(final XFunction<? super InputStream, ? extends T, ? extends IOException> method) {
        return () -> {
            try (final InputStream in = newInputStream()) {
                return method.apply(in);
            }
        };
    }

    default <T> Input<T> input(final Charset charset,
                               final XFunction<? super BufferedReader, ? extends T, ? extends IOException> method) {
        return input(Util.inputMethod(method, charset));
    }
}
