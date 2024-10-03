package de.team33.cmd.files.main.job;

import de.team33.cmd.files.main.common.Output;
import de.team33.cmd.files.main.common.RequestException;
import de.team33.cmd.files.main.testing.ModifyingTestBase;
import de.team33.patterns.io.deimos.TextIO;
import de.team33.testing.io.hydra.FileInfo;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MovingTest extends ModifyingTestBase {

    MovingTest() {
        super(ABSOLUTE, InitMode.FILL_LEFT_ONLY);
    }

    @Test
    final void move_PYMDF() throws RequestException {
        final String expected = String.format(TextIO.read(MovingTest.class, "MovingTest-move_PYMDF.txt"), testID());

        Moving.job(Output.SYSTEM,
                   Arrays.asList("files", "move", "-r", leftPath().toString(), "../@P.moved/@Y/@M/@D/@F"))
              .run();

        assertEquals(expected, FileInfo.of(testPath()).toString());
    }

    @Test
    final void move_RpNX() throws RequestException {
        final String expected = String.format(TextIO.read(MovingTest.class, "MovingTest-move_RpNX.txt"), testID());

        Moving.job(Output.SYSTEM,
                   Arrays.asList("files", "move", "-r", leftPath().toString(), "@R/../@p-@N.@X"))
              .run();

        assertEquals(expected, FileInfo.of(testPath()).toString());
    }

    @Test
    final void move_hms_hash_X() throws RequestException {
        final String expected = String.format(TextIO.read(MovingTest.class, "MovingTest-move_hms_hash_X.txt"),
                                              testID());

        Moving.job(Output.SYSTEM,
                   Arrays.asList("files", "move", "-r", leftPath().toString(), "@h/@m/@s/@#.@X"))
              .run();

        assertEquals(expected, FileInfo.of(testPath()).toString());
    }

    @Test
    final void move_R_hash_time_X() throws RequestException {
        final String expected = String.format(TextIO.read(MovingTest.class, "MovingTest-move_R_hash_time_X.txt"),
                                              testID());

        Moving.job(Output.SYSTEM,
                   Arrays.asList("files", "move", "-r", leftPath().toString(), "@R/@#@!.@X"))
              .run();

        assertEquals(expected, FileInfo.of(testPath()).toString());
    }

    @Test
    final void move_RF() throws RequestException {
        final String expected = String.format(TextIO.read(MovingTest.class, "MovingTest-move_RF.txt"),
                                              testID());

        Moving.job(Output.SYSTEM,
                   Arrays.asList("files", "move", "-r", leftPath().toString(), "@R/@F"))
              .run();

        assertEquals(expected, FileInfo.of(testPath()).toString());
    }

    @Test
    final void move_flat() throws RequestException {
        final String expected = String.format(TextIO.read(MovingTest.class, "MovingTest-move_RF.txt"),
                                              testID());

        Moving.job(Output.SYSTEM,
                   Arrays.asList("files", "move", leftPath().toString(), "@Y/@M/@D/@#"))
              .run();

        assertEquals(expected, FileInfo.of(testPath()).toString());
    }
}
