package de.team33.patterns.io.gamma;

import de.team33.patterns.exceptional.dione.XFunction;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileInput<T> implements Input<T> {

    private final Input<T> input;

    public FileInput(final Path path, final Charset charset,
                     final XFunction<? super BufferedReader, ? extends T, ? extends IOException> method) {
        this.input = reading(path).input(method, charset);
    }

    public FileInput(final Path path,
                     final XFunction<? super InputStream, ? extends T, ? extends IOException> method) {
        this.input = reading(path).input(method);
    }

    private static Reading reading(final Path path) {
        return () -> Files.newInputStream(path);
    }

    @Override
    public final T read() throws IOException {
        return input.read();
    }
}
