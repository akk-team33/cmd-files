package de.team33.patterns.io.delta;

import de.team33.patterns.enums.pan.Values;

import java.util.EnumSet;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Symbolizes different file types
 */
public enum FileType {

    /**
     * Symbolizes a missing file.
     */
    MISSING(FileEntry::isMissing),

    /**
     * Symbolizes a regular file.
     */
    REGULAR(FileEntry::isRegularFile),

    /**
     * Symbolizes a directory.
     */
    DIRECTORY(FileEntry::isDirectory),

    /**
     * Symbolizes a symbolic link.
     */
    SYMBOLIC(FileEntry::isSymbolicLink),

    /**
     * Symbolizes a special file.
     */
    SPECIAL(FileEntry::isSpecialFile);

    private static final Values<FileType> VALUES = Values.of(FileType.class);
    private static final Supplier<EnumSet<FileType>> NEW_SET = () -> EnumSet.noneOf(FileType.class);

    private final Predicate<FileEntry> filter;

    FileType(final Predicate<FileEntry> filter) {
        this.filter = filter;
    }

    public static Set<FileType> of(final FileEntry entry) {
        return VALUES.findAll(value -> value.filter.test(entry))
                     .collect(NEW_SET, Set::add, Set::addAll);
    }
}
