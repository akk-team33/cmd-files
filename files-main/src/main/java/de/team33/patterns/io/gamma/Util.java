package de.team33.patterns.io.gamma;

import de.team33.patterns.exceptional.dione.XBiConsumer;
import de.team33.patterns.exceptional.dione.XFunction;

import java.io.*;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.util.Properties;

final class Util {

    static <T> XFunction<InputStream, T, IOException>
    inputMethod(final XFunction<? super BufferedReader, ? extends T, ? extends IOException> method,
                final Charset charset) {
        return in -> {
            try (final Reader reader = new InputStreamReader(in, charset);
                 final BufferedReader bufferedReader = new BufferedReader(reader)) {
                return method.apply(bufferedReader);
            }
        };
    }

    static <T> XBiConsumer<OutputStream, T, IOException>
    outputMethod(final XBiConsumer<? super BufferedWriter, ? super T, ? extends IOException> charMethod,
                 final Charset charset) {
        return (out, subject) -> {
            try (final Writer writer = new OutputStreamWriter(out, charset);
                 final BufferedWriter bufferedWriter = new BufferedWriter(writer)) {
                charMethod.accept(bufferedWriter, subject);
            }
        };
    }

    static Properties readProps(final BufferedReader reader) throws IOException {
        final Properties properties = new Properties();
        properties.load(reader);
        return properties;
    }

    static void writeProps(final BufferedWriter writer, Properties properties) throws IOException {
        properties.store(writer, LocalDateTime.now().toString());
    }

    static <T> Input<T> input(final Reading reading,
                              final XFunction<? super InputStream, ? extends T, ? extends IOException> method) {
        return reading.input(method);
    }

    static <T> Input<T> input(final Reading reading, final Charset charset,
                              final XFunction<? super BufferedReader, ? extends T, ? extends IOException> method) {
        return reading.input(charset, method);
    }
}
