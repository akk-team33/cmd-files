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

class KeepingTest {

    private static final Path TEST_PATH = Path.of("target", "testing", KeepingTest.class.getSimpleName());
    private static final Context VERBOSE = new Context(){
    };
    private static final Context SILENT = new Context() {
        @Override
        public void printf(final String format, final Object... args) {
        }
    };

    private final Path keepingPath = TEST_PATH.resolve(UUID.randomUUID().toString()).resolve("keeping");

    @Test
    final void run_singlePath() {
        ZipIO.unzip(KeepingTest.class, "Keeping.zip", keepingPath);
        assertEquals(TextIO.read(KeepingTest.class, "KeepingRunInitial.txt"), FileInfo.of(keepingPath).toString());

        Keeping.job(VERBOSE, Arrays.asList("files", "keep", keepingPath.toString(), "jpg,jpe,jpeg", "tif,tiff")).run();

        assertEquals(TextIO.read(KeepingTest.class, "KeepingRunExpected.txt"), FileInfo.of(keepingPath).toString());
    }

    @Test
    final void run_dualPath() {
        final String path1 = keepingPath.toString();
        final String path2 = keepingPath.resolve("(moved)").toString();
        ZipIO.unzip(KeepingTest.class, "Keeping.zip", keepingPath);
        Keeping.job(SILENT, Arrays.asList("files", "keep", path1, "none", "tif,tiff")).run();

        Keeping.job(VERBOSE, Arrays.asList("files", "keep", path1, "jpg,jpe,jpeg", path2, "tif,tiff")).run();

        assertEquals(TextIO.read(KeepingTest.class, "KeepingRunDPExpected.txt"), FileInfo.of(keepingPath).toString());
    }
}