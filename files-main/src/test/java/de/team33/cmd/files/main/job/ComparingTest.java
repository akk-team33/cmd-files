package de.team33.cmd.files.main.job;

import de.team33.cmd.files.main.common.Output;
import de.team33.cmd.files.main.common.RequestException;
import de.team33.cmd.files.main.testing.ModifyingTestBase;
import de.team33.patterns.io.deimos.TextIO;
import de.team33.testing.stdio.ersa.Redirected;
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
        final Runnable job = Comparing.job(Output.SYSTEM,
                                           Arrays.asList("files", "cmp",
                                                         leftPath().toString(),
                                                         rightPath().toString()));
        final String result = Redirected.outputOf(job::run);
        assertEquals(TextIO.read(ComparingTest.class, "ComparingTest-compare.txt"), result);
    }
}
