package de.team33.cmd.files.main.balancing.publics;

import de.team33.cmd.files.main.Main;
import de.team33.cmd.files.main.balancing.Relative;
import de.team33.cmd.files.main.balancing.Relatives;
import de.team33.cmd.files.main.common.Output;
import de.team33.cmd.files.main.common.RequestException;
import de.team33.patterns.io.deimos.TextIO;
import de.team33.testing.io.hydra.FileInfo;
import de.team33.testing.io.hydra.ZipIO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StateTest {

    private static final String CLASS_NAME = StateTest.class.getSimpleName();
    private static final Path TEST_PATH = Path.of("target", "testing", CLASS_NAME);
    private static final Output MUTE = (format, args) -> {
    };

    private Path leftRoot;
    private Path rightRoot;

    @BeforeEach
    final void init() {
        final Path testPath = TEST_PATH.resolve(UUID.randomUUID().toString());
        leftRoot = testPath.resolve("left");
        rightRoot = testPath.resolve("right");

        ZipIO.unzip(Main.class, "zips/leftFiles.zip", leftRoot);
        assertEquals(TextIO.read(Main.class, "zips/leftFiles.txt"),
                     FileInfo.of(leftRoot).toString());
        ZipIO.unzip(Main.class, "zips/rightFiles.zip", rightRoot);
        assertEquals(TextIO.read(Main.class, "zips/rightFiles.txt"),
                     FileInfo.of(rightRoot).toString());
    }

    @Test
    final void of() throws RequestException {
        final String result = Relatives.stream(leftRoot, rightRoot)
                                       .map(this::report_)
                                       .collect(Collectors.joining())
                                       .trim();
        assertEquals(TextIO.read(StateTest.class, "StateTest-of.txt"), result);
    }

    private String report_(final Relative relative) {
        return String.format("%s - %s%n", relative.path(), relative.state());
    }
}
