package de.team33.cmd.files.job;

import de.team33.cmd.files.common.RequestException;
import de.team33.cmd.files.testing.Buffer;
import de.team33.cmd.files.testing.ModifyingTestBase;
import de.team33.patterns.files.pluto.FileEntry;
import de.team33.patterns.files.styx.Styx;
import de.team33.patterns.io.thalassa.TextIO;
import de.team33.testing.io.hydra.FileInfo;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RegistrarTest extends ModifyingTestBase {

    private static final FileTime DEFINITE_TIME = FileTime.from(Instant.parse("2024-01-01T00:00:00Z"));

    RegistrarTest() {
        super(RELATIVE, InitMode.FILL_BOTH);
    }

    private static void setDefiniteTime(final Path path) {
        try {
            Files.setLastModifiedTime(path, DEFINITE_TIME);
        } catch (IOException e) {
            throw new IllegalStateException(Optional.ofNullable(e.getMessage())
                                                    .orElseGet(e::toString), e);
        }
    }

    @Test
    final void register_fs() throws RequestException {
        final Path registryPath = testPath().resolve("registry");
        final String expected = TextIO.read(RegistrarTest.class, "RegistrarTest-register_fs.txt")
                                      .formatted(testPath().getFileName());
        final String leftQueryString = leftPath().resolve("**").toString();
        final String rightQueryString = rightPath().resolve("**").toString();

        Registrar.job(Context.of(MUTE, "files", "register", leftQueryString, registryPath.toString(), "4"))
                 .run();
        Registrar.job(Context.of(MUTE, "files", "register", rightQueryString, registryPath.toString(), "8"))
                 .run();

        Styx.stream(FileEntry.original(registryPath))
            .map(FileEntry::path)
            .forEach(RegistrarTest::setDefiniteTime);
        final String result = FileInfo.of(testPath()).toString();
        assertEquals(expected, result);
    }

    @Test
    final void register_out() throws RequestException {
        final Buffer buffer = new Buffer();
        final Path registryPath = testPath().resolve("registry");
        final String expected = TextIO.read(RegistrarTest.class, "RegistrarTest-register_out.txt")
                                      .formatted();
        final String leftQueryString = leftPath().resolve("**").toString();
        final String rightQueryString = rightPath().resolve("**").toString();

        Registrar.job(Context.of(buffer, "files", "register", leftQueryString, registryPath.toString(), "0"))
                 .run();
        Registrar.job(Context.of(buffer, "files", "register", rightQueryString, registryPath.toString(), "0"))
                 .run();

        final String result = buffer.toString()
                                    .replace(testPath().toAbsolutePath().normalize().toString(), "...");
        assertEquals(expected, result);
    }
}
