package de.team33.cmd.files.main.moving;

import de.team33.patterns.lazy.narvi.Lazy;
import de.team33.tools.io.LazyHashing;
import de.team33.tools.io.StrictHashing;
import de.team33.tools.io.FileHashing;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

import static java.nio.file.LinkOption.*;

class FileInfo {

    private final Path path;
    private final Path relativePath;
    private final Path processingDir;
    private final String fullName;
    private final int dotIndex;
    private final Lazy<String> fileName;
    private final Lazy<String> extension;
    private final Lazy<Instant> lastModifiedInstant;
    private final Lazy<LocalDateTime> lastModified;
    private final Lazy<String> hash;
    private final FileHashing hashing = LazyHashing.of(StrictHashing.SHA_1);

    FileInfo(final Path cwd, final Path path) {
        assert path.isAbsolute();
        // - - - - - - - - - - - - - - - - - - - - - - - -
        this.path = path;
        this.relativePath = cwd.relativize(path.getParent());
        this.processingDir = cwd.getFileName();
        this.fullName = path.getFileName().toString();
        this.dotIndex = fullName.lastIndexOf('.');

        this.fileName = Lazy.init(() -> (dotIndex < 0) ? fullName : fullName.substring(0, dotIndex));
        this.extension = Lazy.init(() -> (dotIndex < 0) ? "" : fullName.substring(dotIndex + 1));
        this.lastModifiedInstant = Lazy.initEx(this::newLastModifiedInstant);
        this.lastModified = Lazy.initEx(this::newLastModified);
        this.hash = Lazy.init(this::newHash);
    }

    private String newHash() {
        return hashing.hash(path);
    }

    private Instant newLastModifiedInstant() throws IOException {
        return Files.getLastModifiedTime(path, NOFOLLOW_LINKS)
                    .toInstant();
    }

    private LocalDateTime newLastModified() throws IOException {
        return LocalDateTime.ofInstant(lastModifiedInstant.get(), ZoneId.systemDefault());
    }

    final String getTimestamp() {
        return String.format("[%x]", lastModifiedInstant.get().truncatedTo(ChronoUnit.SECONDS).toEpochMilli());
    }

    final String getLastModifiedYear() {
        return String.format("%04d", lastModified.get().getYear());
    }

    final String getLastModifiedMonth() {
        return String.format("%02d", lastModified.get().getMonthValue());
    }

    final String getLastModifiedDay() {
        return String.format("%02d", lastModified.get().getDayOfMonth());
    }

    final String getLastModifiedHour() {
        return String.format("%02d", lastModified.get().getHour());
    }

    final String getLastModifiedMinute() {
        return String.format("%02d", lastModified.get().getMinute());
    }

    final String getLastModifiedSecond() {
        return String.format("%02d", lastModified.get().getSecond());
    }

    final String getFullName() {
        return fullName;
    }

    final String getFileName() {
        return fileName.get();
    }

    final String getExtension() {
        return extension.get();
    }

    final String getExtensionLC() {
        return extension.get().toLowerCase();
    }

    final String getHash() {
        return "#" + hash.get();
    }

    final String getRelativePath() {
        return relativePath.toString();
    }

    final String getParentDir() {
        return relativePath.getFileName().toString();
    }

    final String getProcessingDir() {
        return processingDir.toString();
    }
}