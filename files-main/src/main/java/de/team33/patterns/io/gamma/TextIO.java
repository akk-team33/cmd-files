package de.team33.patterns.io.gamma;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

public class TextIO extends FileIO<String> {

    private TextIO(final Path path, final Charset charset) {
        super(path, charset, Util::readString, Util::writeString);
    }

    public static TextIO by(final Path path, final Charset charset) {
        return new TextIO(path, charset);
    }

    public static TextIO by(final Path path) {
        return by(path, StandardCharsets.UTF_8);
    }
}
