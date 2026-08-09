package de.team33.cmd.files.job;

import de.team33.cmd.files.common.RequestException;
import de.team33.cmd.files.testing.Buffer;
import de.team33.cmd.files.testing.ModifyingTestBase;
import de.team33.patterns.io.thalassa.TextIO;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ListerTest extends ModifyingTestBase {

    ListerTest() {
        super(ABSOLUTE, InitMode.FILL_LEFT_ONLY);
    }

    @Test
    final void run_ALL_DEEP() throws RequestException {
        final Buffer buffer = new Buffer();

        Lister.job(Context.of(buffer, "files", "find", leftPath().resolve("**").toString()))
              .run();

        final String result = buffer.toString()
                                    .replace(leftPath().toString(), "[PATH]");
        assertEquals(TextIO.read(ListerTest.class, "ListerTest-run_ALL_DEEP.txt"), result);
    }

    @Test
    final void run_ALL_FLAT() throws RequestException {
        final Buffer buffer = new Buffer();

        Lister.job(Context.of(buffer, "files", "find", leftPath().toString()))
              .run();

        final String result = buffer.toString()
                                    .replace(leftPath().toString(), "[PATH]");
        assertEquals(TextIO.read(ListerTest.class, "ListerTest-run_ALL_FLAT.txt"), result);
    }

    @Test
    final void run_order_by_date() throws RequestException {
        final Buffer buffer = new Buffer();
        final String expected = TextIO.read(ListerTest.class, "ListerTest-run_order_by_date.txt");

        Lister.job(Context.of(buffer, "files", "find", leftPath().resolve("**").toString(), "o:d:d"))
              .run();

        final String result = buffer.toString()
                                    .replace(leftPath().toString(), "[PATH]");
        assertEquals(expected, result);
    }

    @Test
    final void run_filter_by_type() throws RequestException {
        final Buffer buffer = new Buffer();
        final String expected = TextIO.read(ListerTest.class, "ListerTest-run_filter_by_type.txt");

        Lister.job(Context.of(buffer, "files", "find", leftPath().resolve("**").toString(), "t:f", "o:s"))
              .run();

        final String result = buffer.toString()
                                    .replace(leftPath().toString(), "[PATH]");
        assertEquals(expected, result);
    }

    @Test
    final void run_rxALL() throws RequestException {
        final Buffer buffer = new Buffer();
        final String expected = TextIO.read(ListerTest.class, "ListerTest-run_ALL_DEEP.txt");

        Lister.job(Context.of(buffer, "files", "find", leftPath().resolve("**").toString(), "n:rx:.*"))
              .run();

        final String result = buffer.toString()
                                    .replace(leftPath().toString(), "[PATH]");
        assertEquals(expected, result);
    }

    @Test
    final void run_wcALL() throws RequestException {
        final Buffer buffer = new Buffer();
        final String expected = TextIO.read(ListerTest.class, "ListerTest-run_ALL_DEEP.txt");

        Lister.job(Context.of(buffer, "files", "find", leftPath().resolve("**").toString(), "n:*"))
              .run();

        final String result = buffer.toString()
                                    .replace(leftPath().toString(), "[PATH]");
        assertEquals(expected, result);
    }

    @Test
    final void run_java() throws RequestException {
        final Buffer buffer = new Buffer();
        final String expected = TextIO.read(ListerTest.class, "ListerTest-run_java.txt");

        final String queryString = leftPath().resolve("**").resolve("*.java").toString();
        Lister.job(Context.of(buffer, "files", "find", queryString))
              .run();

        final String result = buffer.toString()
                                    .replace(leftPath().toString(), "[PATH]");
        assertEquals(expected, result);
    }

    @Test
    final void run_56() throws RequestException {
        final Buffer buffer = new Buffer();
        final String expected = TextIO.read(ListerTest.class, "ListerTest-run_56.txt");

        Lister.job(Context.of(buffer, "files", "find", leftPath().resolve("**").toString(), "n:rx:.{5,6}"))
              .run();

        final String result = buffer.toString()
                                    .replace(leftPath().toString(), "[PATH]");
        assertEquals(expected, result);
    }
}
