package de.team33.tools.io;

import java.math.BigInteger;

class Bytes {

    static byte[] compact(final byte[] normal, final int length, final Operation op) {
        final byte[] result = new byte[length];
        for (int i = 0, k = 0, n = 0;
             i == 0 || k != 0 || n != 0;
             i += 1, k = i % length, n = i % normal.length) {
            result[k] = op.apply(result[k], normal[n]);
        }
        return result;
    }

    static String toString(final byte[] bytes, final int radix) {
        final BigInteger stage = new BigInteger(bytes);
        final String result = BigInteger.ONE.shiftLeft(bytes.length * Byte.SIZE).add(stage).toString(radix);
        return (BigInteger.ZERO.compareTo(stage) > 0) ? result : result.substring(1);
    }

    static String toHexString(final byte[] bytes) {
        return toString(bytes, 16);
    }

    static String toBase32String(final byte[] bytes) {
        return toString(bytes, 32);
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
