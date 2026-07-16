package de.team33.patterns.io.gamma.publics;

import de.team33.patterns.io.gamma.Resource;
import de.team33.testing.Supply;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ResourceTest extends Supply {

    private static final Path PATH = Path.of("target", "testing", ResourceTest.class.getSimpleName());
    private static final String CHARACTERS = "abcdefghijklmnopqrstuvwxyz";

    private final Path path;
    private final Resource resource;

    ResourceTest() throws IOException {
        Files.createDirectories(PATH);
        path = PATH.resolve("%s.txt".formatted(anyString(8, CHARACTERS)));
        resource = new Resource(() -> Files.newInputStream(path),
                                () -> Files.newOutputStream(path));
    }

    private static String inputString(final InputStream in) throws IOException {
        return new String(in.readAllBytes(), StandardCharsets.UTF_8);
    }

    private static String readString(final BufferedReader in) throws IOException {
        try (final StringWriter out = new StringWriter()) {
            in.transferTo(out);
            return out.toString();
        }
    }

    private static void outputString(final OutputStream out, String string) throws IOException {
        out.write(string.getBytes(StandardCharsets.UTF_8));
    }

    private static void writeString(final BufferedWriter out, String string) throws IOException {
        out.write(string);
    }

    @Test
    void roundTrip_bytes() throws IOException {
        final String origin = anyString();
        resource.output(ResourceTest::outputString).write(origin);

        final String result = resource.input(ResourceTest::inputString).read();
        assertEquals(origin, result);
    }

    @Test
    void roundTrip_chars() throws IOException {
        final String origin = anyString();
        resource.writing(ResourceTest::writeString)
                .write(origin);

        final String result = resource.reading(ResourceTest::readString)
                                      .read();
        assertEquals(origin, result);
    }
}