package de.team33.patterns.io.iocaste;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static de.team33.patterns.io.iocaste.LinkHandling.ORIGINAL;
import static de.team33.patterns.io.iocaste.LinkHandling.RESOLVE;

/**
 * A tool that serves to stream the recursive contents of any directory represented by a
 * {@link Path} or {@link FileEntry}.
 */
public final class DirectoryStreamer {

    @SuppressWarnings("rawtypes")
    private static final Predicate NEVER = new Predicate() {
        @Override
        public boolean test(final Object any) {
            return false;
        }

        @Override
        public Predicate or(final Predicate other) {
            return other;
        }
    };

    @SuppressWarnings("unchecked")
    public static final DirectoryStreamer DEFAULT = new DirectoryStreamer(DirectoryLister.DEFAULT, NEVER);
    @SuppressWarnings("unchecked")
    public static final DirectoryStreamer RESOLVING = new DirectoryStreamer(DirectoryLister.RESOLVING, NEVER);

    private final DirectoryLister lister;
    private final Predicate<FileEntry> skipCondition;

    @SuppressWarnings("unchecked")
    DirectoryStreamer(final DirectoryLister lister, final Predicate<FileEntry> skipCondition) {
        this.lister = lister;
        this.skipCondition = (null == skipCondition) ? NEVER : skipCondition;
    }

    @SuppressWarnings("unchecked")
    public static DirectoryStreamer basedOn(final DirectoryLister lister) {
        return new DirectoryStreamer(lister, NEVER);
    }

    private FileEntry entryOf(final Path path) {
        return FileEntry.of(path, lister.linkHandling());
    }

    /**
     * Returns an instance that corresponds to <em>this</em> {@link DirectoryStreamer} but resolves symbolic links.
     * Returns <em>this</em> {@link DirectoryStreamer} if it already resolves symbolic links.
     */
    public final DirectoryStreamer resolved() {
        return (RESOLVE == lister.linkHandling()) ? this : new DirectoryStreamer(lister.linkHandling(RESOLVE), skipCondition);
    }

    /**
     * Returns an instance that corresponds to <em>this</em> {@link DirectoryStreamer} but handles original symbolic links.
     * Returns <em>this</em> {@link DirectoryStreamer} if it already handles original symbolic links.
     */
    public final DirectoryStreamer original() {
        return (ORIGINAL == lister.linkHandling()) ? this : new DirectoryStreamer(lister.linkHandling(ORIGINAL), skipCondition);
    }

    /**
     * Returns a new {@link DirectoryStreamer} that skips all entries that meet the given <em>condition</em>,
     * as well as their entire content.
     */
    public final DirectoryStreamer skip(final Predicate<? super FileEntry> condition) {
        return new DirectoryStreamer(lister, skipCondition.or(condition));
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
        return stream(entry, FileEntry.Problem::log);
    }

    /**
     * Returns a {@link Stream} starting with a {@link FileEntry} based on the given <em>path</em>
     * followed by its recursive contents.
     * <p>
     * If an involved file refuses access to its contents and thus throws an {@link IOException},
     * a corresponding {@link FileEntry.Problem} will be reported to the given {@link Consumer}.
     * <p>
     * NOTE: the starting {@link FileEntry} will be created using the {@link LinkHandling} of the associated
     * {@link DirectoryLister}. If this does not meet your requirements, use {@link #stream(FileEntry, Consumer)} instead.
     *
     * @see #stream(FileEntry, Consumer)
     * @see #stream(Path)
     * @see #stream(FileEntry)
     */
    public final Stream<FileEntry> stream(final Path path, final Consumer<? super FileEntry.Problem> onProblem) {
        return stream(entryOf(path), onProblem);
    }

    /**
     * Returns a {@link Stream} starting with the given <em>entry</em> followed by its recursive contents.
     * <p>
     * If an involved <em>entry</em> refuses access to its contents and thus throws an exception,
     * a corresponding {@link FileEntry.Problem} will be reported to the given {@link Consumer}.
     *
     * @see #stream(Path, Consumer)
     * @see #stream(FileEntry)
     * @see #stream(Path)
     */
    public final Stream<FileEntry> stream(final FileEntry entry, final Consumer<? super FileEntry.Problem> onProblem) {
        return new Actor(onProblem).stream(entry);
    }

    private class Actor {

        private final Consumer<? super FileEntry.Problem> onProblem;

        private Actor(final Consumer<? super FileEntry.Problem> onProblem) {
            this.onProblem = onProblem;
        }

        private Stream<FileEntry> stream(final FileEntry entry) {
            return skipCondition.test(entry) ? Stream.of(entry)
                                             : stream(Stream.of(entry), lister.list(entry, onProblem));
        }

        private Stream<FileEntry> stream(final Stream<FileEntry> head, final List<FileEntry> tail) {
            return tail.isEmpty() ? head : Stream.concat(head, tail.stream().flatMap(this::stream));
        }
    }
}
