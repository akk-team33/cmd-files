package de.team33.cmd.files.job;

import de.team33.cmd.files.common.Output;
import de.team33.cmd.files.common.RequestException;
import de.team33.cmd.files.testing.ModifyingTestBase;
import de.team33.patterns.io.deimos.TextIO;
import de.team33.testing.stdio.ersa.Redirected;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static de.team33.cmd.files.job.Util.condition;
import static org.junit.jupiter.api.Assertions.assertEquals;

class DirFinderTest extends ModifyingTestBase {

    DirFinderTest() {
        super(ABSOLUTE, InitMode.FILL_LEFT_ONLY);
    }

    @Test
    final void run_java() throws RequestException, IOException {
        final Runnable job = DirFinder.job(condition(Output.SYSTEM,
                                                     "files", "findir", "*.java", leftPath().toString()));
        final String result = Redirected.outputOf(job::run)
                                        .replace(leftPath().toString(), "[PATH]");
        assertEquals(TextIO.read(DirFinderTest.class, "DirFinderTest-run_java.txt"), result);
    }

    @Test
    final void run_56() throws RequestException, IOException {
        final Runnable job = DirFinder.job(condition(Output.SYSTEM,
                                                     "files", "findir", "rx:.{5,6}", leftPath().toString()));
        final String result = Redirected.outputOf(job::run)
                                        .replace(leftPath().toString(), "[PATH]");
        assertEquals(TextIO.read(DirFinderTest.class, "DirFinderTest-run_56.txt"), result);
    }
}
