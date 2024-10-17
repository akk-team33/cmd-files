package de.team33.tools.io;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class StreamHashingTest {

    private static final byte[] EMPTY = {};

    @Test
    void hash_empty_md5() throws IOException {
        final byte[] expected = {-44, 29, -116, -39, -113, 0, -78, 4, -23, -128, 9, -104, -20, -8, 66, 126};
        try (InputStream in = new ByteArrayInputStream(EMPTY)) {
            final byte[] result = StreamHashing.MD5.hash(in);
            assertArrayEquals(expected, result);
        }
    }

    @Test
    void hash_empty_sha_1() throws IOException {
        final byte[] expected = {-38, 57, -93, -18, 94, 107, 75, 13, 50, 85,
                -65, -17, -107, 96, 24, -112, -81, -40, 7, 9};
        try (InputStream in = new ByteArrayInputStream(EMPTY)) {
            final byte[] result = StreamHashing.SHA_1.hash(in);
            assertArrayEquals(expected, result);
        }
    }
}
