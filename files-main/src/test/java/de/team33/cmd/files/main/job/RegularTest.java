package de.team33.cmd.files.main.job;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

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
