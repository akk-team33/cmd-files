package de.team33.cmd.files.main.finder.publics;

import de.team33.cmd.files.main.finder.Pattern;
import de.team33.patterns.io.alpha.FileEntry;
import de.team33.patterns.io.alpha.FilePolicy;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class PatternTest {

    private FileEntry entryOf(final String name) {
        return FileEntry.of(Path.of(name), FilePolicy.DISTINCT_SYMLINKS);
    }

    @Test
    final void parse_WC1() {
        final Pattern pattern = Pattern.parse("my*.jpg");
        assertTrue(pattern.matcher().test(entryOf("myImage.jpg")));
        assertFalse(pattern.matcher().test(entryOf("yourImage.jpg")));
    }

    @Test
    final void parse_WC2() {
        final Pattern pattern = Pattern.parse("*image.*");
        assertTrue(pattern.matcher().test(entryOf("myImage.jpg")));
        assertTrue(pattern.matcher().test(entryOf("yourImage.jpg")));
        assertFalse(pattern.matcher().test(entryOf("myImagine.jpg")));
    }

    @Test
    final void parse_WC3() {
        final Pattern pattern = Pattern.parse("??imag*.???");
        assertTrue(pattern.matcher().test(entryOf("myImage.jpg")));
        assertFalse(pattern.matcher().test(entryOf("yourImage.jpg")));
        assertTrue(pattern.matcher().test(entryOf("myImagine.jpg")));
        assertFalse(pattern.matcher().test(entryOf("myImagine.jpeg")));
    }

    @Test
    final void parse_RX() {
        final Pattern pattern = Pattern.parse("rx/cs:myImage\\.(jpg|jpe|jpeg)");
        assertTrue(pattern.matcher().test(entryOf("myImage.jpg")));
        assertFalse(pattern.matcher().test(entryOf("MyImage.jpg")));
    }
}
