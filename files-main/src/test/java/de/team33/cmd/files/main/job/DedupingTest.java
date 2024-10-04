package de.team33.cmd.files.main.job;

import de.team33.cmd.files.main.common.Output;
import de.team33.cmd.files.main.common.RequestException;
import de.team33.cmd.files.main.testing.ModifyingTestBase;
import de.team33.patterns.io.deimos.TextIO;
import de.team33.testing.io.hydra.FileInfo;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DedupingTest extends ModifyingTestBase {

    private static final FileTime REPRODUCIBLE = FileTime.from(Instant.parse("2024-01-01T00:00:00Z"));

    DedupingTest() {
        super(ABSOLUTE, InitMode.RIGHT_IN_LEFT);
    }

    @Test
    final void dedupe() throws RequestException, IOException {
        final String expected = String.format(TextIO.read(DedupingTest.class, "DedupingTest-dedupe.txt"), testID());

        Deduping.job(Output.SYSTEM, Arrays.asList("files", "dedupe", leftPath().toString())).run();
        Deduping.job(Output.SYSTEM, Arrays.asList("files", "dedupe", leftPath().toString())).run();

        Files.setLastModifiedTime(leftPath().resolve("(deduped-post).txt"), REPRODUCIBLE);
        assertEquals(expected, FileInfo.of(testPath()).toString());
    }
}
