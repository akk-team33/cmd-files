package de.team33.patterns.io.gamma;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

public class TextInput extends FileInput<String> {

    private TextInput(final Path path, final Charset charset) {
        super(path, charset, Util::readString);
    }

    public static TextInput by(final Path path, final Charset charset) {
        return new TextInput(path, charset);
    }

    public static TextInput by(final Path path) {
        return by(path, StandardCharsets.UTF_8);
    }

    public static Input<String> by(final Class<?> refClass, final String name, final Charset charset) {
        return Util.input(() -> refClass.getResourceAsStream(name), charset, Util::readString);
    }

    public static Input<String> by(final Class<?> refClass, final String name) {
        return by(refClass, name, StandardCharsets.UTF_8);
    }
}
