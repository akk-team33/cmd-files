package de.team33.tools.io;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;

@SuppressWarnings("unused")
public enum StrictHashing implements FileHashing {

    MD5(StreamHashing.MD5, 32),
    SHA_1(StreamHashing.SHA_1, 40),
    SHA_256(StreamHashing.SHA_256, 64),
    SHA_512(StreamHashing.SHA_512, 128);

    private final StreamHashing backing;
    private final int resultLength;

    StrictHashing(final StreamHashing backing, final int resultLength) {
        this.backing = backing;
        this.resultLength = resultLength;
    }

    @Override
    public final String hash(final Path filePath) {
        try (final InputStream in = Files.newInputStream(filePath)) {
            return Bytes.toHexString(backing.hash(in));
        } catch (final IOException e) {
            throw new IllegalStateException("Could not read file content of <" + filePath + ">", e);
        }
    }

    @Override
    public final String algorithm() {
        return backing.algorithm();
    }

    @Override
    public final int resultLength() {
        return resultLength;
    }
}
