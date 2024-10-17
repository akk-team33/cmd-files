package de.team33.tools.io;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@SuppressWarnings("unused")
enum StreamHashing {

    MD5("MD5"),
    SHA_1("SHA-1"),
    SHA_256("SHA-256"),
    SHA_512("SHA-512");

    private static final int ONE_KB = 1024;

    private final String algorithm;

    StreamHashing(final String algorithm) {
        this.algorithm = algorithm;
    }

    final String algorithm() {
        return algorithm;
    }

    final byte[] hash(final InputStream in) throws IOException {
        final MessageDigest md = getMessageDigest();
        final byte[] buffer = new byte[ONE_KB];
        for (int read = in.read(buffer, 0, ONE_KB); 0 < read; read = in.read(buffer, 0, ONE_KB)) {
            md.update(buffer, 0, read);
        }
        return md.digest();
    }

    private MessageDigest getMessageDigest() {
        try {
            return MessageDigest.getInstance(algorithm);
        } catch (final NoSuchAlgorithmException e) {
            throw new IllegalStateException("Message digest algorithm <" + algorithm + "> is not supported", e);
        }
    }
}
