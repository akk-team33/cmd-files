package de.team33.cmd.files.job;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class RegularTest {

    @Test
    void excerpt() {
        final String result = Regular.excerpts();
        // System.out.println(result);
        for (final Regular item : Regular.values()) {
            assertTrue(result.contains(item.name()));
        }
    }
}
