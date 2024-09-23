package de.team33.cmd.files.main.publics;

import de.team33.cmd.files.main.job.Regular;
import de.team33.cmd.files.main.Main;
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
        final String expected = String.format(TextIO.read(MainTest.class, "MainTest-main_oneArg.txt"),
                                              Regular.excerpts());

        final String result = Redirected.outputOf(() -> Main.main(CMD_NAME));
        // System.out.println(result);

        assertEquals(expected, result);
    }

    @Test
    final void main_about() throws Exception {
        final String expected = String.format(TextIO.read(MainTest.class, "MainTest-main_about.txt"));

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
        final String expected = String.format(TextIO.read(MainTest.class, "MainTest-main_keep.txt"));

        final String result = Redirected.outputOf(() -> Main.main(CMD_NAME, "keep"));
        // System.out.println(result);

        assertEquals(expected, result);
    }

    @Test
    final void main_list() throws Exception {
        final String expected = String.format(TextIO.read(MainTest.class, "MainTest-main_list.txt"));

        final String result = Redirected.outputOf(() -> Main.main(CMD_NAME, "list"));
        // System.out.println(result);

        assertEquals(expected, result);
    }

    @Test
    final void main_find() throws Exception {
        final String expected = String.format(TextIO.read(MainTest.class, "MainTest-main_find.txt"));

        final String result = Redirected.outputOf(() -> Main.main(CMD_NAME, "find"));
        // System.out.println(result);

        assertEquals(expected, result);
    }

    @Test
    final void main_findir() throws Exception {
        final String expected = String.format(TextIO.read(MainTest.class, "MainTest-main_findir.txt"));

        final String result = Redirected.outputOf(() -> Main.main(CMD_NAME, "findir"));
        System.out.println(result);

        assertEquals(expected, result);
    }
}
