package de.team33.patterns.io.gamma.publics;

import de.team33.patterns.io.gamma.Resource;
import de.team33.patterns.io.gamma.TextResource;
import de.team33.testing.Supply;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TextResourceTest extends Supply {

    private static final Path PATH = Path.of("target", "testing", TextResourceTest.class.getSimpleName());
    private static final String CHARACTERS = "abcdefghijklmnopqrstuvwxyz";
    private static final TextResource CLASSPATH_RESOURCE = TextResource.by(TextResourceTest.class, "ResourceTest.txt");

    private final TextResource resource;

    TextResourceTest() throws IOException {
        Files.createDirectories(PATH);
        final Path path = PATH.resolve("%s.txt".formatted(anyString(8, CHARACTERS)));
        this.resource = TextResource.by(path);
    }

    private static String readString(final BufferedReader in) throws IOException {
        try (final StringWriter out = new StringWriter()) {
            in.transferTo(out);
            return out.toString();
        }
    }

    private static void writeString(final BufferedWriter out, String string) throws IOException {
        out.write(string);
    }

    @Test
    final void by_classpath_read() throws IOException {
        final String expected = "p1=v1\n" +
                                "p2=v2\n" +
                                "p3=v3\n";
        final String result = CLASSPATH_RESOURCE.read();
        assertEquals(expected, result);
    }

    @Test
    final void by_classpath_write() {
        assertThrows(UnsupportedOperationException.class, () -> CLASSPATH_RESOURCE.write(anyString()));
    }

    @Test
    final void read() throws IOException {
        final String original = anyString();
        final byte[] bytes = original.getBytes(StandardCharsets.UTF_8);
        final TextResource resource = TextResource.by(Resource.readOnly(() -> new ByteArrayInputStream(bytes)));

        final String result = resource.read();
        assertEquals(original, result);
    }

    @Test
    final void write() throws IOException {
        final String original = anyString();
        resource.write(original);
        assertEquals(original, resource.read());
    }
}