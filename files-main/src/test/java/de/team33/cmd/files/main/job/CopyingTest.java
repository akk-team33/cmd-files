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

class CopyingTest {

    private static final String CLASS_NAME = CopyingTest.class.getSimpleName();
    private static final Path TEST_PATH = Path.of("target", "testing", CLASS_NAME);
    private static final Output MUTE = (format, args) -> {};

    private Path leftPath;
    private Path rightPath;

    @BeforeEach
    final void init() {
        final Path testPath = TEST_PATH.resolve(UUID.randomUUID().toString())
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
    final void test_() throws RequestException {
        Copying.job(Output.SYSTEM,
                    Arrays.asList("files", "copy", "C",
                                  leftPath.toString(),
                                  rightPath.toString()))
               .run();
        assertEquals(TextIO.read(CopyingTest.class, "CopyingTest-copy-C.txt"),
                     FileInfo.of(rightPath).toString());
    }
}
