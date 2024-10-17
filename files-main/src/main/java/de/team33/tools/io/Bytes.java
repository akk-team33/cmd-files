package de.team33.tools.io;

class Bytes {

    static String toHexString(final byte[] bytes) {
        final StringBuilder sb = new StringBuilder();
        for (final byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    static String toCompactString(final byte[] bytes) {
        final int shortLength = bytes.length / 5;
        final byte[] shortBytes = new byte[shortLength];
        throw new UnsupportedOperationException("not yet implemented");
    }
}
