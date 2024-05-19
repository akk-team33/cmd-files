package de.team33.cmd.files.main.job;

import de.team33.cmd.files.main.common.Context;
import de.team33.patterns.io.deimos.TextIO;
import de.team33.patterns.testing.titan.io.FileInfo;
import de.team33.patterns.testing.titan.io.ZipIO;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class KeepingTest implements Context {

    private static final Path TEST_PATH = Path.of("target", "testing", KeepingTest.class.getSimpleName())
                                              .toAbsolutePath()
                                              .normalize();
    private final Path keepingPath = TEST_PATH.resolve(UUID.randomUUID().toString()).resolve("keeping");

    @Test
    final void run_singlePath() {
        ZipIO.unzip(KeepingTest.class, "Keeping.zip", keepingPath);
        assertEquals(TextIO.read(KeepingTest.class, "KeepingRunInitial.txt"), FileInfo.of(keepingPath).toString());

        Keeping.job(this, Arrays.asList("files", "keep", keepingPath.toString(), "jpg,jpe,jpeg", "tif,tiff")).run();

        assertEquals(TextIO.read(KeepingTest.class, "KeepingRunExpected.txt"), FileInfo.of(keepingPath).toString());
    }
}