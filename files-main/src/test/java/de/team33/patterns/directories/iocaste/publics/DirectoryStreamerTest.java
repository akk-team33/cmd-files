package de.team33.patterns.directories.iocaste.publics;

import de.team33.patterns.directories.iocaste.*;
import de.team33.patterns.exceptional.dione.XConsumer;
import de.team33.testing.io.hydra.ZipIO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static de.team33.patterns.directories.iocaste.LinkHandling.ORIGINAL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("FieldCanBeLocal")
class DirectoryStreamerTest {

    private static final String CLASS_NAME = DirectoryStreamerTest.class.getSimpleName();
    @SuppressWarnings("HardcodedFileSeparator")
    private static final Path TEST_PATH = Path.of("target", "testing", CLASS_NAME);
    @SuppressWarnings("HardcodedFileSeparator")
    private static final Path DEV_NULL = Paths.get("/dev/null"); // special file
    private static final DirectoryLister LISTER = DirectoryLister.DEFAULT.pathOrder(PathOrder.BY_NAME);

    private final String uuid = UUID.randomUUID().toString();
    private final Path testPath = TEST_PATH.resolve(uuid);
    private final Path missingLink = testPath.resolve("missing.link");
    private final Path dirLink = testPath.resolve("directory.link");
    private final Path regularLink = testPath.resolve("regular.link");
    private final Path specialLink = testPath.resolve("special.link");
    private final Path linkLink = testPath.resolve("link.link");
    private final Path missingFile = testPath.resolve("file/is/missing");
    private final Path directory = testPath.resolve("de/team33");
    private final Path regularFile = directory.resolve("cmd/files/Main.java");

    DirectoryStreamerTest() throws IOException {
        Files.createDirectories(testPath);
        ZipIO.unzip(getClass(), "../files.zip", testPath);
        Files.createSymbolicLink(missingLink, missingFile.toAbsolutePath().normalize());
        Files.createSymbolicLink(dirLink, directory.toAbsolutePath().normalize());
        Files.createSymbolicLink(regularLink, regularFile.toAbsolutePath().normalize());
        Files.createSymbolicLink(specialLink, DEV_NULL);
        Files.createSymbolicLink(linkLink, regularLink.toAbsolutePath().normalize());
    }

    private static <X extends Exception> void forbidden(final Path path, final XConsumer<Path, X> method)
            throws IOException, X {
        final Set<PosixFilePermission> permissions = Files.getPosixFilePermissions(path);
        Files.setPosixFilePermissions(path, Set.of());
        try {
            method.accept(path);
        } finally {
            Files.setPosixFilePermissions(path, permissions);
        }
    }

    @Test
    final void stream() {
        final List<String> expected = List.of(uuid, "de", "team33", "cmd", "files", "balancing", "cleaning",
                                              "common", "Counter.java", "FileType.java", "HashId.java", "Output.java",
                                              "RequestException.java", "TimeId.java", "job", "Main.java", "matching",
                                              "CaseSensitivity.java", "InternalException.java", "Method.java",
                                              "NameMatcher.java", "TypeMatcher.java", "WildcardString.java", "moving",
                                              "patterns", "tools", "io", "Bytes.java", "FileHashing.java",
                                              "LazyHashing.java", "LazyTiming.java", "StrictHashing.java",
                                              "directory.link", "link.link", "missing.link", "regular.link",
                                              "special.link");
        final List<Problem> problems = new LinkedList<>();
        final DirectoryStreamer streamer = DirectoryStreamer.basedOn(LISTER)
                                                            .skip(entry -> entry.path().endsWith("balancing"))
                                                            .skip(entry -> entry.path().endsWith("cleaning"))
                                                            .skip(entry -> entry.path().endsWith("job"))
                                                            .skip(entry -> entry.path().endsWith("moving"))
                                                            .skip(entry -> entry.path().endsWith("patterns"));

        final List<String> result = streamer.stream(testPath, problems::add)
                                            .map(FileEntry::name)
                                            .toList();

        assertEquals(expected, result);
        assertTrue(problems.isEmpty());
    }

    @Test
    final void stream_skip_origin() {
        final List<Path> expected = List.of(testPath.toAbsolutePath().normalize());
        final DirectoryStreamer streamer = DirectoryStreamer.RESOLVING
                .skip(FileEntry::isDirectory);

        final List<Path> result = streamer.stream(testPath)
                                          .map(FileEntry::path)
                                          .toList();

        assertEquals(expected, result);
    }

    @Test
    final void stream_forbidden() throws IOException {
        final List<Problem> problems = new LinkedList<>();
        forbidden(testPath, path -> {
            final FileEntry entry = FileEntry.of(path, ORIGINAL);
            final DirectoryStreamer streamer = DirectoryStreamer.DEFAULT;

            final List<FileEntry> result = streamer.stream(entry, problems::add)
                                                   .toList();

            assertEquals(List.of(entry), result);
        });
        assertEquals(1, problems.size());
        assertEquals(testPath.toAbsolutePath().normalize(), problems.get(0).entry().path());
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 6039})
    final void start_limit(final int level) {
        final List<FileEntry> result = DirectoryStreamer.DEFAULT.start(level)
                                                                .limit(level)
                                                                .stream(testPath)
                                                                .toList();
        assertEquals(List.of(), result);
    }
}