package de.team33.cmd.files.main.copying.publics;

import de.team33.cmd.files.main.copying.Relative;
import de.team33.patterns.io.deimos.TextIO;
import de.team33.testing.io.hydra.FileInfo;
import de.team33.testing.io.hydra.ZipIO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RelativeTest {

    private static final String CLASS_NAME = RelativeTest.class.getSimpleName();
    private static final Path TEST_PATH = Path.of("target", "testing", CLASS_NAME);

    private Path leftPath;
    private Path rightPath;

    @BeforeEach
    final void init() {
        final Path testPath = TEST_PATH.resolve(UUID.randomUUID().toString());
        leftPath = testPath.resolve("left");
        rightPath = testPath.resolve("right");

        ZipIO.unzip(Relative.class, "leftFiles.zip", leftPath);
        assertEquals(TextIO.read(Relative.class, "leftFiles.txt"),
                     FileInfo.of(leftPath).toString());
        ZipIO.unzip(Relative.class, "rightFiles.zip", rightPath);
        assertEquals(TextIO.read(Relative.class, "rightFiles.txt"),
                     FileInfo.of(rightPath).toString());
    }

    @Test
    final void test() {
        final Set<Path> expected = TextIO.read(RelativeTest.class, "RelativeTest-collect.txt")
                                         .lines()
                                         .filter(Predicate.not(String::isBlank))
                                         .map(Path::of)
                                         .collect(Collectors.toSet());
        final Set<Path> result = Relative.collect(leftPath, rightPath);
        assertEquals(expected, result);
    }
}