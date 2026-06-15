package de.team33.cmd.files.job;

import de.team33.cmd.files.common.RequestException;
import de.team33.cmd.files.testing.Buffer;
import de.team33.cmd.files.testing.ModifyingTestBase;
import de.team33.patterns.io.deimos.TextIO;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ListerTest extends ModifyingTestBase {

    ListerTest() {
        super(ABSOLUTE, InitMode.FILL_LEFT_ONLY);
    }

    @Test
    final void run_ALL_DEEP() throws RequestException {
        final Buffer buffer = new Buffer();

        Lister.job(buffer, List.of("files", "find", leftPath().toString(), "d:deep"))
              .run();

        final String result = buffer.toString()
                                    .replace(leftPath().toString(), "[PATH]");
        assertEquals(TextIO.read(ListerTest.class, "ListerTest-run_ALL_DEEP.txt"), result);
    }

    @Test
    final void run_ALL_FLAT() throws RequestException {
        final Buffer buffer = new Buffer();

        Lister.job(buffer, List.of("files", "find", leftPath().toString(), "d:flat"))
              .run();

        final String result = buffer.toString()
                                    .replace(leftPath().toString(), "[PATH]");
        assertEquals(TextIO.read(ListerTest.class, "ListerTest-run_ALL_FLAT.txt"), result);
    }

    @Test
    final void run_order_by_date() throws RequestException {
        final Buffer buffer = new Buffer();

        Lister.job(buffer, List.of("files", "find", leftPath().toString(), "o:d:d"))
              .run();

        final String result = buffer.toString()
                                    .replace(leftPath().toString(), "[PATH]");
        assertEquals(TextIO.read(ListerTest.class, "ListerTest-run_order_by_date.txt"), result);
    }

    @Test
    final void run_filter_by_type() throws RequestException {
        final Buffer buffer = new Buffer();

        Lister.job(buffer, List.of("files", "find", leftPath().toString(), "t:f", "o:s"))
              .run();

        final String result = buffer.toString()
                                    .replace(leftPath().toString(), "[PATH]");
        assertEquals(TextIO.read(ListerTest.class, "ListerTest-run_filter_by_type.txt"), result);
    }

    @Test
    final void run_rxALL() throws RequestException {
        final Buffer buffer = new Buffer();

        Lister.job(buffer, List.of("files", "find", leftPath().toString(), "n:rx:.*"))
              .run();

        final String result = buffer.toString()
                                    .replace(leftPath().toString(), "[PATH]");
        assertEquals(TextIO.read(ListerTest.class, "ListerTest-run_ALL_DEEP.txt"), result);
    }

    @Test
    final void run_wcALL() throws RequestException {
        final Buffer buffer = new Buffer();

        Lister.job(buffer, List.of("files", "find", leftPath().toString(), "n:*"))
              .run();

        final String result = buffer.toString()
                                    .replace(leftPath().toString(), "[PATH]");
        assertEquals(TextIO.read(ListerTest.class, "ListerTest-run_ALL_DEEP.txt"), result);
    }

    @Test
    final void run_java() throws RequestException {
        final Buffer buffer = new Buffer();

        Lister.job(buffer, List.of("files", "find", leftPath().toString(), "n:*.java"))
              .run();

        final String result = buffer.toString()
                                    .replace(leftPath().toString(), "[PATH]");
        assertEquals(TextIO.read(ListerTest.class, "ListerTest-run_java.txt"), result);
    }

    @Test
    final void run_56() throws RequestException {
        final Buffer buffer = new Buffer();

        Lister.job(buffer, List.of("files", "find", leftPath().toString(), "n:rx:.{5,6}"))
              .run();

        final String result = buffer.toString()
                                    .replace(leftPath().toString(), "[PATH]");
        assertEquals(TextIO.read(ListerTest.class, "ListerTest-run_56.txt"), result);
    }
}
