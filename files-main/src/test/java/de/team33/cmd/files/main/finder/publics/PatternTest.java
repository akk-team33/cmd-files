package de.team33.cmd.files.main.finder.publics;

import de.team33.cmd.files.main.finder.Pattern;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PatternTest {

    @Test
    final void parse_WC1() {
        final Pattern pattern = Pattern.parse("my*.jpg");
        assertTrue(pattern.matcher().test("myImage.jpg"));
        assertFalse(pattern.matcher().test("yourImage.jpg"));
    }

    @Test
    final void parse_WC2() {
        final Pattern pattern = Pattern.parse("*image.*");
        assertTrue(pattern.matcher().test("myImage.jpg"));
        assertTrue(pattern.matcher().test("yourImage.jpg"));
        assertFalse(pattern.matcher().test("myImagine.jpg"));
    }

    @Test
    final void parse_WC3() {
        final Pattern pattern = Pattern.parse("??imag*.???");
        assertTrue(pattern.matcher().test("myImage.jpg"));
        assertFalse(pattern.matcher().test("yourImage.jpg"));
        assertTrue(pattern.matcher().test("myImagine.jpg"));
        assertFalse(pattern.matcher().test("myImagine.jpeg"));
    }

    @Test
    final void parse_RX() {
        final Pattern pattern = Pattern.parse("rx/cs:myImage\\.(jpg|jpe|jpeg)");
        assertTrue(pattern.matcher().test("myImage.jpg"));
        assertFalse(pattern.matcher().test("MyImage.jpg"));
    }
}
