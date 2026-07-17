package de.team33.patterns.io.gamma;

import de.team33.patterns.exceptional.dione.XBiConsumer;
import de.team33.patterns.exceptional.dione.XFunction;
import de.team33.patterns.exceptional.dione.XSupplier;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.util.Objects.requireNonNull;

public class Resource {

    @SuppressWarnings("rawtypes")
    private static final XSupplier NOT_SUPPORTED = () -> {
        throw new UnsupportedOperationException("operation not supported");
    };

    private final XSupplier<? extends InputStream, ? extends IOException> newInputStream;
    private final XSupplier<? extends OutputStream, ? extends IOException> newOutputStream;

    private Resource(final XSupplier<? extends InputStream, ? extends IOException> newInputStream,
                     final XSupplier<? extends OutputStream, ? extends IOException> newOutputStream) {
        this.newInputStream = requireNonNull(newInputStream);
        this.newOutputStream = requireNonNull(newOutputStream);
    }

    public static Resource using(final XSupplier<? extends InputStream, ? extends IOException> newInputStream,
                                 final XSupplier<? extends OutputStream, ? extends IOException> newOutputStream) {
        return new Resource(newInputStream, newOutputStream);
    }

    @SuppressWarnings("unchecked")
    public static Resource readOnly(final XSupplier<? extends InputStream, ? extends IOException> newInputStream) {
        return using(newInputStream, NOT_SUPPORTED);
    }

    @SuppressWarnings("unchecked")
    public static Resource writeOnly(final XSupplier<? extends OutputStream, ? extends IOException> newOutputStream) {
        return using(NOT_SUPPORTED, newOutputStream);
    }

    public static Resource by(final Path path) {
        return using(() -> Files.newInputStream(path), () -> Files.newOutputStream(path));
    }

    public static Resource by(final Class<?> refClass, final String name) {
        return readOnly(() -> refClass.getResourceAsStream(name));
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

}
