package de.team33.cmd.files.job;

import de.team33.cmd.files.common.RequestException;
import de.team33.cmd.files.moving.Guard;
import de.team33.cmd.files.testing.ModifyingTestBase;
import de.team33.patterns.io.deimos.TextIO;
import de.team33.testing.io.hydra.FileInfo;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DedupingTest extends ModifyingTestBase {

    private static final FileTime DEFINITE_TIME = FileTime.from(Instant.parse("2024-01-01T00:00:00Z"));

    DedupingTest() {
        super(ABSOLUTE, InitMode.FILL_BOTH);
    }

    @Test
    final void dedupe() throws RequestException, IOException {
        final Path leftIndexPath = leftPath().resolve(Guard.DEDUPED_INDEX);
        final Path rightIndexPath = rightPath().resolve(Guard.DEDUPED_INDEX);

        Deduping.job(MUTE, Arrays.asList("files", "dedupe", leftPath().toString())).run();
        assertEquals(TextIO.read(DedupingTest.class, "DedupingTest-dedupe-index-left.txt")
                           .formatted(TextIO.read(leftPath().resolve(Guard.DEDUPE_PATH_ID))),
                     TextIO.read(leftIndexPath));

        Files.copy(leftIndexPath, rightIndexPath);

        Deduping.job(MUTE, Arrays.asList("files", "dedupe", rightPath().toString())).run();
        assertEquals(TextIO.read(DedupingTest.class, "DedupingTest-dedupe-index-right.txt")
                           .formatted(TextIO.read(rightPath().resolve(Guard.DEDUPE_PATH_ID))),
                     TextIO.read(rightIndexPath));

        for (final String name : List.of(Guard.DEDUPE_PATH_ID, Guard.DEDUPED_INDEX))
            for (final Path path : List.of(leftPath(), rightPath()))
                Files.setLastModifiedTime(path.resolve(name), DEFINITE_TIME);
        assertEquals(TextIO.read(DedupingTest.class, "DedupingTest-dedupe.txt")
                           .formatted(testID()),
                     FileInfo.of(testPath()).toString());
    }
}
