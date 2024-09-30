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

class DirFinderTest {

    private static final String CLASS_NAME = DirFinderTest.class.getSimpleName();
    private static final Path TEST_PATH = Path.of("target", "testing", CLASS_NAME);

    private Path testPath;

    @BeforeEach
    final void init() {
        testPath = TEST_PATH.resolve(UUID.randomUUID().toString())
                            .resolve("left")
                            .toAbsolutePath()
                            .normalize();
        ZipIO.unzip(Main.class, "zips/leftFiles.zip", testPath);
        assertEquals(TextIO.read(Main.class, "zips/leftFiles.txt"),
                     FileInfo.of(testPath).toString());
    }

    @Test
    final void run_java() throws RequestException, IOException {
        final Runnable job = DirFinder.job(Output.SYSTEM,
                                        Arrays.asList("files", "findir", "*.java", testPath.toString()));
        final String result = Redirected.outputOf(job::run)
                                        .replace(testPath.toString(), "[PATH]");
        assertEquals(TextIO.read(DirFinderTest.class, "DirFinderTest-run_java.txt"), result);
    }

    @Test
    final void run_56() throws RequestException, IOException {
        final Runnable job = DirFinder.job(Output.SYSTEM,
                                        Arrays.asList("files", "findir", "rx:.{5,6}", testPath.toString()));
        final String result = Redirected.outputOf(job::run)
                                        .replace(testPath.toString(), "[PATH]");
        assertEquals(TextIO.read(DirFinderTest.class, "DirFinderTest-run_56.txt"), result);
    }
}
