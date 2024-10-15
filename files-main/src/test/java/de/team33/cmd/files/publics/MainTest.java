package de.team33.cmd.files.publics;

import de.team33.cmd.files.Main;
import de.team33.cmd.files.job.Regular;
import de.team33.patterns.io.deimos.TextIO;
import de.team33.testing.stdio.ersa.Redirected;
import org.junit.jupiter.api.Test;

import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MainTest {

    private static final String CMD_NAME = MainTest.class.getSimpleName();
    private static final String NEWLINE = String.format("%n");

    @Test
    final void main_noArgs() throws Exception {
        final String expected = String.format("%s%n%n",
                                              TextIO.read(MainTest.class, "MainTest-main_noArgs.txt"));

        final String result = Redirected.outputOf(Main::main);
        // System.out.println(result);

        assertEquals(expected, result);
    }

    @Test
    final void main_oneArg() throws Exception {
        final String expected = TextIO.read(MainTest.class, "MainTest-main_oneArg.txt")
                                      .formatted(Regular.excerpts());

        final String result = Redirected.outputOf(() -> Main.main(CMD_NAME));
        // System.out.println(result);

        assertEquals(expected, result);
    }

    @Test
    final void main_about() throws Exception {
        final String expected = TextIO.read(MainTest.class, "MainTest-main_about.txt")
                                      .formatted();

        final String result = Redirected.outputOf(() -> Main.main(CMD_NAME, "about"))
                                        .lines()
                                        .map(line -> line.startsWith("| Build Timestamp:")
                                                     ? "| Build Timestamp: N/A"
                                                     : line)
                                        .collect(Collectors.joining(NEWLINE));
        // System.out.println(result);

        assertEquals(expected, result);
    }

    @Test
    final void main_keep() throws Exception {
        final String expected = TextIO.read(MainTest.class, "MainTest-main_keep.txt")
                                      .formatted();

        final String result = Redirected.outputOf(() -> Main.main(CMD_NAME, "keep"));
        // System.out.println(result);

        assertEquals(expected, result);
    }

    @Test
    final void main_list() throws Exception {
        final String expected = TextIO.read(MainTest.class, "MainTest-main_list.txt")
                                      .formatted();

        final String result = Redirected.outputOf(() -> Main.main(CMD_NAME, "list"));
        // System.out.println(result);

        assertEquals(expected, result);
    }

    @Test
    final void main_find() throws Exception {
        final String expected = TextIO.read(MainTest.class, "MainTest-main_find.txt")
                                      .formatted();

        final String result = Redirected.outputOf(() -> Main.main(CMD_NAME, "find"));
        // System.out.println(result);

        assertEquals(expected, result);
    }

    @Test
    final void main_findir() throws Exception {
        final String expected = TextIO.read(MainTest.class, "MainTest-main_findir.txt")
                                      .formatted();

        final String result = Redirected.outputOf(() -> Main.main(CMD_NAME, "findir"));
        // System.out.println(result);

        assertEquals(expected, result);
    }

    @Test
    final void main_copy() throws Exception {
        final String expected = TextIO.read(MainTest.class, "MainTest-main_copy.txt")
                                      .formatted();

        final String result = Redirected.outputOf(() -> Main.main(CMD_NAME, "copy"));
        // System.out.println(result);

        assertEquals(expected, result);
    }

    @Test
    final void main_cmp() throws Exception {
        final String expected = TextIO.read(MainTest.class, "MainTest-main_cmp.txt")
                                      .formatted();

        final String result = Redirected.outputOf(() -> Main.main(CMD_NAME, "cmp"));
        // System.out.println(result);

        assertEquals(expected, result);
    }

    @Test
    final void main_clean() throws Exception {
        final String expected = TextIO.read(MainTest.class, "MainTest-main_clean.txt")
                                      .formatted();

        final String result = Redirected.outputOf(() -> Main.main(CMD_NAME, "clean"));
        // System.out.println(result);

        assertEquals(expected, result);
    }

    @Test
    final void main_delete() throws Exception {
        final String expected = TextIO.read(MainTest.class, "MainTest-main_delete.txt")
                                      .formatted();

        final String result = Redirected.outputOf(() -> Main.main(CMD_NAME, "delete"));
        // System.out.println(result);

        assertEquals(expected, result);
    }

    @Test
    final void main_dcopy() throws Exception {
        final String expected = TextIO.read(MainTest.class, "MainTest-main_dcopy.txt")
                                      .formatted();

        final String result = Redirected.outputOf(() -> Main.main(CMD_NAME, "dcopy"));
        // System.out.println(result);

        assertEquals(expected, result);
    }

    @Test
    final void main_move() throws Exception {
        final String expected = TextIO.read(MainTest.class, "MainTest-main_move.txt")
                                      .formatted();

        final String result = Redirected.outputOf(() -> Main.main(CMD_NAME, "move"));
        // System.out.println(result);

        assertEquals(expected, result);
    }

    @Test
    final void main_dedupe() throws Exception {
        final String expected = TextIO.read(MainTest.class, "MainTest-main_dedupe.txt")
                                      .formatted();

        final String result = Redirected.outputOf(() -> Main.main(CMD_NAME, "dedupe"));
        // System.out.println(result);

        assertEquals(expected, result);
    }

    @Test
    final void main_index() throws Exception {
        final String expected = TextIO.read(MainTest.class, "MainTest-main_index.txt")
                                      .formatted();

        final String result = Redirected.outputOf(() -> Main.main(CMD_NAME, "index"));
        // System.out.println(result);

        assertEquals(expected, result);
    }
}
