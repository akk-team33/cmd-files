package de.team33.cmd.files.main.copying.publics;

import de.team33.cmd.files.main.copying.Relative;
import de.team33.patterns.io.deimos.TextIO;
import de.team33.testing.io.hydra.FileInfo;
import de.team33.testing.io.hydra.ZipIO;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RelativeTest {

    private static final String CLASS_NAME = RelativeTest.class.getSimpleName();
    private static final Path TEST_PATH = Path.of("target", "testing", CLASS_NAME);

    private final String uuid = UUID.randomUUID().toString();
    private final Path mainPath = TEST_PATH.resolve(uuid);
    private final Path leftPath = mainPath.resolve("left");
    private final Path rightPath = mainPath.resolve("right");

    {
        ZipIO.unzip(Relative.class, "leftFiles.zip", leftPath);
        assertEquals(TextIO.read(Relative.class, "leftFiles.txt"),
                     FileInfo.of(leftPath).toString());
        ZipIO.unzip(Relative.class, "rightFiles.zip", rightPath);
        assertEquals(TextIO.read(Relative.class, "rightFiles.txt"),
                     FileInfo.of(rightPath).toString());
    }

    @Test
    final void test() {
        //Relative
    }
}