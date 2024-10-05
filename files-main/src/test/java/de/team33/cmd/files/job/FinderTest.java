package de.team33.cmd.files.job;

import de.team33.cmd.files.Main;
import de.team33.cmd.files.common.Output;
import de.team33.cmd.files.common.RequestException;
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

class FinderTest {

    private static final String CLASS_NAME = FinderTest.class.getSimpleName();
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
    final void run_rxALL() throws RequestException, IOException {
        final Runnable job = Finder.job(Output.SYSTEM,
                                        Arrays.asList("files", "find", "rx:.*", testPath.toString()));
        final String result = Redirected.outputOf(job::run)
                                        .replace(testPath.toString(), "[PATH]");
        assertEquals(TextIO.read(FinderTest.class, "FinderTest-run_ALL.txt"), result);
    }

    @Test
    final void run_wcALL() throws RequestException, IOException {
        final Runnable job = Finder.job(Output.SYSTEM,
                                        Arrays.asList("files", "find", "*", testPath.toString()));
        final String result = Redirected.outputOf(job::run)
                                        .replace(testPath.toString(), "[PATH]");
        assertEquals(TextIO.read(FinderTest.class, "FinderTest-run_ALL.txt"), result);
    }

    @Test
    final void run_java() throws RequestException, IOException {
        final Runnable job = Finder.job(Output.SYSTEM,
                                        Arrays.asList("files", "find", "*.java", testPath.toString()));
        final String result = Redirected.outputOf(job::run)
                                        .replace(testPath.toString(), "[PATH]");
        assertEquals(TextIO.read(FinderTest.class, "FinderTest-run_java.txt"), result);
    }

    @Test
    final void run_56() throws RequestException, IOException {
        final Runnable job = Finder.job(Output.SYSTEM,
                                        Arrays.asList("files", "find", "rx:.{5,6}", testPath.toString()));
        final String result = Redirected.outputOf(job::run)
                                        .replace(testPath.toString(), "[PATH]");
        assertEquals(TextIO.read(FinderTest.class, "FinderTest-run_56.txt"), result);
    }
}
