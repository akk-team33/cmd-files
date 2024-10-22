package de.team33.cmd.files.job;

import de.team33.cmd.files.common.RequestException;
import de.team33.cmd.files.testing.ModifyingTestBase;
import de.team33.patterns.io.deimos.TextIO;
import de.team33.testing.io.hydra.FileInfo;
import org.junit.jupiter.api.Test;

import static de.team33.cmd.files.job.Util.condition;
import static org.junit.jupiter.api.Assertions.assertEquals;

class DirCopyingTest extends ModifyingTestBase {

    DirCopyingTest() {
        super(ABSOLUTE, InitMode.FILL_LEFT_ONLY);
    }

    @Test
    final void dcopy() throws RequestException {
        DirCopying.job(condition(MUTE, "files", "dcopy", leftPath().toString(), rightPath().toString()))
                  .run();
        assertEquals(TextIO.read(DirCopyingTest.class, "DirCopyingTest-dcopy.txt"),
                     FileInfo.of(rightPath()).toString());
        DirCopying.job(condition(MUTE, "files", "dcopy", leftPath().toString(), rightPath().toString()))
                  .run();
        assertEquals(TextIO.read(DirCopyingTest.class, "DirCopyingTest-dcopy.txt"),
                     FileInfo.of(rightPath()).toString());
    }
}
