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
    final void move_A() throws RequestException {
        final String expected = String.format(TextIO.read(MovingTest.class, "MovingTest-move_A.txt"), testID());

        Moving.job(Output.SYSTEM,
                   Arrays.asList("files", "move", "-r", leftPath().toString(), "../left.moved/@Y/@M/@D/@F"))
              .run();

        assertEquals(expected, FileInfo.of(testPath()).toString());
    }
}
