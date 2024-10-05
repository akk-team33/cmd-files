package de.team33.cmd.files.job;

import de.team33.cmd.files.common.RequestException;
import de.team33.cmd.files.testing.ModifyingTestBase;
import de.team33.patterns.io.deimos.TextIO;
import de.team33.testing.io.hydra.FileInfo;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CopyingTest extends ModifyingTestBase {

    CopyingTest() {
        super(RELATIVE);
    }

    @Test
    final void copy_C() throws RequestException {
        Copying.job(MUTE, Arrays.asList("files", "copy", "C", leftPath().toString(), rightPath().toString()))
               .run();
        assertEquals(TextIO.read(CopyingTest.class, "CopyingTest-copy-C.txt"),
                     FileInfo.of(rightPath()).toString());
    }

    @Test
    final void copy_U() throws RequestException {
        Copying.job(MUTE, Arrays.asList("files", "copy", "U", leftPath().toString(), rightPath().toString()))
               .run();
        assertEquals(TextIO.read(CopyingTest.class, "CopyingTest-copy-U.txt"),
                     FileInfo.of(rightPath()).toString());
    }

    @Test
    final void copy_O() throws RequestException {
        Copying.job(MUTE, Arrays.asList("files", "copy", "O", leftPath().toString(), rightPath().toString()))
               .run();
        assertEquals(TextIO.read(CopyingTest.class, "CopyingTest-copy-O.txt"),
                     FileInfo.of(rightPath()).toString());
    }

    @Test
    final void copy_R() throws RequestException {
        Copying.job(MUTE, Arrays.asList("files", "copy", "R", leftPath().toString(), rightPath().toString()))
               .run();
        assertEquals(TextIO.read(CopyingTest.class, "CopyingTest-copy-R.txt"),
                     FileInfo.of(rightPath()).toString());
    }

    @Test
    final void copy_D() throws RequestException {
        Copying.job(MUTE, Arrays.asList("files", "copy", "D", leftPath().toString(), rightPath().toString()))
               .run();
        assertEquals(TextIO.read(CopyingTest.class, "CopyingTest-copy-D.txt"),
                     FileInfo.of(rightPath()).toString());
    }
}
