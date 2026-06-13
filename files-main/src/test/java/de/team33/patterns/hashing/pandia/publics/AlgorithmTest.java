package de.team33.patterns.hashing.pandia.publics;

import de.team33.patterns.hashing.pandia.Algorithm;
import de.team33.patterns.hashing.pandia.AlgorithmCase;
import de.team33.patterns.hashing.pandia.Hash;
import de.team33.patterns.hashing.pandia.testing.Supply;
import de.team33.tools.io.FileHashing;
import de.team33.tools.io.StrictHashing;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

class AlgorithmTest extends Supply {

    private static final String DIGITS = "0123456789abcdefghijklmnopqrstuvwxyz";

    @ParameterizedTest
    @EnumSource
    final void hash_string(final AlgorithmCase given) {
        final Hash result = given.algorithm.hash(given.string);
        assertEquals(given.expected, result);
    }

    @ParameterizedTest
    @EnumSource
    final void hash_file(final AlgorithmCase given) {
        final Hash result = given.algorithm.hash(given.path);
        assertEquals(given.expected, result);
    }

    @Disabled
    @ParameterizedTest
    @EnumSource
    final void hash_file_alt(final AlgorithmCase given) {
        final FileHashing altHashing = StrictHashing.valueOf(given.algorithm.name());
        final String expected = altHashing.hash(given.path);
        final Hash result = given.algorithm.hash(given.path);
        assertEquals(expected, result.toHexString());
    }

    @Test
    final void hash_file_missing() {
        try {
            final Hash result = Algorithm.MD5.hash(AlgorithmCase.MD5_EMPTY.path.resolve("missing"));
            fail("expected to fail - but was " + result);
        } catch (final IllegalStateException e) {
            // as expected!
            // e.printStackTrace();
        }
    }

    @ParameterizedTest
    @EnumSource
    final void hash_bytes(final AlgorithmCase given) {
        final Hash result = given.algorithm.hash(given.bytes);
        assertEquals(given.expected, result);
    }

    @ParameterizedTest
    @EnumSource
    final void hash_bytes_alt(final AlgorithmCase given) {
        final Hash result = given.algorithm.hash(given.bytes);
        assertEquals(given.expected.toBigInteger().toString(36),
                     result.toString(DIGITS));
    }

    @ParameterizedTest
    @EnumSource
    final void byteSize(final AlgorithmCase given) {
        final Hash result = given.algorithm.hash(anyString(1 + anyInt(8192)));
        assertEquals(given.algorithm.byteSize(), result.bytes().length);
    }

    @ParameterizedTest
    @EnumSource
    final void parseBigInteger(final AlgorithmCase given) {
        final Hash expected = given.algorithm.hash(anyString(1 + anyInt(8192)));
        final Hash result = given.algorithm.parse(expected.toBigInteger());
        assertEquals(expected, result);
    }

    @ParameterizedTest
    @EnumSource
    final void parseString(final AlgorithmCase given) {
        final Hash expected = given.algorithm.hash(anyString(1 + anyInt(8192)));
        final Hash result = given.algorithm.parse(expected.toString(DIGITS), DIGITS);
        assertEquals(expected, result);
    }

    @ParameterizedTest
    @EnumSource
    final void parseString_illegal(final AlgorithmCase given) {
        final Hash origin = given.algorithm.hash(given.path);
        try {
            final Hash result = given.algorithm.parse(origin.toString(DIGITS), DIGITS.toUpperCase());
            fail("expected to fail - but was " + result);
        } catch (final IllegalArgumentException e) {
            // as expected
            // e.printStackTrace();
        }
    }
}