package de.team33.patterns.io.adrastea.publics;

import de.team33.patterns.exceptional.dione.XConsumer;
import de.team33.patterns.io.adrastea.FileEntry;
import de.team33.patterns.io.adrastea.TUtil;
import de.team33.testing.io.hydra.ZipIO;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static de.team33.patterns.io.adrastea.LinkHandling.ORIGINAL;
import static de.team33.patterns.io.adrastea.LinkHandling.RESOLVE;
import static org.junit.jupiter.api.Assertions.*;

class FileEntryTest {

    private static final String CLASS_NAME = FileEntryTest.class.getSimpleName();
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

    FileEntryTest() throws IOException {
        Files.createDirectories(testPath);
        ZipIO.unzip(getClass(), "../files.zip", testPath);
        Files.createSymbolicLink(missingLink, missingFile.toAbsolutePath().normalize());
        Files.createSymbolicLink(dirLink, directory.toAbsolutePath().normalize());
        Files.createSymbolicLink(regularLink, regularFile.toAbsolutePath().normalize());
        Files.createSymbolicLink(specialLink, DEV_NULL);
        Files.createSymbolicLink(linkLink, regularLink.toAbsolutePath().normalize());
    }

    private static String nameOf(final Path path) {
        return Optional.ofNullable(path.getFileName()).orElse(path).toString();
    }

    private static BasicFileAttributes readAttributes(final Path path, final LinkOption[] options) {
        try {
            return Files.readAttributes(path, BasicFileAttributes.class, options);
        } catch (IOException e) {
            return TUtil.MISSING_FILE_ATTRIBUTES;
        }
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
    final void path() {
        paths().forEach(path -> {
            final FileEntry entry = FileEntry.of(path, ORIGINAL);
            assertTrue(entry.path().isAbsolute());
        });
    }

    @Test
    final void name() {
        paths().forEach(path -> {
            final FileEntry entry = FileEntry.of(path, ORIGINAL);
            assertEquals(nameOf(path), entry.name());
        });
    }

    @Test
    final void testToString() {
        paths().forEach(path -> {
            final FileEntry entry = FileEntry.of(path, ORIGINAL);
            assertEquals(path.toAbsolutePath().normalize().toString(), entry.toString());
        });
    }

    @Test
    final void isDirectory() {
        paths().forEach(path -> {
            final FileEntry entry = FileEntry.of(path, RESOLVE);
            assertEquals(Files.isDirectory(path), entry.isDirectory());
        });
    }

    @Test
    final void isRegularFile() {
        paths().forEach(path -> {
            final FileEntry entry = FileEntry.of(path, RESOLVE);
            assertEquals(Files.isRegularFile(path), entry.isRegularFile());
        });
    }

    @Test
    final void isSymbolicLink() {
        paths().forEach(path -> {
            final FileEntry entry = FileEntry.of(path, ORIGINAL);
            assertEquals(Files.isSymbolicLink(path), entry.isSymbolicLink());
        });
    }

    @Test
    final void isSpecialFile() {
        for (final Path path : paths()) {
            // System.out.println(path);
            final boolean expected = readAttributes(path, TUtil.RESOLVE_LINKS).isOther();
            final FileEntry entry = FileEntry.of(path, RESOLVE);
            assertEquals(expected, entry.isSpecialFile());
        }
    }

    @Test
    final void isMissing() {
        paths().forEach(path -> {
            // System.out.println(path);
            final FileEntry entry = FileEntry.of(path, RESOLVE);
            assertEquals(!Files.exists(path, TUtil.RESOLVE_LINKS), entry.isMissing());
        });
    }

    @Test
    final void isPresent() {
        paths().forEach(path -> {
            // System.out.println(path);
            final FileEntry entry = FileEntry.of(path, ORIGINAL);
            assertEquals(Files.exists(path, TUtil.ORIGINAL_LINKS), entry.isPresent());
        });
    }

    @Test
    final void lastModified() {
        for (final Path path : paths()) {
            // System.out.println(path);
            final FileEntry entry = FileEntry.of(path, RESOLVE);
            try {
                final Instant result = entry.lastModified();
                assertFalse(entry.isMissing());
                final Instant expected = readAttributes(path, TUtil.RESOLVE_LINKS).lastModifiedTime()
                                                                                  .toInstant();
                assertEquals(expected, result);
            } catch (final UnsupportedOperationException caught) {
                assertTrue(entry.isMissing());
                assertEquals(entry.isSymbolicLink(), entry.isPresent());
            }
        }
    }

    @Test
    final void lastAccess() {
        for (final Path path : paths()) {
            // System.out.println(path);
            final FileEntry entry = FileEntry.of(path, RESOLVE);
            try {
                final Instant result = entry.lastAccess();
                assertFalse(entry.isMissing());
                final Instant expected = readAttributes(path, TUtil.RESOLVE_LINKS).lastAccessTime()
                                                                                  .toInstant();
                assertEquals(expected, result);
            } catch (final UnsupportedOperationException caught) {
                assertTrue(entry.isMissing());
                assertEquals(entry.isSymbolicLink(), entry.isPresent());
            }
        }
    }

    @Test
    final void creation() {
        for (final Path path : paths()) {
            // System.out.println(path);
            final FileEntry entry = FileEntry.of(path, ORIGINAL);
            try {
                final Instant result = entry.creation();
                assertTrue(entry.isPresent());
                final Instant expected = readAttributes(path, TUtil.ORIGINAL_LINKS).creationTime()
                                                                                   .toInstant();
                assertEquals(expected, result);
            } catch (final UnsupportedOperationException caught) {
                assertTrue(entry.isMissing());
                assertFalse(entry.isSymbolicLink());
            }
        }
    }

    @Test
    final void size() throws IOException {
        for (final Path path : paths()) {
            final FileEntry entry = FileEntry.of(path, RESOLVE);
            if (entry.isMissing()) {
                assertEquals(0L, entry.size());
            } else {
                assertEquals(Files.size(path), entry.size());
            }
        }
    }

    @Test
    final void isOriginal() {
        paths().forEach(path -> {
            final FileEntry entry = FileEntry.original(path);
            assertTrue(entry.isOriginal());
            assertEquals(!Files.isSymbolicLink(path), entry.isResolved());
        });
    }

    @Test
    final void original() {
        paths().forEach(path -> {
            final FileEntry entry = FileEntry.of(path, RESOLVE).original();
            assertTrue(entry.isOriginal());
            assertEquals(!Files.isSymbolicLink(path), entry.isResolved());
        });
    }

    @Test
    final void isResolved() {
        paths().forEach(path -> {
            final FileEntry entry = FileEntry.resolved(path);
            assertTrue(entry.isResolved());
            assertEquals(!Files.isSymbolicLink(path), entry.isOriginal());
        });
    }

    @Test
    final void resolved() {
        paths().forEach(path -> {
            final FileEntry entry = FileEntry.of(path, ORIGINAL).resolved();
            assertTrue(entry.isResolved());
            assertEquals(!Files.isSymbolicLink(path), entry.isOriginal());
        });
    }
}