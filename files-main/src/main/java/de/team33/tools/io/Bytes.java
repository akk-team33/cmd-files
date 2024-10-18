package de.team33.tools.io;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

class Bytes {

    private static final Map<ExpectedStringLengthKey, Integer> EXPECTED_STRING_LENGTH = new ConcurrentHashMap<>();

    static byte[] compact(final byte[] normal, final int length, final Operation op) {
        final byte[] result = new byte[length];
        for (int i = 0, k = 0, n = 0;
             i == 0 || k != 0 || n != 0;
             i += 1, k = i % length, n = i % normal.length) {
            result[k] = op.apply(result[k], normal[n]);
        }
        return result;
    }

    private static int newExpectedStringLength(ExpectedStringLengthKey key) {
        return BigInteger.ONE.shiftLeft(key.bytesLength * Byte.SIZE)
                             .subtract(BigInteger.ONE)
                             .toString(key.radix)
                             .length();
    }

    private static int expectedStringLength(final int bytesLength, final int radix) {
        return EXPECTED_STRING_LENGTH.computeIfAbsent(new ExpectedStringLengthKey(bytesLength, radix),
                                                      Bytes::newExpectedStringLength);
    }

    static String toString(final byte[] bytes, final int radix) {
        final int expectedLength = expectedStringLength(bytes.length, radix);
        final byte[] stage = IntStream.range(0, bytes.length)
                                      .collect(() -> new byte[bytes.length + 1],
                                               (array, index) -> array[index + 1] = bytes[index],
                                               (left, right) -> notSupported("combiner"));
        final String resultTail = new BigInteger(stage).toString(radix);
        return Stream.concat(Stream.generate(() -> "0")
                                   .limit(expectedLength - resultTail.length()),
                             Stream.of(resultTail))
                     .collect(Collectors.joining());
    }

    private static void notSupported(final String subject) {
        throw new UnsupportedOperationException("a %s is not supported".formatted(subject));
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

    private record ExpectedStringLengthKey(int bytesLength, int radix) {
    }
}
