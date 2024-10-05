package de.team33.cmd.files.testing;

import de.team33.cmd.files.Main;
import de.team33.cmd.files.common.Output;
import de.team33.patterns.io.deimos.TextIO;
import de.team33.testing.io.hydra.FileInfo;
import de.team33.testing.io.hydra.ZipIO;
import org.junit.jupiter.api.BeforeEach;

import java.nio.file.Path;
import java.util.UUID;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;

public abstract class ModifyingTestBase {

    public static final Output MUTE = (format, args) -> {};
    public static final Function<Path, Path> RELATIVE = path -> path;
    public static final Function<Path, Path> ABSOLUTE = path -> path.toAbsolutePath().normalize();

    private final Function<Path, Path> normal;
    private final InitMode initMode;

    private String testID;
    private Path testPath;
    private Path leftPath;
    private Path rightPath;

    protected ModifyingTestBase(final Function<Path, Path> normal, final InitMode initMode) {
        this.normal = normal;
        this.initMode = initMode;
    }

    protected ModifyingTestBase(final Function<Path, Path> normal) {
        this(normal, InitMode.FILL_BOTH);
    }

    public final String testID() {
        return testID;
    }

    public final Path testPath() {
        return testPath;
    }

    public final Path leftPath() {
        return leftPath;
    }

    public final Path rightPath() {
        return rightPath;
    }

    @BeforeEach
    public final void initModifyingTestBase() {
        testID = UUID.randomUUID().toString();
        testPath = normal.apply(Path.of("target", "testing", getClass().getSimpleName(), testID));
        leftPath = testPath.resolve("left");
        rightPath = initMode.rightInLeft ? leftPath.resolve("right") : testPath.resolve("right");

        if (initMode.fillLeft) {
            ZipIO.unzip(Main.class, "zips/leftFiles.zip", leftPath);
            assertEquals(TextIO.read(Main.class, "zips/leftFiles.txt"),
                         FileInfo.of(leftPath).toString());
        }
        if (initMode.fillRight) {
            ZipIO.unzip(Main.class, "zips/rightFiles.zip", rightPath);
            assertEquals(TextIO.read(Main.class, "zips/rightFiles.txt"),
                         FileInfo.of(rightPath).toString());
        }
    }

    public enum InitMode {
        RIGHT_IN_LEFT(true, true, true),
        FILL_LEFT_ONLY(false, true, false),
        FILL_BOTH(false, true, true);

        private final boolean fillLeft;
        private final boolean fillRight;
        private final boolean rightInLeft;

        InitMode(final boolean rightInLeft, final boolean fillLeft, final boolean fillRight) {
            this.rightInLeft = rightInLeft;
            this.fillLeft = fillLeft;
            this.fillRight = fillRight;
        }
    }
}
