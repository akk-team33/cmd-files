package de.team33.tools.io;

class Bytes {

    public static byte[] compact(final byte[] normal, final int length, final Operation op) {
        final byte[] result = new byte[length];
        for (int i = 0, k = 0, n = 0;
             i == 0 || k != 0 || n != 0;
             i += 1, k = i % length, n = i % normal.length) {
            result[k] = op.apply(result[k], normal[n]);
        }
        return result;
    }

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

    interface Operation {

        Operation ADD = (left, right) -> (byte) (left + right);
        Operation XOR = (left, right) -> (byte) (left ^ right);

        byte apply(byte left, byte right);
    }
}
