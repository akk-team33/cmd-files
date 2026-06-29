package de.team33.patterns.io.adrastea;

import de.team33.patterns.decision.thyone.Choices;
import de.team33.patterns.lazy.narvi.Lazy;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static de.team33.patterns.io.adrastea.LinkHandling.ORIGINAL;
import static de.team33.patterns.io.adrastea.LinkHandling.RESOLVE;
import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.Logger.Level.WARNING;

public class Directory {

    private static BasicFileAttributes newAttributes(final Path path, final LinkHandling handling) {
        try {
            return Files.readAttributes(path, BasicFileAttributes.class, handling.options());
        } catch (final IOException ignored) {
            return Util.MISSING_FILE_ATTRIBUTES;
        }
    }

    /**
     * Returns a new {@link Lister} based on a given {@link LinkHandling}
     * that applies a default path order (by file name).
     */
    public static Lister lister(final LinkHandling linkHandling) {
        return new Lister(linkHandling, Util.PATH_ORDER, Util.NO_ORDER);
    }

    /**
     * Returns a new {@link Streamer} based on a given {@link LinkHandling}
     * that does not skip any entry.
     */
    public static Streamer streamer(final LinkHandling linkHandling) {
        return streamer(lister(linkHandling));
    }

    /**
     * Returns a new {@link Streamer} based on a given {@link Lister}
     * that does not skip any entry.
     */
    public static Streamer streamer(final Lister lister) {
        //noinspection unchecked
        return new Streamer(lister, Streamer.NEVER);
    }

    /**
     * Reports a problem regarding a file operation.
     *
     * @param entry the {@link Directory} the problem is related to.
     * @param cause an {@link IOException} that represents the problem.
     */
    public record Problem(FileEntry entry, IOException cause) {

        private static final System.Logger LOGGER = System.getLogger(Problem.class.getCanonicalName());
        private static final String MESSAGE = "Cannot access file:%n" +
                                              "    path:    %s%n" +
                                              "    cause:   %s%n" +
                                              "    message: %s%n";

        public final void log() {
            final Supplier<String> msgSupplier = () -> MESSAGE.formatted(entry.path(),
                                                                         cause.getClass().getCanonicalName(),
                                                                         cause.getMessage());
            LOGGER.log(WARNING, msgSupplier);
            LOGGER.log(DEBUG, msgSupplier, cause);
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

        private LinkHandling linkHandling() {
            return linkHandling;
        }

        /**
         * Returns an instance that corresponds to <em>this</em> {@link Lister} but resolves symbolic links.
         * Returns <em>this</em> {@link Lister} if it already resolves symbolic links.
         *
         * @see Directory#lister(LinkHandling)
         */
        public final Lister resolved() {
            return (RESOLVE == linkHandling) ? this : new Lister(RESOLVE, pathOrder, entryOrder);
        }

        /**
         * Returns an instance that corresponds to <em>this</em> {@link Lister} but handles original symbolic links.
         * Returns <em>this</em> {@link Lister} if it already handles original symbolic links.
         *
         * @see Directory#lister(LinkHandling)
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

    /**
     * A tool that serves to stream the recursive contents of any directory represented by a
     * {@link Path} or {@link FileEntry}.
     */
    public static final class Streamer {

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

        private final Lister lister;
        private final Predicate<FileEntry> skipCondition;

        private Streamer(final Lister lister, final Predicate<FileEntry> skipCondition) {
            this.lister = lister;
            this.skipCondition = skipCondition;
        }

        private FileEntry entryOf(final Path path) {
            return FileEntry.of(path, lister.linkHandling());
        }

        /**
         * Returns an instance that corresponds to <em>this</em> {@link Streamer} but resolves symbolic links.
         * Returns <em>this</em> {@link Streamer} if it already resolves symbolic links.
         *
         * @see Directory#streamer(LinkHandling)
         */
        public final Streamer resolved() {
            return (RESOLVE == lister.linkHandling) ? this : new Streamer(lister.resolved(), skipCondition);
        }

        /**
         * Returns an instance that corresponds to <em>this</em> {@link Streamer} but handles original symbolic links.
         * Returns <em>this</em> {@link Streamer} if it already handles original symbolic links.
         *
         * @see Directory#streamer(LinkHandling)
         */
        public final Streamer original() {
            return (ORIGINAL == lister.linkHandling) ? this : new Streamer(lister.original(), skipCondition);
        }

        /**
         * Returns a new {@link Streamer} that skips all entries that meet the given <em>condition</em>,
         * as well as their entire content.
         */
        public final Streamer skip(final Predicate<? super FileEntry> condition) {
            return new Streamer(lister, skipCondition.or(condition));
        }

        /**
         * Returns a {@link Stream} starting with a {@link FileEntry} based on the given <em>path</em>
         * followed by its recursive contents.
         * <p>
         * If an involved file refuses access to its contents and thus throws an {@link IOException},
         * the problem will be logged to a {@link System.Logger}.
         * <p>
         * NOTE: the starting {@link FileEntry} will be created using the {@link LinkHandling} of the associated
         * {@link Lister}. If this does not meet your requirements, use {@link #stream(FileEntry)} instead.
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
         * {@link Lister}. If this does not meet your requirements, use {@link #stream(FileEntry, Consumer)} instead.
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
            return new Actor(onProblem).stream(entry);
        }

        private class Actor {

            private final Consumer<? super Problem> onProblem;

            private Actor(final Consumer<? super Problem> onProblem) {
                this.onProblem = onProblem;
            }

            private Stream<FileEntry> stream(final FileEntry entry) {
                final Stream<FileEntry> head = Stream.of(entry);
                return skipCondition.test(entry) ? Stream.empty() // TODO: head
                                                 : stream(head, lister.list(entry, onProblem));
            }

            private Stream<FileEntry> stream(final Stream<FileEntry> head, final List<FileEntry> tail) {
                return tail.isEmpty() ? head : Stream.concat(head, tail.stream().flatMap(this::stream));
            }
        }
    }
}
