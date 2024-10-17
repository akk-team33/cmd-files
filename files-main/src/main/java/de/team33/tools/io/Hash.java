package de.team33.tools.io;

public record Hash(byte[] bytes, String hexString, String compactString) {

    public static Hash by(byte[] bytes) {
        final String hexString = Bytes.toHexString(bytes);
        final String compactString = Bytes.toCompactString(bytes);
        return new Hash(bytes, hexString, compactString);
    }
}
