package de.team33.patterns.directories.iocaste;

import de.team33.patterns.decision.thyone.Choices;
import de.team33.patterns.lazy.narvi.Lazy;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static de.team33.patterns.directories.iocaste.Filter.accept;
import static de.team33.patterns.directories.iocaste.LinkHandling.ORIGINAL;
import static de.team33.patterns.directories.iocaste.LinkHandling.RESOLVE;
import static de.team33.patterns.directories.iocaste.Util.NO_ORDER;

/**
 * A tool that serves to list the immediate contents of any directory
 * represented by a {@link Path} or {@link FileEntry}.
 */
public final class DirectoryLister {

    /**
     * A public instance of {@link DirectoryLister} that keeps symbolic links unresolved
     * and applies no specific order when listing a directory.
     */
    public static final DirectoryLister DEFAULT = new DirectoryLister(ORIGINAL, accept(), NO_ORDER, accept(), NO_ORDER);

    /**
     * A public instance of {@link DirectoryLister} that resolves symbolic links
     * and applies no specific order when listing a directory.
     */
    public static final DirectoryLister RESOLVING = DEFAULT.linkHandling(RESOLVE);

    private static final Choices<DirectoryLister> CHOICES = Choices.parallel(DirectoryLister::isPathOrder,
                                                                             DirectoryLister::isEntryOrder);

    private final LinkHandling linkHandling;
    private final Predicate<Path> pathFilter;
    private final Comparator<? super Path> pathOrder;
    private final Predicate<FileEntry> entryFilter;
    private final Comparator<? super FileEntry> entryOrder;
    private final Lazy<Function<Stream<Path>, Stream<FileEntry>>> lazyStreaming;

    private DirectoryLister(final LinkHandling linkHandling,
                            final Predicate<Path> pathFilter,
                            final Comparator<? super Path> pathOrder,
                            final Predicate<FileEntry> entryFilter,
                            final Comparator<? super FileEntry> entryOrder) {
        this.linkHandling = linkHandling;
        this.pathFilter = pathFilter;
        this.pathOrder = pathOrder;
        this.entryFilter = entryFilter;
        this.entryOrder = entryOrder;
        this.lazyStreaming = Lazy.init(this::newStreaming);
    }

    private FileEntry entryOfDefinite(final Path path) {
        return FileEntry.ofDefinite(path, linkHandling);
    }

    private FileEntry entryOf(final Path path) {
        return FileEntry.of(path, linkHandling);
    }

    private Function<Stream<Path>, Stream<FileEntry>> newStreaming() {
        return switch (CHOICES.apply(this)) {
            case 0b11 -> paths -> paths.filter(pathFilter)
                                       .sorted(pathOrder)
                                       .map(this::entryOfDefinite)
                                       .filter(entryFilter)
                                       .sorted(entryOrder);
            case 0b10 -> paths -> paths.filter(pathFilter)
                                       .sorted(pathOrder)
                                       .map(this::entryOfDefinite)
                                       .filter(entryFilter);
            case 0b01 -> paths -> paths.filter(pathFilter)
                                       .map(this::entryOfDefinite)
                                       .filter(entryFilter)
                                       .sorted(entryOrder);
            default -> paths -> paths.filter(pathFilter)
                                     .map(this::entryOfDefinite)
                                     .filter(entryFilter);
        };
    }

    private boolean isPathOrder() {
        return NO_ORDER != pathOrder;
    }

    private boolean isEntryOrder() {
        return NO_ORDER != entryOrder;
    }

    LinkHandling linkHandling() {
        return linkHandling;
    }

    /**
     * Returns an instance that corresponds to <em>this</em> {@link DirectoryLister} but handles symbolic links
     * as specified by the given <em>handling</em>.
     * Returns <em>this</em> {@link DirectoryLister} if it already handles symbolic links as specified
     * by the given <em>handling</em>.
     */
    public final DirectoryLister linkHandling(final LinkHandling handling) {
        return new DirectoryLister(handling, pathFilter, pathOrder, entryFilter, entryOrder);
    }

    /**
     * Returns a copy of <em>this</em> {@link DirectoryLister}, with no filter applied to the
     * {@linkplain #list(FileEntry, Consumer) listing}.
     */
    public final DirectoryLister noFilter() {
        return new DirectoryLister(linkHandling, Filter.accept(), pathOrder, Filter.accept(), entryOrder);
    }

    /**
     * Returns a copy of <em>this</em> {@link DirectoryLister}, with no order applied to the
     * {@linkplain #list(FileEntry, Consumer) listing}.
     */
    public final DirectoryLister noOrder() {
        return new DirectoryLister(linkHandling, pathFilter, NO_ORDER, entryFilter, NO_ORDER);
    }

    /**
     * Returns a copy of <em>this</em> {@link DirectoryLister}, with the given path <em>filter</em>
     * additionally applied to the {@linkplain #list(FileEntry, Consumer) listing}.
     * <p>
     * The given <em>filter</em> is combined with an existing filter using the AND operator.
     */
    public final DirectoryLister pathFilter(final Predicate<? super Path> filter) {
        return new DirectoryLister(linkHandling, pathFilter.and(filter), pathOrder, entryFilter, entryOrder);
    }

    /**
     * Returns a copy of <em>this</em> {@link DirectoryLister}, with the given path order applied to the
     * {@linkplain #list(FileEntry, Consumer) listing}.
     */
    public final DirectoryLister pathOrder(final Comparator<? super Path> order) {
        return new DirectoryLister(linkHandling, pathFilter, order, entryFilter, entryOrder);
    }

    /**
     * Returns a copy of <em>this</em> {@link DirectoryLister}, with the given entry <em>filter</em>
     * additionally applied to the {@linkplain #list(FileEntry, Consumer) listing}.
     * <p>
     * The given <em>filter</em> is combined with an existing filter using the AND operator.
     */
    public final DirectoryLister entryFilter(final Predicate<? super FileEntry> filter) {
        return new DirectoryLister(linkHandling, pathFilter, pathOrder, entryFilter.and(filter), entryOrder);
    }

    /**
     * Returns a copy of <em>this</em> {@link DirectoryLister}, with the given entry order applied to the
     * {@linkplain #list(FileEntry, Consumer) listing}.
     */
    public final DirectoryLister entryOrder(final Comparator<? super FileEntry> order) {
        return new DirectoryLister(linkHandling, pathFilter, pathOrder, entryFilter, order);
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
                return lazyStreaming.get().apply(paths).toList();
            } catch (final IOException caught) {
                onProblem.accept(new Problem(entry, caught));
            }
        }
        return List.of();
    }
}
