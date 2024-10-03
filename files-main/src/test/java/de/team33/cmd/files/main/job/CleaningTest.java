package de.team33.cmd.files.main.job;

import de.team33.cmd.files.main.common.RequestException;
import de.team33.cmd.files.main.testing.ModifyingTestBase;
import de.team33.patterns.io.deimos.TextIO;
import de.team33.testing.io.hydra.FileInfo;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CleaningTest extends ModifyingTestBase {

    CleaningTest() {
        super(RELATIVE);
    }

    @Test
    final void clean_left() throws RequestException {
        // Given ...
        final String expected = String.format(TextIO.read(CleaningTest.class, "CleaningTest-clean_left.txt"), testID());
        Deletion.job(MUTE, Arrays.asList("files", "delete", "*.java", leftPath().toString()))
                .run();

        // When ...
        Cleaning.job(MUTE, Arrays.asList("files", "clean", testPath().toString()))
                .run();

        // Then ...
        assertEquals(expected, FileInfo.of(testPath()).toString());
    }
}
