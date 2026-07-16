package de.team33.patterns.io.gamma;

import de.team33.patterns.exceptional.dione.XBiConsumer;
import de.team33.patterns.exceptional.dione.XFunction;
import de.team33.patterns.exceptional.dione.XSupplier;

import java.io.*;

public class Resource {

    private final XSupplier<? extends InputStream, ? extends IOException> newInputStream;
    private final XSupplier<? extends OutputStream, ? extends IOException> newOutputStream;

    public Resource(final XSupplier<? extends InputStream, ? extends IOException> newInputStream,
                    final XSupplier<? extends OutputStream, ? extends IOException> newOutputStream) {
        this.newInputStream = newInputStream;
        this.newOutputStream = newOutputStream;
    }

    private <T> XBiConsumer<OutputStream, T, IOException>
    writeMethod(final XBiConsumer<? super BufferedWriter, ? super T, ? extends IOException> charMethod) {
        return (out, subject) -> {
            try (final Writer writer = new OutputStreamWriter(out);
                 final BufferedWriter bufferedWriter = new BufferedWriter(writer)) {
                charMethod.accept(bufferedWriter, subject);
            }
        };
    }

    private <T> XFunction<InputStream, T, IOException>
    readMethod(final XFunction<? super BufferedReader, ? extends T, ? extends IOException> charMethod) {
        return in -> {
            try (final Reader reader = new InputStreamReader(in);
                 final BufferedReader bufferedReader = new BufferedReader(reader)) {
                return charMethod.apply(bufferedReader);
            }
        };
    }

    public <T> Input<T>
    input(final XFunction<? super InputStream, ? extends T, ? extends IOException> method) {
        return () -> {
            try (final InputStream in = newInputStream.get()) {
                return method.apply(in);
            }
        };
    }

    public <T> Input<T>
    reading(final XFunction<? super BufferedReader, ? extends T, ? extends IOException> method) {
        return input(readMethod(method));
    }

    public <T> Output<T>
    output(final XBiConsumer<? super OutputStream, ? super T, ? extends IOException> method) {
        return origin -> {
            try (final OutputStream out = newOutputStream.get()) {
                method.accept(out, origin);
            }
        };
    }

    public <T> Output<T>
    writing(final XBiConsumer<? super BufferedWriter, ? super T, ? extends IOException> method) {
        return output(writeMethod(method));
    }

    public interface Input<T> {

        T read() throws IOException;
    }

    public interface Output<T> {

        void write(T origin) throws IOException;
    }
}
