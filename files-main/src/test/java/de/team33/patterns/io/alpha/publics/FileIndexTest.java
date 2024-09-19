package de.team33.patterns.io.alpha.publics;

import de.team33.patterns.io.alpha.FileEntry;
import de.team33.patterns.io.alpha.FileIndex;
import de.team33.patterns.io.alpha.FilePolicy;
import de.team33.patterns.io.deimos.TextIO;
import de.team33.testing.io.hydra.ZipIO;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import static java.util.Comparator.comparing;
import static org.junit.jupiter.api.Assertions.assertEquals;

class FileIndexTest {

    private static final Path TEST_PATH = Path.of("target", "testing",
                                                  FileIndexTest.class.getSimpleName(),
                                                  UUID.randomUUID().toString());

    @BeforeAll
    static void beforeAll() {
        ZipIO.unzip(FileIndexTest.class, "FileIndex.zip", TEST_PATH);
    }

    @Test
    void files() {
        final List<Path> expected = TextIO.read(FileIndexTest.class, "FileIndexTest-files.txt")
                                          .lines()
                                          .map(line -> TEST_PATH.resolve(line).toAbsolutePath().normalize())
                                          .toList();
        final FileIndex index = FileIndex.of(TEST_PATH, FilePolicy.DISTINCT_SYMLINKS);

        final List<Path> result = index.entries()
                                       .map(FileEntry::path)
                                       .sorted(comparing(Path::toString))
                                       .toList();

        assertEquals(expected, result);
    }

    @Test
    void files_skip() {
        final List<Path> expected = TextIO.read(FileIndexTest.class, "FileIndexTest-files_skip.txt")
                                          .lines()
                                          .map(line -> TEST_PATH.resolve(line).toAbsolutePath().normalize())
                                          .toList();
        final FileIndex index = FileIndex.of(TEST_PATH, FilePolicy.DISTINCT_SYMLINKS)
                                         .skipEntry(entry -> entry.name().equals("patterns"));

        final List<Path> result = index.entries()
                                       .map(FileEntry::path)
                                       .sorted(comparing(Path::toString))
                                       .toList();

        assertEquals(expected, result);
    }
}
