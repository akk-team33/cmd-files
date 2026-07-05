package de.team33.patterns.directories.iocaste.publics;

import de.team33.patterns.directories.iocaste.DirectoryLister;
import de.team33.patterns.directories.iocaste.FileEntry;
import de.team33.patterns.directories.iocaste.Problem;
import de.team33.patterns.directories.iocaste.TUtil;
import de.team33.testing.io.hydra.ZipIO;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static de.team33.patterns.directories.iocaste.LinkHandling.ORIGINAL;
import static de.team33.patterns.directories.iocaste.LinkHandling.RESOLVE;
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
            final List<Problem> problems = new LinkedList<>();
            final FileEntry entry = FileEntry.of(path, RESOLVE);
            final DirectoryLister lister = DirectoryLister.RESOLVING;

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

        final List<String> result = DirectoryLister.RESOLVING.list(testPath)
                                                             .stream()
                                                             .map(FileEntry::name)
                                                             .toList();

        assertNotEquals(unexpected, result);
        assertEquals(expected, Set.copyOf(result));
    }

    private Set<String> names(final DirectoryLister lister) {
        return lister.list(testPath)
                     .stream()
                     .map(FileEntry::name)
                     .collect(Collectors.toSet());
    }

    @Test
    final void pathFilter() {
        assertEquals(Set.of(), names(DirectoryLister.DEFAULT.pathFilter(any -> false)));
    }

    @Test
    final void entryFilter() {
        assertEquals(Set.of(), names(DirectoryLister.DEFAULT.entryFilter(any -> false)));
    }

    @Test
    final void noFilter() {
        final Set<String> expected = names(DirectoryLister.DEFAULT);
        final DirectoryLister stage = DirectoryLister.DEFAULT.pathFilter(any -> false);
        final Set<String> result = names(stage.noFilter());
        assertEquals(expected, result);
    }

    @Test
    final void list_maxOrder() {
        final List<String> expected = List.of(
                "special.link", "regular.link", "missing.link", "link.link", "directory.link", "de");
        final List<Problem> problems = new LinkedList<>();
        final FileEntry entry = FileEntry.of(testPath, ORIGINAL);
        final DirectoryLister lister = DirectoryLister.DEFAULT
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
        final List<Problem> problems = new LinkedList<>();
        final FileEntry entry = FileEntry.of(testPath, ORIGINAL);
        final DirectoryLister lister = DirectoryLister.DEFAULT
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
        final List<Problem> problems = new LinkedList<>();
        final FileEntry entry = FileEntry.of(testPath, RESOLVE);
        final DirectoryLister lister = DirectoryLister.RESOLVING
                .noOrder()
                .entryOrder(comparing(FileEntry::name).reversed());

        final List<String> result = lister.list(entry, problems::add)
                                          .stream()
                                          .map(FileEntry::name)
                                          .toList();

        assertEquals(expected, result);
        assertTrue(problems.isEmpty());
    }
}