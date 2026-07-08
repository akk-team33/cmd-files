package de.team33.cmd.files.job;

import de.team33.cmd.files.common.RequestException;
import de.team33.cmd.files.testing.Buffer;
import de.team33.cmd.files.testing.ModifyingTestBase;
import de.team33.patterns.io.deimos.TextIO;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DirListerTest extends ModifyingTestBase {

    DirListerTest() {
        super(ABSOLUTE, InitMode.FILL_LEFT_ONLY);
    }

    @Test
    final void run_java() throws RequestException, IOException {
        final Buffer buffer = new Buffer();
        final String expected = TextIO.read(DirListerTest.class, "DirListerTest-run_java.txt");
        final String queryString = leftPath().resolve("**").resolve("*.java").toString();

        DirLister.job(buffer, Arrays.asList("files", "lsd", queryString))
                 .run();

        final String result = buffer.toString()
                                    .replace(leftPath().toString(), "[PATH]");
        assertEquals(expected, result);
    }

    @Test
    final void run_56() throws RequestException, IOException {
        final Buffer buffer = new Buffer();
        final String expected = TextIO.read(DirListerTest.class, "DirListerTest-run_56.txt");
        final String queryString = leftPath().resolve("**").toString();

        DirLister.job(buffer, Arrays.asList("files", "lsd", queryString, "n:rx:.{5,6}", "t:a"))
                 .run();

        final String result = buffer.toString()
                                    .replace(leftPath().toString(), "[PATH]");
        assertEquals(expected, result);
    }
}
