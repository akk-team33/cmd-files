package de.team33.cmd.files.job;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class CommandTest {

    @Test
    void excerpt() {
        final String result = Command.excerpts();
        // System.out.println(result);
        for (final Command item : Command.values()) {
            assertTrue(result.contains(item.name()));
        }
    }
}
