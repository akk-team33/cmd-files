package de.team33.cmd.files.finding.publics;

import de.team33.cmd.files.finding.NameMatcher;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NameMatcherTest {

    @Test
    final void parse_WC1() {
        final NameMatcher pattern = NameMatcher.parse("my*.jpg");
        assertTrue(pattern.matches("myImage.jpg"));
        assertFalse(pattern.matches("yourImage.jpg"));
    }

    @Test
    final void parse_WC2() {
        final NameMatcher pattern = NameMatcher.parse("*image.*");
        assertTrue(pattern.matches("myImage.jpg"));
        assertTrue(pattern.matches("yourImage.jpg"));
        assertFalse(pattern.matches("myImagine.jpg"));
    }

    @Test
    final void parse_WC3() {
        final NameMatcher pattern = NameMatcher.parse("??imag*.???");
        assertTrue(pattern.matches("myImage.jpg"));
        assertFalse(pattern.matches("yourImage.jpg"));
        assertTrue(pattern.matches("myImagine.jpg"));
        assertFalse(pattern.matches("myImagine.jpeg"));
    }

    @Test
    final void parse_RX() {
        final NameMatcher pattern = NameMatcher.parse("rx/cs:myImage\\.(jpg|jpe|jpeg)");
        assertTrue(pattern.matches("myImage.jpg"));
        assertFalse(pattern.matches("MyImage.jpg"));
    }
}
