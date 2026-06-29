package de.team33.patterns.io.adrastea.publics;

import de.team33.patterns.exceptional.dione.XConsumer;
import de.team33.patterns.io.adrastea.Directory;
import de.team33.patterns.io.adrastea.FileEntry;
import de.team33.patterns.io.adrastea.TUtil;
import de.team33.testing.io.hydra.ZipIO;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static de.team33.patterns.io.adrastea.LinkHandling.ORIGINAL;
import static de.team33.patterns.io.adrastea.LinkHandling.RESOLVE;
import static java.util.Comparator.comparing;
import static org.junit.jupiter.api.Assertions.*;

class DirectoryListerTest {

    private static final String CLASS_NAME = DirectoryListerTest.class.getSimpleName();
    @SuppressWarnings("HardcodedFileSeparator")
    private static final Path TEST_PATH = Path.of("target", "testing", CLASS_NAME);
    @SuppressWarnings("HardcodedFileSeparator")
    private static final Path DEV_NULL = Paths.get("/dev/null"); // special file
    @SuppressWarnings("HardcodedFileSeparator")
    private static final Path ROOT_HOME = Paths.get("/root"); // unreadable directory (Linux)
    @SuppressWarnings("HardcodedFileSeparator")
    private static final Path ROOT = Paths.get("/"); // root directory

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

    DirectoryListerTest() throws IOException {
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

    final List<Path> paths() {
        return List.of(
                missingFile,
                directory,
                regularFile,
                missingLink,
                dirLink,
                regularLink,
                specialLink,
                linkLink,
                DEV_NULL,
                ROOT_HOME,
                ROOT);
    }

    @Test
    final void list() {
        for (final Path path : paths()) {
            final List<Directory.Problem> problems = new LinkedList<>();
            final FileEntry entry = FileEntry.of(path, RESOLVE);
            final Directory.Lister lister = Directory.lister(RESOLVE);

            final List<FileEntry> result = lister.list(path, problems::add);

            assertNotEquals(entry.isDirectory(), result.isEmpty());
            assertTrue(problems.isEmpty());
        }
    }

    @Test
    final void list_noOrder() {
        // alphabetic order ...
        final List<String> unexpected = List.of(
                "directory.link", "link.link", "missing.link", "regular.link", "special.link", "de");
        // no order ...
        final Set<String> expected = Set.copyOf(unexpected);

        final Directory.Lister lister = Directory.lister(RESOLVE).noOrder();

        final List<String> result = lister.list(testPath)
                                          .stream()
                                          .map(FileEntry::name)
                                          .toList();

        assertNotEquals(unexpected, result);
        assertEquals(expected, Set.copyOf(result));
    }

    @Test
    final void list_maxOrder() {
        final List<String> expected = List.of(
                "special.link", "regular.link", "missing.link", "link.link", "directory.link", "de");
        final List<Directory.Problem> problems = new LinkedList<>();
        final FileEntry entry = FileEntry.of(testPath, ORIGINAL);
        final Directory.Lister lister = Directory.lister(ORIGINAL)
                                                 .entryOrder(comparing(FileEntry::name).reversed());

        final List<String> result = lister.list(entry, problems::add)
                                          .stream()
                                          .map(FileEntry::name)
                                          .toList();

        assertEquals(expected, result);
        assertTrue(problems.isEmpty());
    }

    @Test
    final void list_pathOrder() {
        final List<String> expected = List.of(
                "special.link", "regular.link", "missing.link", "link.link", "directory.link", "de");
        final List<Directory.Problem> problems = new LinkedList<>();
        final FileEntry entry = FileEntry.of(testPath, ORIGINAL);
        final Directory.Lister lister = Directory.lister(ORIGINAL)
                                                 .pathOrder(TUtil.PATH_ORDER.reversed());

        final List<String> result = lister.list(entry, problems::add)
                                          .stream()
                                          .map(FileEntry::name)
                                          .toList();

        assertEquals(expected, result);
        assertTrue(problems.isEmpty());
    }

    @Test
    final void list_entryOrder() {
        final List<String> expected = List.of(
                "special.link", "regular.link", "missing.link", "link.link", "directory.link", "de");
        final List<Directory.Problem> problems = new LinkedList<>();
        final FileEntry entry = FileEntry.of(testPath, RESOLVE);
        final Directory.Lister lister = Directory.lister(RESOLVE)
                                                 .noOrder()
                                                 .entryOrder(comparing(FileEntry::name).reversed());

        final List<String> result = lister.list(entry, problems::add)
                                          .stream()
                                          .map(FileEntry::name)
                                          .toList();

        assertEquals(expected, result);
        assertTrue(problems.isEmpty());
    }

    @Test
    final void stream() {
        final List<String> expected = List.of(uuid, "de", "team33", "cmd", "files", "common", "Counter.java",
                                              "FileType.java", "HashId.java", "Output.java", "RequestException.java",
                                              "TimeId.java", "Main.java", "matching", "CaseSensitivity.java",
                                              "InternalException.java", "Method.java", "NameMatcher.java",
                                              "TypeMatcher.java", "WildcardString.java", "tools", "io", "Bytes.java",
                                              "FileHashing.java", "LazyHashing.java", "LazyTiming.java",
                                              "StrictHashing.java", "directory.link", "link.link", "missing.link",
                                              "regular.link", "special.link");
        final List<Directory.Problem> problems = new LinkedList<>();
        final Directory.Streamer streamer = Directory.streamer(ORIGINAL)
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
        final Directory.Streamer streamer = Directory.streamer(RESOLVE)
                                                     .skip(FileEntry::isDirectory);

        final List<FileEntry> result = streamer.stream(testPath)
                                               .toList();

        assertEquals(List.of(), result);
    }

    @Test
    final void stream_forbidden() throws IOException {
        final List<Directory.Problem> problems = new LinkedList<>();
        forbidden(testPath, path -> {
            final FileEntry entry = FileEntry.of(path, ORIGINAL);
            final Directory.Streamer streamer = Directory.streamer(ORIGINAL);

            final List<FileEntry> result = streamer.stream(entry, problems::add)
                                                   .toList();

            assertEquals(List.of(entry), result);
        });
        assertEquals(1, problems.size());
        assertEquals(testPath.toAbsolutePath().normalize(), problems.get(0).entry().path());
    }
}