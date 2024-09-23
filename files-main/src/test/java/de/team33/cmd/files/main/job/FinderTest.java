package de.team33.cmd.files.main.job;

import de.team33.cmd.files.main.common.Output;
import de.team33.cmd.files.main.common.RequestException;
import de.team33.patterns.io.deimos.TextIO;
import de.team33.testing.io.hydra.ZipIO;
import de.team33.testing.stdio.ersa.Redirected;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FinderTest {

    private static final Path TEST_PATH = Path.of("target", "testing", FinderTest.class.getSimpleName());

    private final String uuid = UUID.randomUUID().toString();
    private final Path testPath = TEST_PATH.resolve(uuid)
                                           .toAbsolutePath()
                                           .normalize();

    @Test
    final void run_All() throws IOException, RequestException {
        ZipIO.unzip(FinderTest.class, "Keeping.zip", testPath);

        final String result = Redirected.outputOf(() -> Finder.job(Output.SYSTEM,
                                                                   Arrays.asList("files", "find", "*",
                                                                                 testPath.toString()))
                                                              .run())
                                        .replace(testPath.toString(), "[PATH]");
        // System.out.println(result);

        assertEquals(TextIO.read(getClass(), "Finder-run-All.txt"), result);
    }

    @Test
    final void run_DSC_0001() throws IOException, RequestException {
        ZipIO.unzip(FinderTest.class, "Keeping.zip", testPath);

        final String result = Redirected.outputOf(() -> Finder.job(Output.SYSTEM,
                                                                   Arrays.asList("files", "find", "*_0001.*",
                                                                                 testPath.toString()))
                                                              .run())
                                        .replace(testPath.toString(), "[PATH]");
        // result.forEach(System.out::println);

        assertEquals(TextIO.read(getClass(), "Finder-run-DSC_0001.txt"), result);
    }

    @Test
    final void run_TIFF() throws IOException, RequestException {
        ZipIO.unzip(FinderTest.class, "Keeping.zip", testPath);

        final String result = Redirected.outputOf(() -> Finder.job(Output.SYSTEM,
                                                                   Arrays.asList("files", "find",
                                                                                 "rx:.*\\.TIFF",
                                                                                 testPath.toString()))
                                                              .run())
                                        .replace(testPath.toString(), "[PATH]");
        // result.forEach(System.out::println);

        assertEquals(TextIO.read(getClass(), "Finder-run-TIFF.txt"), result);
    }
}
