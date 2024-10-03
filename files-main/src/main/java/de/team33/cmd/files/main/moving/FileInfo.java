package de.team33.cmd.files.main.moving;

import de.team33.patterns.io.alpha.FileEntry;
import de.team33.patterns.lazy.narvi.Lazy;
import de.team33.tools.io.FileDating;
import de.team33.tools.io.FileHashing;
import de.team33.tools.io.LazyDating;
import de.team33.tools.io.LazyHashing;
import de.team33.tools.io.StrictHashing;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

class FileInfo {

    private final FileEntry entry;
    private final Path relativePath;
    private final Path processingDir;
    private final String fullName;
    private final int dotIndex;
    private final Lazy<String> fileName;
    private final Lazy<String> extension;
    private final Lazy<LocalDateTime> lastModified;
    private final Lazy<String> hash;
    private final FileHashing hashing = LazyHashing.of(StrictHashing.SHA_1);
    private final FileDating dating = LazyDating.of("[");

    FileInfo(final Path cwd, final FileEntry entry) {
        this.entry = entry;
        this.relativePath = cwd.relativize(entry.path().getParent());
        this.processingDir = cwd.getFileName();
        this.fullName = entry.name();
        this.dotIndex = fullName.lastIndexOf('.');

        this.fileName = Lazy.init(() -> (dotIndex < 0) ? fullName : fullName.substring(0, dotIndex));
        this.extension = Lazy.init(() -> (dotIndex < 0) ? "" : fullName.substring(dotIndex + 1));
        this.lastModified = Lazy.init(this::newLastModified);
        this.hash = Lazy.init(this::newHash);
    }

    private String newHash() {
        return hashing.hash(entry.path());
    }

    private LocalDateTime newLastModified() {
        return LocalDateTime.ofInstant(entry.lastModified(), ZoneId.systemDefault());
    }

    final String getTimestamp() {
        return String.format("[%s]", dating.timestamp(entry));
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