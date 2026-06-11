package de.team33.cmd.files.job;

import de.team33.cmd.files.common.RequestException;
import de.team33.cmd.files.testing.ModifyingTestBase;
import de.team33.patterns.io.deimos.TextIO;
import de.team33.testing.io.hydra.FileInfo;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RegistrarTest extends ModifyingTestBase {

    private static final FileTime DEFINITE_TIME = FileTime.from(Instant.parse("2024-01-01T00:00:00Z"));

    RegistrarTest() {
        super(ABSOLUTE, InitMode.FILL_BOTH);
    }

    @Test
    final void register() throws RequestException {
        final Path registryPath = testPath().resolve("registry");
        final String expected = TextIO.read(RegistrarTest.class, "RegistrarTest-register.txt")
                                      .formatted(testPath().getFileName());

        Registrar.job(MUTE, Arrays.asList("files", "register", leftPath().toString(), registryPath.toString(), "0"))
                 .run();
        Registrar.job(MUTE, Arrays.asList("files", "register", rightPath().toString(), registryPath.toString(), "0"))
                 .run();

        final String result = FileInfo.of(testPath()).toString();
        assertEquals(expected, result);
    }
}
