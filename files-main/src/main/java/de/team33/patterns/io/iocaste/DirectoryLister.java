package de.team33.patterns.io.iocaste;

import de.team33.patterns.decision.thyone.Choices;
import de.team33.patterns.lazy.narvi.Lazy;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import static de.team33.patterns.io.iocaste.LinkHandling.ORIGINAL;
import static de.team33.patterns.io.iocaste.LinkHandling.RESOLVE;

/**
 * A tool that serves to list the immediate contents of any file represented by a
 * {@link Path} or {@link FileEntry}.
 */
public final class DirectoryLister {

    private static final Choices<DirectoryLister> CHOICES = Choices.parallel(DirectoryLister::isPathOrder, DirectoryLister::isEntryOrder);

    private final LinkHandling linkHandling;
    private final Comparator<? super Path> pathOrder;
    private final Comparator<? super FileEntry> entryOrder;
    private final Lazy<Function<Stream<Path>, Stream<FileEntry>>> mapping;

    DirectoryLister(final LinkHandling linkHandling,
                    final Comparator<? super Path> pathOrder,
                    final Comparator<? super FileEntry> entryOrder) {
        this.linkHandling = linkHandling;
        this.pathOrder = pathOrder;
        this.entryOrder = entryOrder;
        this.mapping = Lazy.init(this::newMapping);
    }

    private FileEntry entryOfDefinite(final Path path) {
        return FileEntry.ofDefinite(path, linkHandling);
    }

    private FileEntry entryOf(final Path path) {
        return FileEntry.of(path, linkHandling);
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
     * Returns an instance that corresponds to <em>this</em> {@link DirectoryLister} but resolves symbolic links.
     * Returns <em>this</em> {@link DirectoryLister} if it already resolves symbolic links.
     *
     * @see FileEntry#lister(LinkHandling)
     */
    public final DirectoryLister resolved() {
        return (RESOLVE == linkHandling) ? this : new DirectoryLister(RESOLVE, pathOrder, entryOrder);
    }

    /**
     * Returns an instance that corresponds to <em>this</em> {@link DirectoryLister} but handles original symbolic links.
     * Returns <em>this</em> {@link DirectoryLister} if it already handles original symbolic links.
     *
     * @see FileEntry#lister(LinkHandling)
     */
    public final DirectoryLister original() {
        return (ORIGINAL == linkHandling) ? this : new DirectoryLister(ORIGINAL, pathOrder, entryOrder);
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
        return list(entry, FileEntry.Problem::log);
    }

    /**
     * Returns a {@link List} of the immediate contents of a given <em>path</em> from a directory structure.
     * <p>
     * Returns an empty {@link List} if the given <em>path</em> does not represent a directory
     * and thus cannot have any directory contents.
     * <p>
     * Also returns an empty {@link List} if the given <em>path</em> refuses access to its contents
     * and thus throws an exception. In that case, a corresponding {@link FileEntry.Problem} will be reported
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
    public final List<FileEntry> list(final Path path, final Consumer<? super FileEntry.Problem> onProblem) {
        return list(entryOf(path), onProblem);
    }

    /**
     * Returns a {@link List} of the immediate contents of a given <em>entry</em> from a directory structure.
     * <p>
     * Returns an empty {@link List} if the given <em>entry</em> does not represent a directory
     * and thus cannot have any directory contents.
     * <p>
     * Also returns an empty {@link List} if the given <em>entry</em> refuses access to its contents
     * and throws an exception. In that case, a corresponding {@link FileEntry.Problem} will be reported
     * to the given {@link Consumer}.
     *
     * @see #list(Path, Consumer)
     * @see #list(FileEntry)
     * @see #list(Path)
     */
    public final List<FileEntry> list(final FileEntry entry, final Consumer<? super FileEntry.Problem> onProblem) {
        if (entry.isDirectory()) {
            try (final Stream<Path> paths = Files.list(entry.path())) {
                return mapping.get().apply(paths).toList();
            } catch (final IOException caught) {
                onProblem.accept(new FileEntry.Problem(entry, caught));
            }
        }
        return List.of();
    }

    /**
     * Returns a copy of <em>this</em> {@link DirectoryLister}, with no order applied to the
     * {@linkplain #list(FileEntry, Consumer) listing}.
     */
    public final DirectoryLister noOrder() {
        return new DirectoryLister(linkHandling, Util.NO_ORDER, Util.NO_ORDER);
    }

    /**
     * Returns a copy of <em>this</em> {@link DirectoryLister}, with the given path order applied to the
     * {@linkplain #list(FileEntry, Consumer) listing}.
     */
    public final DirectoryLister pathOrder(final Comparator<? super Path> order) {
        return new DirectoryLister(linkHandling, order, entryOrder);
    }

    /**
     * Returns a copy of <em>this</em> {@link DirectoryLister}, with the given entry order applied to the
     * {@linkplain #list(FileEntry, Consumer) listing}.
     */
    public final DirectoryLister entryOrder(final Comparator<? super FileEntry> order) {
        return new DirectoryLister(linkHandling, pathOrder, order);
    }
}
