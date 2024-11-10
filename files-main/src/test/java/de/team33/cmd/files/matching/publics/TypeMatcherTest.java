package de.team33.cmd.files.matching.publics;

import de.team33.cmd.files.matching.TypeMatcher;
import de.team33.patterns.io.phobos.FileEntry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class TypeMatcherTest {

    private FileEntry entry(final String path) {
        return FileEntry.of(Path.of(path));
    }

    @Test
    final void parse_extensions() {
        final TypeMatcher matcher = TypeMatcher.parse("a:jpg,JPE,jPEg");
        assertTrue(matcher.matches(entry("myImage.Jpg")));
        assertTrue(matcher.matches(entry("myImage.jPe")));
        assertTrue(matcher.matches(entry("myImage.jpEg")));
        assertFalse(matcher.matches(entry("myImage")));
        assertFalse(matcher.matches(entry("myImage.tif")));
        assertFalse(matcher.matches(entry("myImage.tiff")));
    }

    @ParameterizedTest
    @ValueSource(strings = {"::"})
    final void parse_fail(final String pattern) {
        try {
            final TypeMatcher matcher = TypeMatcher.parse(pattern);
            fail("expected to fail - but was " + matcher);
        } catch (final IllegalArgumentException e) {
            // as expected -> OK!
            // e.printStackTrace();
        }
    }
}
