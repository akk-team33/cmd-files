package de.team33.patterns.io.iocaste;

import de.team33.patterns.enums.pan.Values;

import java.nio.file.attribute.BasicFileAttributes;
import java.util.NoSuchElementException;
import java.util.function.Predicate;

/**
 * Symbolizes possible types of a file represented by a {@link FileEntry}.
 */
public enum FileType {

    /**
     * Symbolizes a regular file
     * (maybe a resolved symbolic link if it is not {@linkplain FileEntry#isOriginal() original}).
     */
    REGULAR_FILE(BasicFileAttributes::isRegularFile),

    /**
     * Symbolizes a directory
     * (maybe a resolved symbolic link if it is not {@linkplain FileEntry#isOriginal() original}).
     */
    DIRECTORY(BasicFileAttributes::isDirectory),

    /**
     * Symbolizes a special file (typically, a <em>device</em>)
     * (maybe a resolved symbolic link if it is not {@linkplain FileEntry#isOriginal() original}).
     */
    SPECIAL_FILE(BasicFileAttributes::isOther),

    /**
     * Symbolizes a symbolic link (if it is not {@linkplain FileEntry#isResolved() resolved}).
     */
    SYMBOLIC_LINK(attributes -> LinkAttributes.effective(attributes).isSymbolicLink()),

    /**
     * Symbolizes a missing file
     * (maybe a resolved symbolic link if it is not {@linkplain FileEntry#isOriginal() original}).
     */
    MISSING(attributes -> LinkAttributes.effective(attributes) == Util.MISSING_FILE_ATTRIBUTES);

    private static final Values<FileType> VALUES = Values.of(FileType.class);
    private static final String UNKNOWN_TYPE = "Unknown type: <%s>";

    private final Predicate<BasicFileAttributes> predicate;

    FileType(final Predicate<BasicFileAttributes> predicate) {
        this.predicate = predicate;
    }

    static FileType of(final BasicFileAttributes attributes) {
        return VALUES.findFirst(type -> type.predicate.test(attributes))
                     .orElseThrow(() -> new NoSuchElementException(UNKNOWN_TYPE.formatted(attributes)));
    }
}
