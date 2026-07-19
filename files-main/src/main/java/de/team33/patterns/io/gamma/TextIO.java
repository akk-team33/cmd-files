package de.team33.patterns.io.gamma;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

public class TextIO extends FileIO<String> {

    private TextIO(final Path path, final Charset charset) {
        super(path, charset, TextIO::readString, TextIO::writeString);
    }

    static String readString(final BufferedReader reader) throws IOException {
        try (final StringWriter writer = new StringWriter()) {
            reader.transferTo(writer);
            return writer.toString();
        }
    }

    static void writeString(final Writer writer, final String text) throws IOException {
        writer.write(text);
    }

    public static TextIO by(final Path path, final Charset charset) {
        return new TextIO(path, charset);
    }

    public static TextIO by(final Path path) {
        return by(path, StandardCharsets.UTF_8);
    }

    public static Input<String> by(final Class<?> refClass, final String resourceName, final Charset charset) {
        return Reading.by(refClass, resourceName).input(charset, TextIO::readString);
    }

    public static Input<String> by(final Class<?> refClass, final String resourceName) {
        return by(refClass, resourceName, StandardCharsets.UTF_8);
    }
}
