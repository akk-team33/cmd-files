package de.team33.cmd.files.job;

import de.team33.cmd.files.common.RequestException;
import de.team33.cmd.files.testing.Buffer;
import de.team33.cmd.files.testing.ModifyingTestBase;
import de.team33.patterns.io.deimos.TextIO;
import de.team33.testing.io.hydra.FileInfo;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RegistrarTest extends ModifyingTestBase {

    private static final FileTime DEFINITE_TIME = FileTime.from(Instant.parse("2024-01-01T00:00:00Z"));

    RegistrarTest() {
        super(RELATIVE, InitMode.FILL_BOTH);
    }

    @Test
    final void register_fs() throws RequestException {
        final Path registryPath = testPath().resolve("registry");
        final String expected = TextIO.read(RegistrarTest.class, "RegistrarTest-register_fs.txt")
                                      .formatted(testPath().getFileName());

        Registrar.job(MUTE, List.of("files", "register", leftPath().toString(), registryPath.toString(), "0"))
                 .run();
        Registrar.job(MUTE, List.of("files", "register", rightPath().toString(), registryPath.toString(), "0"))
                 .run();

        final String result = FileInfo.of(testPath()).toString();
        assertEquals(expected, result);
    }

    @Test
    final void register_out() throws RequestException {
        final Buffer buffer = new Buffer();
        final Path registryPath = testPath().resolve("registry");
        final String expected = TextIO.read(RegistrarTest.class, "RegistrarTest-register_out.txt")
                                      .formatted(testPath().getFileName());

        Registrar.job(buffer, List.of("files", "register", leftPath().toString(), registryPath.toString(), "0"))
                 .run();
        Registrar.job(buffer, List.of("files", "register", rightPath().toString(), registryPath.toString(), "0"))
                 .run();

        final String result = buffer.toString();
        assertEquals(expected, result);
    }
}
