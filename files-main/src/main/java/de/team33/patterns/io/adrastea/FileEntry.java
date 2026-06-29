package de.team33.patterns.io.adrastea;

import de.team33.patterns.enums.pan.Values;
import de.team33.patterns.lazy.narvi.Lazy;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Predicate;

import static de.team33.patterns.io.adrastea.LinkHandling.ORIGINAL;
import static de.team33.patterns.io.adrastea.LinkHandling.RESOLVE;

/**
 * Represents a directory entry.
 * Includes some meta information about a file, particularly the file system path, file type, size,
 * and some timestamps.
 * <p>
 * Strictly speaking, the meta information only applies to the moment of instantiation.
 * Therefore, an instance should be short-lived. The longer an instance "lives", the more likely it is
 * that the meta information is out of date because the underlying file may have been changed in the meantime.
 * <p>
 * Use {@link #of(Path, LinkHandling)}, {@link #original(Path)} or {@link #resolved(Path)}
 * to get a new instance.
 */
public class FileEntry {

    private final Path path;
    private final Lazy<BasicFileAttributes> lazyAttributes;
    private final Lazy<Type> lazyType;

    private FileEntry(final Path path, final Normality normality, final LinkHandling linkHandling) {
        this.path = normality.apply(path);
        this.lazyAttributes = Lazy.init(() -> newAttributes(linkHandling));
        this.lazyType = Lazy.init(() -> Type.of(this));
    }

    private static BasicFileAttributes newAttributes(final Path path, final LinkHandling handling) {
        try {
            return Files.readAttributes(path, BasicFileAttributes.class, handling.options());
        } catch (final IOException ignored) {
            return Util.MISSING_FILE_ATTRIBUTES;
        }
    }

    /**
     * Returns a new {@link FileEntry} based on a given {@link Path} and a given {@link LinkHandling}.
     */
    public static FileEntry of(final Path path, final LinkHandling linkHandling) {
        return new FileEntry(path, Normality.UNKNOWN, linkHandling);
    }

    /**
     * Returns a new {@link FileEntry} based on a given {@link Path} that {@link #isOriginal()}.
     *
     * @see #of(Path, LinkHandling)
     * @see LinkHandling#ORIGINAL
     */
    public static FileEntry original(final Path path) {
        return of(path, ORIGINAL);
    }

    /**
     * Returns a new {@link FileEntry} based on a given {@link Path} that {@link #isResolved()}.
     *
     * @see #of(Path, LinkHandling)
     * @see LinkHandling#RESOLVE
     */
    public static FileEntry resolved(final Path path) {
        return of(path, RESOLVE);
    }

    static FileEntry ofDefinite(final Path path, final LinkHandling linkHandling) {
        return new FileEntry(path, Normality.DEFINITE, linkHandling);
    }

    private static BasicFileAttributes effective(final BasicFileAttributes attributes) {
        return (attributes instanceof LinkAttributes linkAttributes) ? linkAttributes.backing() : attributes;
    }

    private BasicFileAttributes newAttributes(final LinkHandling handling) {
        final BasicFileAttributes original = newAttributes(path, ORIGINAL);
        if (original.isSymbolicLink()) {
            return newLinkAttributes(handling, original);
        } else {
            return original;
        }
    }

    private LinkAttributes newLinkAttributes(final LinkHandling handling, final BasicFileAttributes original) {
        if (ORIGINAL == handling) {
            return new LinkAttributes(ORIGINAL, original);
        } else {
            return new LinkAttributes(handling, newAttributes(path, handling));
        }
    }

    private BasicFileAttributes attributes() {
        return lazyAttributes.get();
    }

    /**
     * Returns the file system path of the represented file as an
     * {@linkplain Path#toAbsolutePath() absolute} {@linkplain Path#normalize() normalized} {@link Path}.
     */
    public final Path path() {
        return path;
    }

    /**
     * Returns the simple name of the represented file.
     */
    public final String name() {
        return Optional.ofNullable(path.getFileName()).orElse(path).toString();
    }

    /**
     * Returns a {@link FileEntry} based on <em>this</em>' {@link #path()} that definitely {@link #isOriginal()}.
     */
    public final FileEntry original() {
        return isOriginal() ? this : new FileEntry(path, Normality.DEFINITE, ORIGINAL);
    }

    /**
     * Returns a {@link FileEntry} based on <em>this</em>' {@link #path()} that definitely {@link #isResolved()}.
     */
    public final FileEntry resolved() {
        return isResolved() ? this : new FileEntry(path, Normality.DEFINITE, RESOLVE);
    }

    /**
     * Returns the {@link Type} of <em>this</em> {@link FileEntry}.
     */
    public final Type type() {
        return lazyType.get();
    }

    /**
     * Determines whether <em>this</em> {@link FileEntry} exposes its original attributes,
     * even if it {@linkplain #isSymbolicLink() is a symbolic link}.
     *
     * @see #isResolved()
     */
    public final boolean isOriginal() {
        if (attributes() instanceof LinkAttributes linkAttributes) {
            return ORIGINAL == linkAttributes.handling();
        } else {
            return true;
        }
    }

