package de.team33.cmd.files.job;

import de.team33.cmd.files.common.RequestException;
import de.team33.cmd.files.testing.Buffer;
import de.team33.cmd.files.testing.ModifyingTestBase;
import de.team33.patterns.io.deimos.TextIO;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FinderTest extends ModifyingTestBase {

    FinderTest() {
        super(ABSOLUTE, InitMode.FILL_LEFT_ONLY);
    }

    @Test
    final void run_ALL() throws RequestException {
        final Buffer buffer = new Buffer();

        Finder.job(buffer, List.of("files", "find", leftPath().toString()))
              .run();

        final String result = buffer.toString()
                                    .replace(leftPath().toString(), "[PATH]");
        assertEquals(TextIO.read(FinderTest.class, "FinderTest-run_ALL.txt"), result);
    }

    @Test
    final void run_rxALL() throws RequestException {
        final Buffer buffer = new Buffer();

        Finder.job(buffer, List.of("files", "find", leftPath().toString(), "n:rx:.*"))
              .run();

        final String result = buffer.toString()
                                    .replace(leftPath().toString(), "[PATH]");
        assertEquals(TextIO.read(FinderTest.class, "FinderTest-run_ALL.txt"), result);
    }

    @Test
    final void run_wcALL() throws RequestException {
        final Buffer buffer = new Buffer();

        Finder.job(buffer, List.of("files", "find", leftPath().toString(), "n:*"))
              .run();

        final String result = buffer.toString()
                                    .replace(leftPath().toString(), "[PATH]");
        assertEquals(TextIO.read(FinderTest.class, "FinderTest-run_ALL.txt"), result);
    }

    @Test
    final void run_java() throws RequestException {
        final Buffer buffer = new Buffer();

        Finder.job(buffer, List.of("files", "find", leftPath().toString(), "n:*.java"))
              .run();

        final String result = buffer.toString()
                                    .replace(leftPath().toString(), "[PATH]");
        assertEquals(TextIO.read(FinderTest.class, "FinderTest-run_java.txt"), result);
    }

    @Test
    final void run_56() throws RequestException {
        final Buffer buffer = new Buffer();

        Finder.job(buffer, List.of("files", "find", leftPath().toString(), "n:rx:.{5,6}"))
              .run();

        final String result = buffer.toString()
                                    .replace(leftPath().toString(), "[PATH]");
        assertEquals(TextIO.read(FinderTest.class, "FinderTest-run_56.txt"), result);
    }
}
