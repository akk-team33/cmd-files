package de.team33.patterns.io.gamma;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;

/**
 * A {@link FileIO} implementation for reading and writing complete file
 * contents as byte arrays.
 * <p>
 * The complete content of the underlying file is held in memory when
 * reading. Writing replaces the current file content with the supplied byte
 * array.
 */
public class BytesIO extends FileIO<byte[]> {

    /**
     * Creates a new {@code BytesIO} for the given file.
     *
     * @param path the file to read from and write to
     */
    private BytesIO(final Path path) {
        super(path, BytesIO::readBytes, BytesIO::writeBytes);
    }

    /**
     * Reads all bytes from the given input stream.
     *
     * @param in the input stream to read from
     * @return the complete content of the input stream
     * @throws IOException if an I/O error occurs while reading
     */
    static byte[] readBytes(final InputStream in) throws IOException {
        return in.readAllBytes();
    }

    /**
     * Writes the given bytes to the output stream.
     *
     * @param out   the output stream to write to
     * @param bytes the bytes to write
     * @throws IOException if an I/O error occurs while writing
     */
    static void writeBytes(final OutputStream out, final byte[] bytes) throws IOException {
        out.write(bytes);
    }

    /**
     * Creates a new {@code BytesIO} for the given file.
     * <p>
     * Writing through the returned instance replaces the current file
     * content with the supplied byte array.
     *
     * @param path the file to read from and write to
     * @return a {@code BytesIO} for the specified file
     */
    public static BytesIO by(final Path path) {
        return new BytesIO(path);
    }

    /**
     * Creates an {@link Input} that reads all bytes from the specified
     * classpath resource.
     * <p>
     * The resulting input only supports reading. The underlying input stream
     * is automatically closed after reading.
     *
     * @param refClass     the class used to resolve the resource
     * @param resourceName the resource name
     * @return an {@code Input} producing the resource content as a byte array
     */
    public static Input<byte[]> by(final Class<?> refClass, final String resourceName) {
        return Reading.by(refClass, resourceName).input(BytesIO::readBytes);
    }
}