    /**
     * Determines whether <em>this</em> {@link FileEntry} resolves its final attributes,
     * even if it {@linkplain #isSymbolicLink() is a symbolic link}.
     *
     * @see #isOriginal()
     */
    public final boolean isResolved() {
        if (attributes() instanceof LinkAttributes linkAttributes) {
            return RESOLVE == linkAttributes.handling();
        } else {
            return true;
        }
    }

    /**
     * Determines if the represented file is a directory.
     * <p>
     * This may also be the case if it {@link #isSymbolicLink()} and {@link #isResolved()}.
     */
    public final boolean isDirectory() {
        return attributes().isDirectory();
    }

    /**
     * Determines if the represented file is a regular file.
     * <p>
     * This may also be the case if it {@link #isSymbolicLink()} and {@link #isResolved()}.
     */
    public final boolean isRegularFile() {
        return attributes().isRegularFile();
    }

    /**
     * Determines if the represented file is a special file (typically, a <em>device</em>).
     * <p>
     * This may also be the case if it {@link #isSymbolicLink()} and {@link #isResolved()}.
     */
    public final boolean isSpecialFile() {
        return attributes().isOther();
    }

    /**
     * Determines if the represented file is a symbolic link.
     * <p>
     * No matter if it {@link #isOriginal()} or {@link #isResolved()}.
     */
    public final boolean isSymbolicLink() {
        return attributes().isSymbolicLink();
    }

    /**
     * Determines if the represented file is missing.
     * <p>
     * This may also be the case if it {@link #isSymbolicLink()} and {@link #isResolved()}.
     * <p>
     * <b>NOTE</b> that in this case, it also {@link #isPresent()}!
     */
    public final boolean isMissing() {
        return effective(attributes()) == Util.MISSING_FILE_ATTRIBUTES;
    }

    /**
     * Determines if the represented file is present.
     * <p>
     * That is always the case if {@link #isRegularFile()}, {@link #isDirectory()}, {@link #isSpecialFile()}
     * or {@link #isSymbolicLink()}, and therefore especially if a (resolved) symbolic link {@link #isMissing()}!
     */
    public final boolean isPresent() {
        return attributes() != Util.MISSING_FILE_ATTRIBUTES;
    }

    /**
     * Returns the timestamp of the last modification of the represented file as an {@link Instant}.
     *
     * @throws UnsupportedOperationException if <em>this</em> {@link #isMissing()}
     */
    public final Instant lastModified() {
        return attributes().lastModifiedTime().toInstant();
    }

    /**
     * Returns the timestamp of the last access to the represented file as an {@link Instant}.
     *
     * @throws UnsupportedOperationException if <em>this</em> {@link #isMissing()}
     */
    public final Instant lastAccess() {
        return attributes().lastAccessTime().toInstant();
    }

    /**
     * Returns the timestamp of the creation of the represented file as an {@link Instant}.
     *
     * @throws UnsupportedOperationException if <em>this</em> {@link #isMissing()}
     */
    public final Instant creation() {
        return attributes().creationTime().toInstant();
    }

    /**
     * Returns the size of the represented file.
     * <p>
     * If <em>this</em> {@link #isMissing()} returns {@code 0L}.
     */
    public final long size() {
        return attributes().size();
    }

    @Override
    public final String toString() {
        return path.toString();
    }

    /**
     * Symbolizes possible types of a file represented by a {@link FileEntry}.
     */
    public enum Type {

        /**
         * Symbolizes a regular file
         * (maybe a resolved symbolic link if it is not {@linkplain FileEntry#isOriginal() original}).
         */
        REGULAR_FILE(FileEntry::isRegularFile),

        /**
         * Symbolizes a directory
         * (maybe a resolved symbolic link if it is not {@linkplain FileEntry#isOriginal() original}).
         */
        DIRECTORY(FileEntry::isDirectory),

        /**
         * Symbolizes a special file (typically, a <em>device</em>)
         * (maybe a resolved symbolic link if it is not {@linkplain FileEntry#isOriginal() original}).
         */
        SPECIAL_FILE(FileEntry::isSpecialFile),

        /**
         * Symbolizes a symbolic link (if it is not {@linkplain FileEntry#isResolved() resolved}).
         */
        SYMBOLIC_LINK(Util.and(FileEntry::isOriginal, FileEntry::isSymbolicLink)),

        /**
         * Symbolizes a missing file
         * (maybe a resolved symbolic link if it is not {@linkplain FileEntry#isOriginal() original}).
         */
        MISSING(FileEntry::isMissing);

        private static final Values<Type> VALUES = Values.of(Type.class);
        private static final String UNKNOWN_TYPE = "Unknown type: <%s>";

        private final Predicate<FileEntry> predicate;

        Type(final Predicate<FileEntry> predicate) {
            this.predicate = predicate;
        }

        private static Type of(final FileEntry entry) {
            return VALUES.findFirst(type -> type.predicate.test(entry))
                         .orElseThrow(() -> new NoSuchElementException(UNKNOWN_TYPE.formatted(entry)));
        }
    }
}
