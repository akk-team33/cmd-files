package de.team33.patterns.io.gamma;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Properties;

public class PropertiesIO extends FileIO<Properties> {

    private PropertiesIO(final Path path, final Charset charset) {
        super(path, charset, Util::readProps, Util::writeProps);
    }

    public static PropertiesIO by(final Path path, final Charset charset) {
        return new PropertiesIO(path, charset);
    }

    public static PropertiesIO by(final Path path) {
        return by(path, StandardCharsets.UTF_8);
    }

    public static Input<Properties> by(final Class<?> refClass, final String name, final Charset charset) {
        return Util.input(() -> refClass.getResourceAsStream(name), charset, Util::readProps);
    }

    public static Input<Properties> by(final Class<?> refClass, final String name) {
        return by(refClass, name, StandardCharsets.UTF_8);
    }
}
