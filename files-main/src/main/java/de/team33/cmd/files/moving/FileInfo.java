package de.team33.cmd.files.moving;

import de.team33.cmd.files.common.HashId;
import de.team33.cmd.files.common.TimeId;
import de.team33.patterns.io.alpha.FileEntry;
import de.team33.patterns.lazy.narvi.Lazy;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneId;

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
    private final Lazy<String> timeId;

    FileInfo(final Path cwd, final FileEntry entry) {
        this.entry = entry;
        this.relativePath = cwd.relativize(entry.path().getParent());
        this.processingDir = cwd.getFileName();
        this.fullName = entry.name();
        this.dotIndex = fullName.lastIndexOf('.');

        this.fileName = Lazy.init(() -> (dotIndex < 0) ? fullName : fullName.substring(0, dotIndex));
        this.extension = Lazy.init(() -> (dotIndex < 0) ? "" : fullName.substring(dotIndex + 1));
        this.lastModified = Lazy.init(this::newLastModified);
        this.hash = Lazy.init(() -> HashId.valueOf(entry.path()));
        this.timeId = Lazy.init(() -> TimeId.valueOf(entry));
    }

    private LocalDateTime newLastModified() {
        return LocalDateTime.ofInstant(entry.lastModified(), ZoneId.systemDefault());
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
        return hash.get();
    }

    final String getTimeId() {
        return timeId.get();
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
