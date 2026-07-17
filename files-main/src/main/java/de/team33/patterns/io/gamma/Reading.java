package de.team33.patterns.io.gamma;

import de.team33.patterns.exceptional.dione.XFunction;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

@FunctionalInterface
public interface Reading {

    InputStream newInputStream() throws IOException;

    default <T> Input<T> input(final XFunction<? super InputStream, ? extends T, ? extends IOException> method) {
        return () -> {
            try (final InputStream in = newInputStream()) {
                return method.apply(in);
            }
        };
    }

    default <T> Input<T> input(final XFunction<? super BufferedReader, ? extends T, ? extends IOException> method,
                           final Charset charset) {
        return input(Util.inputMethod(method, charset));
    }
}
