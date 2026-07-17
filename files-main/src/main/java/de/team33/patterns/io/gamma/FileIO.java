package de.team33.patterns.io.gamma;

import de.team33.patterns.exceptional.dione.XBiConsumer;
import de.team33.patterns.exceptional.dione.XFunction;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileIO<T> extends FileInput<T> implements Output<T> {

    private final Output<T> output;

    public FileIO(final Path path, final Charset charset,
                  final XFunction<? super BufferedReader, ? extends T, ? extends IOException> inputMethod,
                  final XBiConsumer<? super BufferedWriter, ? super T, ? extends IOException> outputMethod) {
        super(path, charset, inputMethod);
        this.output = writing(path).output(outputMethod, charset);
    }

    public FileIO(final Path path,
                  final XFunction<? super InputStream, ? extends T, ? extends IOException> inputMethod,
                  final XBiConsumer<? super OutputStream, ? super T, ? extends IOException> outputMethod) {
        super(path, inputMethod);
        this.output = writing(path).output(outputMethod);
    }

    private static Writing writing(final Path path) {
        return () -> Files.newOutputStream(path);
    }

    @Override
    public final void write(final T origin) throws IOException {
        output.write(origin);
    }
}
