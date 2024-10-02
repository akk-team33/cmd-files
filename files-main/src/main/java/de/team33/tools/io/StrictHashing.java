package de.team33.tools.io;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@SuppressWarnings("unused")
public enum StrictHashing implements FileHashing {

    MD5("MD5", 32),
    SHA_1("SHA-1", 40),
    SHA_256("SHA-256", 64),
    SHA_512("SHA-512", 128);

    private static final int ONE_KB = 1024;

    private final String algorithm;
    private final int resultLength;

    StrictHashing(final String algorithm, final int resultLength) {
        this.algorithm = algorithm;
        this.resultLength = resultLength;
    }

    private String tryHash(final InputStream in) throws IOException, NoSuchAlgorithmException {
        final MessageDigest md = MessageDigest.getInstance(algorithm);
        final byte[] buffer = new byte[ONE_KB];
        for (int read = in.read(buffer, 0, ONE_KB); 0 < read; read = in.read(buffer, 0, ONE_KB)) {
            md.update(buffer, 0, read);
        }
        return Bytes.toHexString(md.digest());
    }

    @Override
    public final String hash(final Path filePath) {
        try (final InputStream in = Files.newInputStream(filePath)) {
            return tryHash(in);
        } catch (final NoSuchAlgorithmException e) {
            throw new IllegalStateException("Algorithm <" + algorithm + "> is not supported", e);
        } catch (final IOException e) {
            throw new IllegalStateException("Could not read file content of <" + filePath + ">", e);
        }
    }

    @Override
    public final String algorithm() {
        return algorithm;
    }

    @Override
    public final int resultLength() {
        return resultLength;
    }
}