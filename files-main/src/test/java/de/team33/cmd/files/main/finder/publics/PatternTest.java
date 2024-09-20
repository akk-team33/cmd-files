package de.team33.cmd.files.main.finder.publics;

import de.team33.cmd.files.main.finder.Pattern;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PatternTest {

    @Test
    final void parse_WC() {
        final Pattern pattern = Pattern.parse("myImage.jpg");
        assertTrue(pattern.matcher().test("myImage.jpg"));
        assertFalse(pattern.matcher().test("yourImage.jpg"));
    }

    @Test
    final void parse_RX() {
        final Pattern pattern = Pattern.parse("rx/cs:myImage\\.(jpg|jpe|jpeg)");
        assertTrue(pattern.matcher().test("myImage.jpg"));
        assertFalse(pattern.matcher().test("MyImage.jpg"));
    }
}
