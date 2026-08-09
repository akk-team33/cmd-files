package de.team33.cmd.files.job;

import de.team33.cmd.files.common.RequestException;
import de.team33.cmd.files.testing.ModifyingTestBase;
import de.team33.patterns.io.thalassa.TextIO;
import de.team33.testing.io.hydra.FileInfo;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CleaningTest extends ModifyingTestBase {

    CleaningTest() {
        super(RELATIVE);
    }

    @Test
    final void clean_left() throws RequestException {
        // Given ...
        final String expected = TextIO.read(CleaningTest.class, "CleaningTest-clean_left.txt")
                                      .formatted(testID());
        Deletion.job(Context.of(MUTE, "files", "delete", "*.java", leftPath().toString()))
                .run();

        // When ...
        Cleaning.job(Context.of(MUTE, "files", "clean", testPath().toString()))
                .run();

        // Then ...
        assertEquals(expected, FileInfo.of(testPath()).toString());
    }
}
