package de.team33.cmd.files.main.job;

import de.team33.cmd.files.main.Main;
import de.team33.cmd.files.main.common.Output;
import de.team33.cmd.files.main.common.RequestException;
import de.team33.patterns.io.deimos.TextIO;
import de.team33.testing.io.hydra.FileInfo;
import de.team33.testing.io.hydra.ZipIO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DeletionTest {

    private static final String CLASS_NAME = DeletionTest.class.getSimpleName();
    private static final Path TEST_PATH = Path.of("target", "testing", CLASS_NAME);
    private static final Output MUTE = (format, args) -> {
    };

    private String uuid;
    private Path testPath;
    private Path leftPath;
    private Path rightPath;

    @BeforeEach
    final void init() {
        uuid = UUID.randomUUID().toString();
        testPath = TEST_PATH.resolve(uuid)
                            .toAbsolutePath()
                            .normalize();
        leftPath = testPath.resolve("left");
        rightPath = testPath.resolve("right");

        ZipIO.unzip(Main.class, "zips/leftFiles.zip", leftPath);
        assertEquals(TextIO.read(Main.class, "zips/leftFiles.txt"),
                     FileInfo.of(leftPath).toString());
        ZipIO.unzip(Main.class, "zips/rightFiles.zip", rightPath);
        assertEquals(TextIO.read(Main.class, "zips/rightFiles.txt"),
                     FileInfo.of(rightPath).toString());
    }

    @Test
    final void delete_java() throws RequestException {
        final String expected = String.format(TextIO.read(DeletionTest.class, "DeletionTest-delete_java.txt"), uuid);
        Deletion.job(Output.SYSTEM,
                     Arrays.asList("files", "delete", "*.java",
                                   leftPath.toString(),
                                   rightPath.toString()))
                .run();
        assertEquals(expected,
                     FileInfo.of(testPath).toString());
    }

    @Test
    final void delete_ALL() throws RequestException {
        final String expected = String.format(TextIO.read(DeletionTest.class, "DeletionTest-delete_ALL.txt"), uuid);
        Deletion.job(Output.SYSTEM,
                     Arrays.asList("files", "delete", "*",
                                   leftPath.toString(),
                                   rightPath.toString()))
                .run();
        assertEquals(expected,
                     FileInfo.of(testPath).toString());
    }

    @Test
    final void delete_MPsi() throws RequestException {
        final String expected = String.format(TextIO.read(DeletionTest.class, "DeletionTest-delete_MPsi.txt"), uuid);
        Deletion.job(Output.SYSTEM,
                     Arrays.asList("files", "delete", "rx/cs:[MPsi].*",
                                   leftPath.toString(),
                                   rightPath.toString()))
                .run();
        assertEquals(expected,
                     FileInfo.of(testPath).toString());
    }
}
