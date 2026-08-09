package de.team33.cmd.files.job;

import de.team33.cmd.files.common.RequestException;
import de.team33.cmd.files.testing.ModifyingTestBase;
import de.team33.patterns.io.thalassa.TextIO;
import de.team33.testing.io.hydra.FileInfo;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MovingTest extends ModifyingTestBase {

    MovingTest() {
        super(RELATIVE, InitMode.FILL_LEFT_ONLY);
    }

    @Test
    final void move_PYMDF() throws RequestException {
        final String expected = TextIO.read(MovingTest.class, "MovingTest-move_PYMDF.txt")
                                      .formatted(testID());
        final String queryString = leftPath().resolve("**").toString();

        Moving.job(Context.of(MUTE, "files", "move", queryString, "../@P.moved/@Y/@M/@D/@F"))
              .run();

        assertEquals(expected, FileInfo.of(testPath()).toString());
    }

    @Test
    final void move_N13F() throws RequestException {
        final String expected = TextIO.read(MovingTest.class, "MovingTest-move_N13F.txt")
                                      .formatted(testID());
        final String queryString = leftPath().resolve("**").toString();

        Moving.job(Context.of(MUTE, "files", "move", queryString, "../@P.moved/@N(1,3)/@F"))
              .run();

        assertEquals(expected, FileInfo.of(testPath()).toString());
    }

    @Test
    final void move_N1F() throws RequestException {
        final String expected = TextIO.read(MovingTest.class, "MovingTest-move_N1F.txt")
                                      .formatted(testID());
        final String queryString = leftPath().resolve("**").toString();

        Moving.job(Context.of(MUTE, "files", "move", queryString, "../@P.moved/@N(1)/@F"))
              .run();

        assertEquals(expected, FileInfo.of(testPath()).toString());
    }

    @Test
    final void move_RpNX() throws RequestException {
        final String expected = String.format(TextIO.read(MovingTest.class, "MovingTest-move_RpNX.txt"), testID());
        final String queryString = leftPath().resolve("**").toString();

        Moving.job(Context.of(MUTE, "files", "move", queryString, "@R/../@p-@N.@X"))
              .run();

        assertEquals(expected, FileInfo.of(testPath()).toString());
    }

    @Test
    final void move_hms_hash_X() throws RequestException {
        final String expected = String.format(TextIO.read(MovingTest.class, "MovingTest-move_hms_hash_X.txt"),
                                              testID());
        final String queryString = leftPath().resolve("**").toString();

        Moving.job(Context.of(MUTE, "files", "move", queryString, "@h/@m/@s/@#.@X"))
              .run();

        assertEquals(expected, FileInfo.of(testPath()).toString());
    }

    @Test
    final void move_R_hash_time_X() throws RequestException {
        final String expected = String.format(TextIO.read(MovingTest.class, "MovingTest-move_R_hash_time_X.txt"),
                                              testID());
        final String queryString = leftPath().resolve("**").toString();

        Moving.job(Context.of(MUTE, "files", "move", queryString, "@R/@#@!.@X"))
              .run();
        Moving.job(Context.of(MUTE, "files", "move", queryString, "@R/@#@!.@X"))
              .run();

        assertEquals(expected, FileInfo.of(testPath()).toString());
    }

    @Test
    final void move_RF() throws RequestException {
        final String expected = String.format(TextIO.read(MovingTest.class, "MovingTest-move_RF.txt"),
                                              testID());
        final String queryString = leftPath().resolve("**").toString();

        Moving.job(Context.of(MUTE, "files", "move", queryString, "@R/@F"))
              .run();

        assertEquals(expected, FileInfo.of(testPath()).toString());
    }

    @Test
    final void move_flat() throws RequestException {
        final String expected = String.format(TextIO.read(MovingTest.class, "MovingTest-move_RF.txt"),
                                              testID());
        final String queryString = leftPath().toString();

        Moving.job(Context.of(MUTE, "files", "move", queryString, "@Y/@M/@D/@#"))
              .run();

        assertEquals(expected, FileInfo.of(testPath()).toString());
    }
}
