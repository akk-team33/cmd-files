package de.team33.patterns.hashing.pandia;

import de.team33.patterns.exceptional.dione.XSupplier;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@SuppressWarnings("WeakerAccess")
public enum Algorithm {

    MD5("MD5", 16),
    SHA_1("SHA-1", 20),
    SHA_256("SHA-256", 32),
    SHA_512("SHA-512", 64);

    private static final int ONE_KB = 1024;

    private final String ident;
    private final int byteSize;
    private final int bitSize;
    private final BigInteger maxInteger;

    Algorithm(final String ident, final int byteSize) {
        this.ident = ident;
        this.byteSize = byteSize;
        this.bitSize = byteSize * Byte.SIZE;
        this.maxInteger = BigInteger.ONE.shiftLeft(bitSize).subtract(BigInteger.ONE);
    }

    public final String ident() {
        return ident;
    }

    public final int byteSize() {
        return byteSize;
    }

    public final int bitSize() {
        return bitSize;
    }

    public final BigInteger maxInteger() {
        return maxInteger;
    }

    public final Hash hash(final String origin) {
        return hash(origin, StandardCharsets.UTF_8);
    }

    public final Hash hash(final String origin, final Charset charset) {
        return hash(origin.getBytes(charset));
    }

    public final Hash hash(final byte[] bytes) {
        return hash(() -> new ByteArrayInputStream(bytes));
    }

    public final Hash hash(final Path path) {
        return hash(() -> Files.newInputStream(path));
    }

    public final Hash hash(final XSupplier<? extends InputStream, ? extends IOException> streamable) {
        try (final InputStream in = streamable.get()) {
            return hash(in);
        } catch (final IOException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    public final Hash hash(final InputStream in) throws IOException {
        final MessageDigest md = getMessageDigest();
        final byte[] buffer = new byte[ONE_KB];
        for (int read = in.read(buffer, 0, ONE_KB); 0 < read; read = in.read(buffer, 0, ONE_KB)) {
            md.update(buffer, 0, read);
        }
        return new Hash(md.digest());
    }

    public final Hash parse(final String hash, final String digits) {
        final BigInteger base = BigInteger.valueOf(digits.length());
        BigInteger result = BigInteger.ZERO;
        for (int index = 0; index < hash.length(); ++index) {
            result = result.multiply(base);
            final char c = hash.charAt(index);
            final int value = digits.indexOf(c);
            if (0 > value) {
                throw new IllegalArgumentException("Illegal character: '%s' at index %d in '%s'".formatted(c, index, hash));
            }
            result = result.add(BigInteger.valueOf(value));
        }
        return parse(result);
    }

    public final Hash parse(final BigInteger bigInteger) {
        if ((BigInteger.ZERO.compareTo(bigInteger) > 0) || (maxInteger.compareTo(bigInteger) < 0)) {
            throw new IllegalArgumentException("Illegal BigInteger value: %s".formatted(bigInteger));
        }
        return parse(bigInteger.toByteArray());
    }

    private Hash parse(final byte[] bytes) {
        final byte[] hashBytes = new byte[byteSize];
        int srcIdx = bytes.length;
        int tgtIdx = hashBytes.length;
        while (0 < srcIdx && 0 < tgtIdx) {
            hashBytes[--tgtIdx] = bytes[--srcIdx];
        }
        return new Hash(hashBytes);
    }

    private MessageDigest getMessageDigest() {
        try {
            return MessageDigest.getInstance(ident);
        } catch (final NoSuchAlgorithmException e) {
            throw new IllegalStateException("Message digest algorithm <" + ident + "> is not supported", e);
        }
    }
}