package de.team33.cmd.files.job;

import de.team33.patterns.enums.pan.Values;
import de.team33.patterns.io.adrastea.FileEntry;

import java.util.function.Predicate;

public enum FileType {

    REGULAR(FileEntry::isRegularFile),
    DIRECTORY(FileEntry::isDirectory),
    SPECIAL(FileEntry::isSpecialFile),
    SYMBOLIC_LINK(FileEntry::isSymbolicLink),
    MISSING(FileEntry::isMissing);

    private static final Values<FileType> VALUES = Values.of(FileType.class);

    private final Predicate<FileEntry> filter;

    FileType(final Predicate<FileEntry> filter) {
        this.filter = filter;
    }

    public static FileType of(final FileEntry entry) {
        return VALUES.findFirst(value -> value.filter.test(entry))
                     .orElse(MISSING);
    }
}
