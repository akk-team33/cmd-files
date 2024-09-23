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

class CopyingTest {

    private static final Path TEST_PATH = Path.of("target", "testing", CopyingTest.class.getSimpleName())
                                              .toAbsolutePath()
                                              .normalize();

    private final String uuid = UUID.randomUUID().toString();
    private final Path srcPath = TEST_PATH.resolve(uuid)
                                          .resolve("left");
    private final Path tgtPath = TEST_PATH.resolve(uuid)
                                          .resolve("right");

    @Test
    final void run_C() throws IOException, RequestException {
        ZipIO.unzip(CopyingTest.class, "LeftFiles.zip", srcPath);

        // first
        {
            final String result = Redirected.outputOf(() -> Copying.job(Output.SYSTEM,
                                                                        Arrays.asList("files", "copy", "C",
                                                                                      srcPath.toString(),
                                                                                      tgtPath.toString()))
                                                                   .run());
            // System.out.println(result);

            assertEquals(TextIO.read(getClass(), "Copying-run-C.txt"), result);
        }

        // second
        {
            final String result = Redirected.outputOf(() -> Copying.job(Output.SYSTEM,
                                                                        Arrays.asList("files", "copy", "C",
                                                                                      srcPath.toString(),
                                                                                      tgtPath.toString()))
                                                                   .run());
            // System.out.println(result);

            assertEquals(TextIO.read(getClass(), "Copying-run-C-second.txt"), result);
        }
    }
}
