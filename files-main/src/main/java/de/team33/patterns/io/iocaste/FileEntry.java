package de.team33.patterns.io.iocaste;

import de.team33.patterns.decision.thyone.Choices;
import de.team33.patterns.lazy.narvi.Lazy;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import static de.team33.patterns.io.iocaste.LinkAttributes.effective;
import static de.team33.patterns.io.iocaste.LinkHandling.ORIGINAL;
import static de.team33.patterns.io.iocaste.LinkHandling.RESOLVE;
import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.Logger.Level.WARNING;

/**
 * Represents an entry from an imaginary file index.
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
@SuppressWarnings("unused")
public class FileEntry {

    private final Path path;
    private final Lazy<BasicFileAttributes> lazyAttributes;
    private final Lazy<FileType> lazyType;

    private FileEntry(final Path path, final Normality normality, final LinkHandling linkHandling) {
        this.path = normality.apply(path);
        this.lazyAttributes = Lazy.init(() -> newAttributes(linkHandling));
        this.lazyType = Lazy.init(() -> FileType.of(attributes()));
    }

    private static BasicFileAttributes basicAttributes(final Path path, final LinkHandling handling) {
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

    private static FileEntry ofDefinite(final Path path, final LinkHandling linkHandling) {
        return new FileEntry(path, Normality.DEFINITE, linkHandling);
    }

    /**
     * Returns a new {@link Lister} based on a given {@link LinkHandling}
     * that applies a default path order (by file name).
     */
    public static Lister lister(final LinkHandling linkHandling) {
        return new Lister(linkHandling, Util.PATH_ORDER, Util.NO_ORDER);
    }

    /**
     * Returns a new {@link DirectoryStreamer} based on a given {@link LinkHandling}
     * that does not skip any entry.
     */
    public static DirectoryStreamer streamer(final LinkHandling linkHandling) {
        return streamer(lister(linkHandling));
    }

    /**
     * Returns a new {@link DirectoryStreamer} based on a given {@link Lister}
     * that does not skip any entry.
     */
    public static DirectoryStreamer streamer(final Lister lister) {
        return new DirectoryStreamer(lister, null);
    }

    private BasicFileAttributes newAttributes(final LinkHandling handling) {
        final BasicFileAttributes original = basicAttributes(path, ORIGINAL);
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
            return new LinkAttributes(handling, basicAttributes(path, handling));
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
     * Returns the {@link FileType} of <em>this</em> {@link FileEntry}.
     */
    public final FileType type() {
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

    public record Problem(FileEntry entry, IOException cause) {

        private static final System.Logger LOGGER = System.getLogger(Problem.class.getCanonicalName());
        private static final String MESSAGE = "Cannot access file entry ...%n" +
                                              "    path:      <%s>%n" +
                                              "    exception: <%s>%n" +
                                              "    message:   '%s'%n";

        final void log() {
            final Lazy<String> lazyMessage = Lazy.init(() -> MESSAGE.formatted(entry.path(),
                                                                               cause.getClass().getCanonicalName(),
                                                                               cause.getMessage()));
            LOGGER.log(WARNING, lazyMessage);
            LOGGER.log(DEBUG, lazyMessage, cause());
        }
    }

    /**
     * A tool that serves to list the immediate contents of any file represented by a
     * {@link Path} or {@link FileEntry}.
     */
    public static final class Lister {

        private static final Choices<Lister> CHOICES = Choices.parallel(Lister::isPathOrder, Lister::isEntryOrder);

        private final LinkHandling linkHandling;
        private final Comparator<? super Path> pathOrder;
        private final Comparator<? super FileEntry> entryOrder;
        private final Lazy<Function<Stream<Path>, Stream<FileEntry>>> mapping;

        private Lister(final LinkHandling linkHandling,
                       final Comparator<? super Path> pathOrder,
                       final Comparator<? super FileEntry> entryOrder) {
            this.linkHandling = linkHandling;
            this.pathOrder = pathOrder;
            this.entryOrder = entryOrder;
            this.mapping = Lazy.init(this::newMapping);
        }

        private FileEntry entryOfDefinite(final Path path) {
            return ofDefinite(path, linkHandling);
        }

        private FileEntry entryOf(final Path path) {
            return of(path, linkHandling);
        }

        private Function<Stream<Path>, Stream<FileEntry>> newMapping() {
            return switch (CHOICES.apply(this)) {
                case 0b11 -> paths -> paths.sorted(pathOrder)
                                           .map(this::entryOfDefinite)
                                           .sorted(entryOrder);
                case 0b10 -> paths -> paths.sorted(pathOrder)
                                           .map(this::entryOfDefinite);
                case 0b01 -> paths -> paths.map(this::entryOfDefinite)
                                           .sorted(entryOrder);
                default -> paths -> paths.map(this::entryOfDefinite);
            };
        }

        private boolean isPathOrder() {
            return Util.NO_ORDER != pathOrder;
        }

        private boolean isEntryOrder() {
            return Util.NO_ORDER != entryOrder;
        }

        LinkHandling linkHandling() {
            return linkHandling;
        }

        /**
         * Returns an instance that corresponds to <em>this</em> {@link Lister} but resolves symbolic links.
         * Returns <em>this</em> {@link Lister} if it already resolves symbolic links.
         *
         * @see FileEntry#lister(LinkHandling)
         */
        public final Lister resolved() {
            return (RESOLVE == linkHandling) ? this : new Lister(RESOLVE, pathOrder, entryOrder);
        }

        /**
         * Returns an instance that corresponds to <em>this</em> {@link Lister} but handles original symbolic links.
         * Returns <em>this</em> {@link Lister} if it already handles original symbolic links.
         *
         * @see FileEntry#lister(LinkHandling)
         */
        public final Lister original() {
            return (ORIGINAL == linkHandling) ? this : new Lister(ORIGINAL, pathOrder, entryOrder);
        }

        /**
         * Returns a {@link List} of the immediate contents of a given <em>path</em> from a directory structure.
         * <p>
         * Returns an empty {@link List} if the given <em>path</em> does not represent a directory
         * and thus cannot have any directory contents.
         * <p>
         * Also returns an empty {@link List} if the given <em>path</em> refuses access to its contents
         * and throws an exception. In that case, the problem will be logged to a {@link System.Logger}.
         * <p>
         * NOTE: an original {@link FileEntry} will be created from the given <em>path</em> using the associated
         * {@link LinkHandling}. If this does not meet your requirements, use {@link #list(FileEntry)} instead.
         *
         * @see #list(FileEntry)
         * @see #list(Path, Consumer)
         * @see #list(FileEntry, Consumer)
         */
        public final List<FileEntry> list(final Path path) {
            return list(entryOf(path));
        }

        /**
         * Returns a {@link List} of the immediate contents of a given <em>entry</em> from a directory structure.
         * <p>
         * Returns an empty {@link List} if the given <em>entry</em> does not represent a directory
         * and thus cannot have any directory contents.
         * <p>
         * Also returns an empty {@link List} if the given <em>entry</em> refuses access to its contents
         * and thus throws an exception. In that case, the problem will be logged to a {@link System.Logger}.
         *
         * @see #list(Path)
         * @see #list(Path, Consumer)
         * @see #list(FileEntry, Consumer)
         */
        public final List<FileEntry> list(final FileEntry entry) {
            return list(entry, Problem::log);
        }

        /**
         * Returns a {@link List} of the immediate contents of a given <em>path</em> from a directory structure.
         * <p>
         * Returns an empty {@link List} if the given <em>path</em> does not represent a directory
         * and thus cannot have any directory contents.
         * <p>
         * Also returns an empty {@link List} if the given <em>path</em> refuses access to its contents
         * and thus throws an exception. In that case, a corresponding {@link Problem} will be reported
         * to the given {@link Consumer}.
         * <p>
         * NOTE: an original {@link FileEntry} will be created from the given <em>path</em> using the associated
         * {@link LinkHandling}. If this does not meet your requirements, use {@link #list(FileEntry, Consumer)}
         * instead.
         *
         * @see #list(FileEntry, Consumer)
         * @see #list(Path)
         * @see #list(FileEntry)
         */
        public final List<FileEntry> list(final Path path, final Consumer<? super Problem> onProblem) {
            return list(entryOf(path), onProblem);
        }

        /**
         * Returns a {@link List} of the immediate contents of a given <em>entry</em> from a directory structure.
         * <p>
         * Returns an empty {@link List} if the given <em>entry</em> does not represent a directory
         * and thus cannot have any directory contents.
         * <p>
         * Also returns an empty {@link List} if the given <em>entry</em> refuses access to its contents
         * and throws an exception. In that case, a corresponding {@link Problem} will be reported
         * to the given {@link Consumer}.
         *
         * @see #list(Path, Consumer)
         * @see #list(FileEntry)
         * @see #list(Path)
         */
        public final List<FileEntry> list(final FileEntry entry, final Consumer<? super Problem> onProblem) {
            if (entry.isDirectory()) {
                try (final Stream<Path> paths = Files.list(entry.path())) {
                    return mapping.get().apply(paths).toList();
                } catch (final IOException caught) {
                    onProblem.accept(new Problem(entry, caught));
                }
            }
            return List.of();
        }

        /**
         * Returns a copy of <em>this</em> {@link Lister}, with no order applied to the
         * {@linkplain #list(FileEntry, Consumer) listing}.
         */
        public final Lister noOrder() {
            return new Lister(linkHandling, Util.NO_ORDER, Util.NO_ORDER);
        }

        /**
         * Returns a copy of <em>this</em> {@link Lister}, with the given path order applied to the
         * {@linkplain #list(FileEntry, Consumer) listing}.
         */
        public final Lister pathOrder(final Comparator<? super Path> order) {
            return new Lister(linkHandling, order, entryOrder);
        }

        /**
         * Returns a copy of <em>this</em> {@link Lister}, with the given entry order applied to the
         * {@linkplain #list(FileEntry, Consumer) listing}.
         */
        public final Lister entryOrder(final Comparator<? super FileEntry> order) {
            return new Lister(linkHandling, pathOrder, order);
        }
    }

}
