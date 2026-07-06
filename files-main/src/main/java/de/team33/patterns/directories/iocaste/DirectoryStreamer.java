package de.team33.patterns.directories.iocaste;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import static de.team33.patterns.directories.iocaste.Filter.reject;

/**
 * A tool that serves to stream the recursive contents of any directory
 * represented by a {@link Path} or {@link FileEntry}.
 */
public final class DirectoryStreamer {

    public static final DirectoryStreamer DEFAULT = basedOn(DirectoryLister.DEFAULT);
    public static final DirectoryStreamer RESOLVING = basedOn(DirectoryLister.RESOLVING);

    private final DirectoryLister lister;
    private final Predicate<FileEntry> skipCondition;
    private final int startLevel;
    private final int limitLevel;

    private DirectoryStreamer(final DirectoryLister lister, final Predicate<FileEntry> skipCondition,
                              final int startLevel, final int limitLevel) {
        this.lister = lister;
        this.skipCondition = skipCondition;
        this.startLevel = startLevel;
        this.limitLevel = limitLevel;
    }

    public static DirectoryStreamer basedOn(final DirectoryLister lister) {
        return new DirectoryStreamer(lister, reject(), 0, Integer.MAX_VALUE);
    }

    private FileEntry entryOf(final Path path) {
        return FileEntry.of(path, lister.linkHandling());
    }

    public DirectoryStreamer rebased(final UnaryOperator<DirectoryLister> operator) {
        return new DirectoryStreamer(operator.apply(lister), skipCondition, startLevel, limitLevel);
    }

    /**
     * Returns a new {@link DirectoryStreamer} that skips all entries that meet the given <em>condition</em>,
     * as well as their entire content.
     */
    public final DirectoryStreamer skip(final Predicate<? super FileEntry> condition) {
        return new DirectoryStreamer(lister, skipCondition.or(condition), startLevel, limitLevel);
    }

    /**
     * Returns a new {@link DirectoryStreamer} that starts streaming at the given recursion <em>level</em>.
     * For instance ...
     * <ul>
     *     <li>at level 0, a given directory entry is included in a resulting stream.</li>
     *     <li>at level 1, a resulting stream begins with the sub-elements of a given directory.</li>
     *     <li>at level 2, a resulting stream begins with the sub-elements of the sub-elements
     *     of a given directory.</li>
     *     <li>...</li>
     * </ul>
     * <p>
     * Default is level 0.
     *
     * @see #stream(Path)
     * @see #stream(FileEntry)
     * @see #stream(Path, Consumer)
     * @see #stream(FileEntry, Consumer)
     */
    public final DirectoryStreamer start(final int level) {
        return new DirectoryStreamer(lister, skipCondition, level, limitLevel);
    }

    /**
     * Returns a new {@link DirectoryStreamer} that limits streaming to the given recursion <em>level</em>.
     * <p>
     * More precisely, the given level is the first one not included in a result stream.
     * <p>
     * If the limit is less than or equal to the start level, streaming will always result in an empty stream.
     * <p>
     * Default limit is {@link Integer#MAX_VALUE} that in fact means <em>no limit</em>.
     *
     * @see #start(int)
     */
    public final DirectoryStreamer limit(final int level) {
        return new DirectoryStreamer(lister, skipCondition, startLevel, level);
    }

    /**
     * Returns a {@link Stream} starting with a {@link FileEntry} based on the given <em>path</em>
     * followed by its recursive contents.
     * <p>
     * If an involved file refuses access to its contents and thus throws an {@link IOException},
     * the problem will be logged to a {@link System.Logger}.
     * <p>
     * NOTE: the starting {@link FileEntry} will be created using the {@link LinkHandling} of the associated
     * {@link DirectoryLister}. If this does not meet your requirements, use {@link #stream(FileEntry)} instead.
     *
     * @see #stream(FileEntry)
     * @see #stream(Path, Consumer)
     * @see #stream(FileEntry, Consumer)
     */
    public final Stream<FileEntry> stream(final Path path) {
        return stream(entryOf(path));
    }

    /**
     * Returns a {@link Stream} starting with the given <em>entry</em> followed by its recursive contents.
     * <p>
     * If an involved <em>entry</em> refuses access to its contents and thus throws an exception,
     * the problem will be logged to a {@link System.Logger}.
     *
     * @see #stream(Path)
     * @see #stream(Path, Consumer)
     * @see #stream(FileEntry, Consumer)
     */
    public final Stream<FileEntry> stream(final FileEntry entry) {
        return stream(entry, Problem::log);
    }

    /**
     * Returns a {@link Stream} starting with a {@link FileEntry} based on the given <em>path</em>
     * followed by its recursive contents.
     * <p>
     * If an involved file refuses access to its contents and thus throws an {@link IOException},
     * a corresponding {@link Problem} will be reported to the given {@link Consumer}.
     * <p>
     * NOTE: the starting {@link FileEntry} will be created using the {@link LinkHandling} of the associated
     * {@link DirectoryLister}. If this does not meet your requirements, use {@link #stream(FileEntry, Consumer)} instead.
     *
     * @see #stream(FileEntry, Consumer)
     * @see #stream(Path)
     * @see #stream(FileEntry)
     */
    public final Stream<FileEntry> stream(final Path path, final Consumer<? super Problem> onProblem) {
        return stream(entryOf(path), onProblem);
    }

    /**
     * Returns a {@link Stream} starting with the given <em>entry</em> followed by its recursive contents.
     * <p>
     * If an involved <em>entry</em> refuses access to its contents and thus throws an exception,
     * a corresponding {@link Problem} will be reported to the given {@link Consumer}.
     *
     * @see #stream(Path, Consumer)
     * @see #stream(FileEntry)
     * @see #stream(Path)
     */
    public final Stream<FileEntry> stream(final FileEntry entry, final Consumer<? super Problem> onProblem) {
        return new Actor(onProblem).stream(0, entry);
    }

    private class Actor {

        private final Consumer<? super Problem> onProblem;

        private Actor(final Consumer<? super Problem> onProblem) {
            this.onProblem = onProblem;
        }

        private Stream<FileEntry> stream(final int level, final FileEntry entry) {
            if (level < limitLevel) {
                final Stream<FileEntry> head = (level < startLevel) ? Stream.empty() : Stream.of(entry);
                return skipCondition.test(entry) ? head : stream(level, head, lister.list(entry, onProblem));
            } else {
                return Stream.empty();
            }
        }

        private Stream<FileEntry> stream(final int level, final Stream<FileEntry> head, final List<FileEntry> tail) {
            return tail.isEmpty() ? head : Stream.concat(head, tail.stream()
                                                                   .flatMap(entry -> stream(level + 1, entry)));
        }
    }
}
