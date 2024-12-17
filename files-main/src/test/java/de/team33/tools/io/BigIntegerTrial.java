package de.team33.tools.io;

import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BigIntegerTrial {

    private static final byte[] ONE = {0, 0, 0, 1};
    private static final byte[] PLUS_128 = {0, -128};
    private static final byte[] MINUS_128 = {-128};

    @Test
    final void testOne() {
        assertEquals(BigInteger.ONE, new BigInteger(ONE));
    }

    @Test
    final void test128() {
        assertEquals(BigInteger.valueOf(128), new BigInteger(PLUS_128));
    }

    @Test
    final void testMinus128() {
        assertEquals(BigInteger.valueOf(-128), new BigInteger(MINUS_128));
    }
}
