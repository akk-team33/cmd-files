package de.team33.cmd.template.main.job;

import org.junit.jupiter.api.Test;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class RegularTest {

    @Test
    void excerpt() {
        final String result = Regular.excerpt();
        // System.out.println(result);
        for (final Regular item : Regular.values()) {
            assertTrue(result.contains(item.name()));
        }
    }
}
