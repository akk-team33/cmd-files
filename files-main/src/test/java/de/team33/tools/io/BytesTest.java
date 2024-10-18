package de.team33.tools.io;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.stream.IntStream;

import static de.team33.tools.io.Bytes.Operation.ADD;
import static de.team33.tools.io.Bytes.Operation.XOR;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class BytesTest {

    private static byte[] bytesOf(final int value, final int length) {
        final byte[] result = new byte[length];
        IntStream.range(0, length)
                 .forEach(index -> result[index] = (byte) value);
        return result;
    }

    private static byte[] toBytes(final int... values) {
        final byte[] result = new byte[values.length];
        IntStream.range(0, values.length)
                 .forEach(index -> result[index] = (byte) values[index]);
        return result;
    }

    @ParameterizedTest
    @EnumSource
    final void toHexString(final ToHexStringCase given) {
        final String result = Bytes.toHexString(given.origin);
        assertEquals(given.expected, result);
    }

    @ParameterizedTest
    @EnumSource
    final void toBase32String(final ToBase32StringCase given) {
        final String result = Bytes.toBase32String(given.origin);
        assertEquals(given.expected, result);
    }

    @ParameterizedTest
    @EnumSource
    void compact(final CompactCase given) {
        final byte[] result = Bytes.compact(given.normal, given.length, given.op);
        assertArrayEquals(given.expected, result);
    }

    @ParameterizedTest
    @EnumSource
    final void toCompactString(final ToCompactStringCase given) {
        final String result = Bytes.toCompactString(given.origin);
        assertEquals(given.expected, result);
    }

    private enum ToHexStringCase {

        _00_ff(toBytes(0, 0, 0, 0, -1, -1, -1, -1), "00000000ffffffff"),
        _ff_00(toBytes(-1, -1, -1, -1, 0, 0, 0, 0), "ffffffff00000000"),
        _1TO16(toBytes(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16), "0102030405060708090a0b0c0d0e0f10"),
        _16TO1(toBytes(16, 15, 14, 13, 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1), "100f0e0d0c0b0a090807060504030201"),
        _ff_20(bytesOf(0xff, 20), "ffffffffffffffffffffffffffffffffffffffff"),
        _0_20(bytesOf(0, 20), "0000000000000000000000000000000000000000");

        private final byte[] origin;
        private final String expected;

        ToHexStringCase(final byte[] origin, String expected) {
            this.origin = origin;
            this.expected = expected;
        }
    }

    private enum ToBase32StringCase {

        _00_ff(toBytes(0, 0, 0, 0, -1, -1, -1, -1), "0000003vvvvvv"),
        _ff_00(toBytes(-1, -1, -1, -1, 0, 0, 0, 0), "fvvvvvs000000"),
        _1TO16(toBytes(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16), "01081g81860s40i2gb1g6gs3og"),
        _16TO1(toBytes(16, 15, 14, 13, 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1), "0g1s70q30b184gg1o60k2060g1"),
        _ff_20(bytesOf(0xff, 20), "vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv"),
        _0_20(bytesOf(0, 20), "00000000000000000000000000000000"),
        _LEN_5(bytesOf(15, 5), "1s7gu3of");

        private final byte[] origin;
        private final String expected;

        ToBase32StringCase(final byte[] origin, String expected) {
            this.origin = origin;
            this.expected = expected;
        }
    }

    private enum ToCompactStringCase {

        _00_ff_8(toBytes(0, 0, 0, 0, -1, -1, -1, -1), "d3c50e78"),
        _ff_00_8(toBytes(-1, -1, -1, -1, 0, 0, 0, 0), "d28cgq78"),
        _00_ff_10(toBytes(0, 0, 0, 0, 0, -1, -1, -1, -1, -1), "14sji299"),
        _ff_00_10(toBytes(-1, -1, -1, -1, -1, 0, 0, 0, 0, 0), "0srge1p7"),
        _1TO16(toBytes(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16), "e1gd043g"),
        _16TO1(toBytes(16, 15, 14, 13, 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1), "a2o3107g"),
        _ff_20(bytesOf(0xff, 20), "008e1s30"),
        _0_20(bytesOf(0, 20), "fh63o33s"),
        _LEN_5(bytesOf(15, 5), "4gm68714");

        private final byte[] origin;
        private final String expected;

        ToCompactStringCase(final byte[] origin, String expected) {
            this.origin = origin;
            this.expected = expected;
        }
    }

    private enum CompactCase {

        _7_5_ADD(bytesOf(1, 7), 5, bytesOf(7, 5), ADD),
        _32_5_ADD(bytesOf(1, 32), 5, bytesOf(32, 5), ADD),
        _40_5_ADD(bytesOf(1, 40), 5, bytesOf(8, 5), ADD),
        _64_5_ADD(bytesOf(1, 64), 5, bytesOf(64, 5), ADD),
        _40_5_XOR(toBytes(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10,
                          11, 12, 13, 14, 15, 16, 17, 18, 19, 20,
                          21, 22, 23, 24, 25, 26, 27, 28, 29, 30,
                          31, 32, 33, 34, 35, 36, 37, 38, 39), 5,
                  toBytes(48, 40, 16, 24, 16), XOR);

        private final byte[] normal;
        private final int length;
        private final byte[] expected;
        private final Bytes.Operation op;

        CompactCase(byte[] normal, int length, byte[] expected, Bytes.Operation op) {
            this.normal = normal;
            this.length = length;
            this.expected = expected;
            this.op = op;
        }
    }
}
