package de.team33.patterns.io.gamma.publics;

import de.team33.patterns.io.gamma.TextIO;
import de.team33.testing.Supply;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TextIOTest extends Supply {

    private static final Path PATH = Path.of("target", "testing", TextIOTest.class.getSimpleName());
    private static final String CHARACTERS = "abcdefghijklmnopqrstuvwxyz";

    private final TextIO textIO;
    private final Path path;

    TextIOTest() throws IOException {
        Files.createDirectories(PATH);
        this.path = PATH.resolve("%s.txt".formatted(anyString(8, CHARACTERS)));
        this.textIO = TextIO.by(path);
    }

    @Test
    final void read() throws IOException {
        final String original = anyString();
        Files.writeString(path, original);

        final String result = textIO.read();
        assertEquals(original, result);
    }

    @Test
    final void write() throws IOException {
        final String original = anyString();
        textIO.write(original);
        assertEquals(original, textIO.read());
    }
}