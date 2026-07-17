package de.team33.patterns.io.gamma.publics;

import de.team33.patterns.io.gamma.Input;
import de.team33.patterns.io.gamma.Output;
import de.team33.patterns.io.gamma.Resource;
import de.team33.testing.Supply;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ResourceTest extends Supply {

    private static final Path PATH = Path.of("target", "testing", ResourceTest.class.getSimpleName());
    private static final String CHARACTERS = "abcdefghijklmnopqrstuvwxyz";
    private static final Resource CLASSPATH_RESOURCE = Resource.by(ResourceTest.class, "ResourceTest.txt");

    private final Resource resource;
    private final Path path;

    ResourceTest() throws IOException {
        Files.createDirectories(PATH);
        this.path = PATH.resolve("%s.txt".formatted(anyString(8, CHARACTERS)));
        this.resource = Resource.by(path);
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
        final Input<String> input = CLASSPATH_RESOURCE.reading(ResourceTest::readString);

        final String result = input.read();
        assertEquals(expected, result);
    }

    @Test
    final void by_classpath_write() {
        final Output<String> output = CLASSPATH_RESOURCE.writing(ResourceTest::writeString);
        assertThrows(UnsupportedOperationException.class, () -> output.write(anyString()));
    }

    @Test
    final void input_read() throws IOException {
        final byte[] original = anyString().getBytes(StandardCharsets.UTF_8);
        final Resource resource = Resource.readOnly(() -> new ByteArrayInputStream(original));

        final Input<byte[]> input = resource.input(InputStream::readAllBytes);
        final byte[] result = input.read();
        assertArrayEquals(original, result);
    }

    @Test
    final void output_write() throws IOException {
        final byte[] original = anyString().getBytes(StandardCharsets.UTF_8);

        final Output<byte[]> output = resource.output(OutputStream::write);
        output.write(original);

        assertArrayEquals(original, resource.input(InputStream::readAllBytes).read());
    }

    @Test
    final void output_writeOnly() throws IOException {
        final byte[] original = anyString().getBytes(StandardCharsets.UTF_8);
        final Resource woResource = Resource.writeOnly(() -> Files.newOutputStream(path));

        final Input<String> reading = woResource.reading(ResourceTest::readString);
        assertThrows(UnsupportedOperationException.class, reading::read);

        final Output<byte[]> output = woResource.output(OutputStream::write);
        output.write(original);
        assertArrayEquals(original, resource.input(InputStream::readAllBytes).read());
    }

    @Test
    final void writing_write() throws IOException {
        final String original = anyString();

        resource.writing(ResourceTest::writeString)
                .write(original);

        assertEquals(original, resource.reading(ResourceTest::readString).read());
    }

    @Test
    final void reading_read() throws IOException {
        final String original = anyString();
        final byte[] bytes = original.getBytes(StandardCharsets.UTF_8);
        final Resource resource = Resource.readOnly(() -> new ByteArrayInputStream(bytes));

        final Input<String> input = resource.reading(ResourceTest::readString);
        final String result = input.read();
        assertEquals(original, result);
    }
}