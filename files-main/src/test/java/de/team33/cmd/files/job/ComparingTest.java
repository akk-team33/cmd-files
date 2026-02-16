package de.team33.cmd.files.job;

import de.team33.cmd.files.common.RequestException;
import de.team33.cmd.files.testing.Buffer;
import de.team33.cmd.files.testing.ModifyingTestBase;
import de.team33.patterns.io.deimos.TextIO;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ComparingTest extends ModifyingTestBase {

    ComparingTest() {
        super(RELATIVE);
    }

    @Test
    final void compare() throws RequestException, IOException {
        final String expected = TextIO.read(ComparingTest.class, "ComparingTest-compare.txt");
        final Buffer out = new Buffer();
        Comparing.job(out, Arrays.asList("files", "cmp",
                                         leftPath().toString(),
                                         rightPath().toString())).run();
        final String result = out.toString();
        assertEquals(expected, result);
    }
}
