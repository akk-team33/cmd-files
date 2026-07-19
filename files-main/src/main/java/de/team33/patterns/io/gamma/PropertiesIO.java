package de.team33.patterns.io.gamma;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Properties;

public class PropertiesIO extends FileIO<Properties> {

    private PropertiesIO(final Path path, final Charset charset) {
        super(path, charset, PropertiesIO::readProps, PropertiesIO::writeProps);
    }

    static Properties readProps(final BufferedReader reader) throws IOException {
        final Properties properties = new Properties();
        properties.load(reader);
        return properties;
    }

    static void writeProps(final BufferedWriter writer, Properties properties) throws IOException {
        properties.store(writer, "%s - by %s".formatted(LocalDateTime.now().toString(),
                                                        PropertiesIO.class.getCanonicalName()));
    }

    public static PropertiesIO by(final Path path, final Charset charset) {
        return new PropertiesIO(path, charset);
    }

    public static PropertiesIO by(final Path path) {
        return by(path, StandardCharsets.UTF_8);
    }

    public static Input<Properties> by(final Class<?> refClass, final String resourceName, final Charset charset) {
        return Reading.by(refClass, resourceName).input(charset, PropertiesIO::readProps);
    }

    public static Input<Properties> by(final Class<?> refClass, final String resourceName) {
        return by(refClass, resourceName, StandardCharsets.UTF_8);
    }
}
