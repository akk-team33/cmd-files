package de.team33.patterns.io.gamma.publics;

import de.team33.patterns.io.gamma.Input;
import de.team33.patterns.io.gamma.TextInput;
import de.team33.testing.Supply;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TextInputTest extends Supply {

    private static final Path PATH = Path.of("target", "testing", TextInputTest.class.getSimpleName());
    private static final String CHARACTERS = "abcdefghijklmnopqrstuvwxyz";
    private static final Input<String> CLASSPATH_INPUT = TextInput.by(TextInputTest.class, "ResourceTest.txt");

    private final TextInput textInput;
    private final Path path;

    TextInputTest() throws IOException {
        Files.createDirectories(PATH);
        this.path = PATH.resolve("%s.txt".formatted(anyString(8, CHARACTERS)));
        this.textInput = TextInput.by(path);
    }

    @Test
    final void by_classpath_read() throws IOException {
        final String expected = "p1=v1\n" +
                                "p2=v2\n" +
                                "p3=v3\n";
        final String result = CLASSPATH_INPUT.read();
        assertEquals(expected, result);
    }

    @Test
    final void read() throws IOException {
        final String original = anyString();
        Files.writeString(path, original);

        final String result = textInput.read();
        assertEquals(original, result);
    }
}