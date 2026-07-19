package de.team33.patterns.io.gamma;

import de.team33.patterns.exceptional.dione.XBiConsumer;
import de.team33.patterns.exceptional.dione.XFunction;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Path;

public class FileIO<T> implements Input<T>, Output<T> {

    private final Input<T> input;
    private final Output<T> output;

    public FileIO(final Path path,
                  final XFunction<? super InputStream, ? extends T, ? extends IOException> readMethod,
                  final XBiConsumer<? super OutputStream, ? super T, ? extends IOException> writeMethod) {
        this.input = Reading.by(path).input(readMethod);
        this.output = Writing.by(path).output(writeMethod);
    }

    public FileIO(final Path path, final Charset charset,
                  final XFunction<? super BufferedReader, ? extends T, ? extends IOException> readMethod,
                  final XBiConsumer<? super BufferedWriter, ? super T, ? extends IOException> writeMethod) {
        this.input = Reading.by(path).input(charset, readMethod);
        this.output = Writing.by(path).output(charset, writeMethod);
    }

    @Override
    public final T read() throws IOException {
        return input.read();
    }

    @Override
    public final void write(final T origin) throws IOException {
        output.write(origin);
    }
}
