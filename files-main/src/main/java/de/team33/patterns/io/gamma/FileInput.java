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

    private FileInput(final Reading reading, final Charset charset,
                      final XFunction<? super BufferedReader, ? extends T, ? extends IOException> method) {
        this.input = reading.input(method, charset);
    }

    private FileInput(final Reading reading,
                      final XFunction<? super InputStream, ? extends T, ? extends IOException> method) {
        this.input = reading.input(method);
    }

    public FileInput(final Path path, final Charset charset,
                     final XFunction<? super BufferedReader, ? extends T, ? extends IOException> method) {
        this(() -> Files.newInputStream(path), charset, method);
    }

    public FileInput(final Path path,
                     final XFunction<? super InputStream, ? extends T, ? extends IOException> method) {
        this(() -> Files.newInputStream(path), method);
    }

    @Override
    public final T read() throws IOException {
        return input.read();
    }
}
