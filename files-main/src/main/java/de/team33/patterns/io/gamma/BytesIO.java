package de.team33.patterns.io.gamma;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

public class BytesIO extends FileIO<byte[]> {

    private BytesIO(final Path path, final Charset charset) {
        super(path, BytesIO::readBytes, BytesIO::writeBytes);
    }

    static byte[] readBytes(final InputStream in) throws IOException {
        return in.readAllBytes();
    }

    static void writeBytes(final OutputStream out, final byte[] bytes) throws IOException {
        out.write(bytes);
    }

    public static BytesIO by(final Path path, final Charset charset) {
        return new BytesIO(path, charset);
    }

    public static BytesIO by(final Path path) {
        return by(path, StandardCharsets.UTF_8);
    }

    public static Input<byte[]> by(final Class<?> refClass, final String resourceName) {
        return Reading.by(refClass, resourceName).input(BytesIO::readBytes);
    }
}
