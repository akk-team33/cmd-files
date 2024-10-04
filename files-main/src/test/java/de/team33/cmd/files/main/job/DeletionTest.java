package de.team33.cmd.files.main.job;

import de.team33.cmd.files.main.common.RequestException;
import de.team33.cmd.files.main.testing.ModifyingTestBase;
import de.team33.patterns.io.deimos.TextIO;
import de.team33.testing.io.hydra.FileInfo;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DeletionTest extends ModifyingTestBase {

    DeletionTest() {
        super(RELATIVE);
    }

    @Test
    final void delete_java() throws RequestException {
        final String expected = String.format(TextIO.read(DeletionTest.class,
                                                          "DeletionTest-delete_java.txt"), testID());
        Deletion.job(MUTE, Arrays.asList("files", "delete", "*.java", leftPath().toString(), rightPath().toString()))
                .run();
        assertEquals(expected, FileInfo.of(testPath()).toString());
    }

    @Test
    final void delete_ALL() throws RequestException {
        final String expected = String.format(TextIO.read(DeletionTest.class,
                                                          "DeletionTest-delete_ALL.txt"), testID());
        Deletion.job(MUTE, Arrays.asList("files", "delete", "*", leftPath().toString(), rightPath().toString()))
                .run();
        assertEquals(expected, FileInfo.of(testPath()).toString());
    }

    @Test
    final void delete_MPsi() throws RequestException {
        final String expected = String.format(TextIO.read(DeletionTest.class,
                                                          "DeletionTest-delete_MPsi.txt"), testID());
        Deletion.job(MUTE, Arrays.asList("files", "delete", "rx/cs:[MPsi].*",
                                         leftPath().toString(), rightPath().toString()))
                .run();
        assertEquals(expected, FileInfo.of(testPath()).toString());
    }
}
