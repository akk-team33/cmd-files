package de.team33.cmd.files.main.job;

import de.team33.cmd.files.main.Main;
import de.team33.cmd.files.main.common.Output;
import de.team33.cmd.files.main.common.RequestException;
import de.team33.patterns.io.deimos.TextIO;
import de.team33.testing.io.hydra.FileInfo;
import de.team33.testing.io.hydra.ZipIO;
import de.team33.testing.stdio.ersa.Redirected;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ComparingTest {

    private static final String CLASS_NAME = ComparingTest.class.getSimpleName();
    private static final Path TEST_PATH = Path.of("target", "testing", CLASS_NAME);
    private static final Output MUTE = (format, args) -> {
    };

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
    final void compare() throws RequestException, IOException {
        final Runnable job = Comparing.job(Output.SYSTEM,
                                           Arrays.asList("files", "cmp",
                                                         leftPath.toString(),
                                                         rightPath.toString()));
        final String result = Redirected.outputOf(job::run);
        assertEquals(TextIO.read(ComparingTest.class, "ComparingTest-compare.txt"), result);
    }
}
