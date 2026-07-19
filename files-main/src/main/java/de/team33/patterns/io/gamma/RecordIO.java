package de.team33.patterns.io.gamma;

import de.team33.patterns.io.gamma.json.JsonMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

public class RecordIO<T extends Record> extends FileIO<T> {

    private RecordIO(final Class<T> recordClass, final Path path, final Charset charset) {
        super(path, charset, reader -> readRecord(recordClass, reader), RecordIO::writeString);
    }

    static <T extends Record> T readRecord(final Class<T> recordClass,
                                           final BufferedReader reader) throws IOException {
        return JsonMapper.map(recordClass, TextIO.readString(reader));
    }

    static <T extends Record> void writeString(final Writer writer,
                                               final T value) throws IOException {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public static <T extends Record> RecordIO<T> by(final Class<T> recordClass,
                                                    final Path path,
                                                    final Charset charset) {
        return new RecordIO<>(recordClass, path, charset);
    }

    public static <T extends Record> RecordIO<T> by(final Class<T> recordClass, final Path path) {
        return by(recordClass, path, StandardCharsets.UTF_8);
    }

    public static <T extends Record> Input<T> by(final Class<T> recordClass,
                                                 final Class<?> refClass,
                                                 final String resourceName,
                                                 final Charset charset) {
        return Reading.by(refClass, resourceName)
                      .input(charset, reader -> readRecord(recordClass, reader));
    }

    public static <T extends Record> Input<T> by(final Class<T> recordClass,
                                                 final Class<?> refClass,
                                                 final String resourceName) {
        return by(recordClass, refClass, resourceName, StandardCharsets.UTF_8);
    }
}