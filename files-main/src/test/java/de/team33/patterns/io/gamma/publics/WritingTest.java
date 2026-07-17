package de.team33.patterns.io.gamma.publics;

import de.team33.patterns.io.gamma.Reading;
import de.team33.patterns.io.gamma.Writing;
import de.team33.testing.Supply;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WritingTest extends Supply {

    private static final Path PATH = Path.of("target", "testing", WritingTest.class.getSimpleName());
    private static final String CHARACTERS = "abcdefghijklmnopqrstuvwxyz";

    private final Writing<String> writing;
    private final Reading<String> reading;
    private final Path path;

    WritingTest() throws IOException {
        Files.createDirectories(PATH);
        this.path = PATH.resolve("%s.txt".formatted(anyString(8, CHARACTERS)));
        this.writing = () -> Files.newOutputStream(path);
        this.reading = () -> Files.newInputStream(path);
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

    private static String inputString(final InputStream in) throws IOException {
        return new String(in.readAllBytes(), StandardCharsets.UTF_8);
    }

    private static void outputString(final OutputStream out, String text) throws IOException {
        out.write(text.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    final void output_write() throws IOException {
        final String original = anyString();

        writing.output(WritingTest::outputString)
               .write(original);

        assertEquals(original, reading.input(WritingTest::inputString).read());
    }

    @Test
    final void writing_write() throws IOException {
        final String original = anyString();

        writing.output(WritingTest::writeString, StandardCharsets.UTF_8)
               .write(original);

        assertEquals(original, reading.input(WritingTest::readString, StandardCharsets.UTF_8).read());
    }
}