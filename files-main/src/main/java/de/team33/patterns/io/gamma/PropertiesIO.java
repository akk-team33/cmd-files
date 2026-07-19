package de.team33.patterns.io.gamma;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Properties;

/**
 * A {@link FileIO} implementation for reading and writing
 * {@link Properties} files.
 * <p>
 * The complete set of properties is held in memory when reading. Writing
 * replaces the current file content with the supplied properties. The written
 * file contains a generated comment identifying the creation time and this
 * class as the source.
 */
public class PropertiesIO extends FileIO<Properties> {

    /**
     * Creates a new {@code PropertiesIO} for the given file using the specified
     * character encoding.
     *
     * @param path    the file to read from and write to
     * @param charset the character encoding used for reading and writing
     */
    private PropertiesIO(final Path path, final Charset charset) {
        super(path, charset, PropertiesIO::readProps, PropertiesIO::writeProps);
    }

    /**
     * Reads properties from the given reader.
     *
     * @param reader the reader to read from
     * @return the properties loaded from the reader
     * @throws IOException if an I/O error occurs while reading
     */
    static Properties readProps(final BufferedReader reader) throws IOException {
        final Properties properties = new Properties();
        properties.load(reader);
        return properties;
    }

    /**
     * Writes the given properties to the specified writer.
     * <p>
     * A generated comment containing the current timestamp and the class
     * name is written as part of the properties file header.
     *
     * @param writer     the writer to write to
     * @param properties the properties to write
     * @throws IOException if an I/O error occurs while writing
     */
    static void writeProps(final BufferedWriter writer, final Properties properties) throws IOException {
        properties.store(writer, "%s - by %s".formatted(LocalDateTime.now().toString(),
                                                        PropertiesIO.class.getCanonicalName()));
    }

    /**
     * Creates a new {@code PropertiesIO} for the given file using the specified
     * character encoding.
     *
     * @param path    the file to read from and write to
     * @param charset the character encoding used for reading and writing
     * @return a {@code PropertiesIO} for the specified file
     */
    public static PropertiesIO by(final Path path, final Charset charset) {
        return new PropertiesIO(path, charset);
    }

    /**
     * Creates a new {@code PropertiesIO} for the given file using UTF-8
     * encoding.
     *
     * @param path the file to read from and write to
     * @return a {@code PropertiesIO} for the specified file
     */
    public static PropertiesIO by(final Path path) {
        return by(path, StandardCharsets.UTF_8);
    }

    /**
     * Creates an {@link Input} that reads the specified classpath resource as
     * a {@link Properties} instance.
     * <p>
     * The resulting input only supports reading. The underlying input stream
     * is automatically closed after reading.
     *
     * @param refClass     the class used to resolve the resource
     * @param resourceName the resource name
     * @param charset      the character encoding used for reading
     * @return an {@code Input} producing the loaded properties
     */
    public static Input<Properties> by(final Class<?> refClass, final String resourceName, final Charset charset) {
        return Reading.by(refClass, resourceName)
                      .input(charset, PropertiesIO::readProps);
    }

    /**
     * Creates an {@link Input} that reads the specified classpath resource as
     * a UTF-8 encoded {@link Properties} instance.
     * <p>
     * The resulting input only supports reading. The underlying input stream
     * is automatically closed after reading.
     *
     * @param refClass     the class used to resolve the resource
     * @param resourceName the resource name
     * @return an {@code Input} producing the loaded properties
     */
    public static Input<Properties> by(final Class<?> refClass, final String resourceName) {
        return by(refClass, resourceName, StandardCharsets.UTF_8);
    }
}
