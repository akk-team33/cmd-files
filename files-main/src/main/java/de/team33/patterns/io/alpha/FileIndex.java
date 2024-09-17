package de.team33.patterns.io.alpha;

import java.io.File;
import java.nio.file.Path;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class FileIndex {

    private static final Predicate<File> NEVER = file -> false;

    private final Path path;
    private final FilePolicy policy;
    private Predicate<File> skipCondition = NEVER;

    private FileIndex(final Path path, final FilePolicy policy) {
        this.path = path.toAbsolutePath().normalize();
        this.policy = policy;
    }

    public static FileIndex of(final Path path, final FilePolicy policy) {
        return new FileIndex(path, policy);
    }

    public final Stream<File> files() {
        return streamAll(path.toFile());
    }

    private Stream<File> streamAll(final File[] files) {
        return Stream.of(files)
                     .flatMap(this::streamAll);
    }

    private Stream<File> streamAll(final File file) {
        if (skipCondition.test(file)) {
            return Stream.empty();
        }
        final Stream<File> head = Stream.of(file);
        if (file.isDirectory()) {
            return Stream.concat(head, streamAll(file.listFiles()));
        } else {
            return head;
        }
    }

    public final FileIndex skipFiles(final Predicate<File> condition) {
        this.skipCondition = condition;
        return this;
    }

    public final FileIndex skipPaths(final Predicate<Path> condition) {
        return skipFiles(file -> condition.test(file.toPath()));
    }
}
