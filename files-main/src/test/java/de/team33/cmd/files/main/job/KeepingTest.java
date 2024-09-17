package de.team33.cmd.files.main.job;

import de.team33.cmd.files.main.common.Output;
import de.team33.cmd.files.main.common.RequestException;
import de.team33.patterns.io.deimos.TextIO;
import de.team33.testing.io.hydra.FileInfo;
import de.team33.testing.io.hydra.ZipIO;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class KeepingTest {

    private static final Path TEST_PATH = Path.of("target", "testing", KeepingTest.class.getSimpleName());

    private final String uuid = UUID.randomUUID().toString();
    private final Path keepingPath = TEST_PATH.resolve(uuid).resolve("keeping");

    private String keepingInfo() {
        return FileInfo.of(keepingPath.getParent()).toString().replace(uuid, "[UUID]");
    }

    private String expectedInfo(final String rsrcName) {
        return TextIO.read(getClass(), rsrcName);
    }

    @Test
    final void run_singlePath() throws RequestException {
        ZipIO.unzip(KeepingTest.class, "Keeping.zip", keepingPath);
        assertEquals(expectedInfo("KeepingRunInitial.txt"), keepingInfo());

        Keeping.job(Output.SYSTEM, Arrays.asList("files", "keep", keepingPath.toString(), "jpg,jpe,jpeg", "tif,tiff")).run();

        assertEquals(expectedInfo("KeepingRunExpected.txt"), keepingInfo());
    }

    @Test
    final void run_dualPath() throws RequestException {
        final String path1 = keepingPath.toString();
        final String path2 = keepingPath.toString() + ".moved";
        ZipIO.unzip(KeepingTest.class, "Keeping.zip", keepingPath);
        Keeping.job(((format, args) -> {}), Arrays.asList("files", "keep", path1, "none", "tif,tiff")).run();

        Keeping.job(Output.SYSTEM, Arrays.asList("files", "keep", path1, "jpg,jpe,jpeg", path2, "tif,tiff")).run();

        assertEquals(expectedInfo("KeepingRunDPExpected.txt"), keepingInfo());
    }
}
