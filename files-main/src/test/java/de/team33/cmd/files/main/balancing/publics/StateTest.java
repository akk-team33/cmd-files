package de.team33.cmd.files.main.balancing.publics;

import de.team33.cmd.files.main.balancing.Relative;
import de.team33.cmd.files.main.balancing.Relatives;
import de.team33.cmd.files.main.common.RequestException;
import de.team33.cmd.files.main.testing.ModifyingTestBase;
import de.team33.patterns.io.deimos.TextIO;
import org.junit.jupiter.api.Test;

import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StateTest extends ModifyingTestBase {

    StateTest() {
        super(RELATIVE);
    }

    @Test
    final void of() throws RequestException {
        final String result = Relatives.stream(leftPath(), rightPath())
                                       .map(this::report_)
                                       .collect(Collectors.joining())
                                       .trim();
        assertEquals(TextIO.read(StateTest.class, "StateTest-of.txt"), result);
    }

    private String report_(final Relative relative) {
        return String.format("%s - %s%n", relative.path(), relative.state());
    }
}
