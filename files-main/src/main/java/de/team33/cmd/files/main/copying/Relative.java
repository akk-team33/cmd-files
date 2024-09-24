package de.team33.cmd.files.main.copying;

import de.team33.patterns.io.alpha.FileEntry;
import de.team33.patterns.io.alpha.FileIndex;
import de.team33.patterns.io.alpha.FilePolicy;

import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class Relative {

    private final List<RuntimeException> problems;
    private final Set<Path> result;
    private final Path leftPath;
    private final Path rightPath;

    private Relative(final Path leftPath, final Path rightPath) {
        this.problems = Collections.synchronizedList(new LinkedList<>());
        this.result = Collections.synchronizedSet(new TreeSet<>());
        this.leftPath = leftPath.toAbsolutePath().normalize();
        this.rightPath = rightPath.toAbsolutePath().normalize();
    }

    public static Set<Path> collect(final Path leftPath, final Path rightPath) {
        return new Relative(leftPath, rightPath).process().result;
    }

    private Relative process() {
        final Thread leftThread = new Thread(() -> run(leftPath));
        final Thread rightThread = new Thread(() -> run(rightPath));
        start(leftThread, rightThread);
        join(leftThread, rightThread);
        return thisOrThrowProblems();
    }

    private Relative thisOrThrowProblems() {
        final RuntimeException problem = problems.stream()
                                                 .reduce(Relative::addSuppressed)
                                                 .orElse(null);
        if (null == problem) {
            return this;
        } else {
            throw problem;
        }
    }

    private static RuntimeException addSuppressed(final RuntimeException head,
                                                  final RuntimeException next) {
        head.addSuppressed(next);
        return head;
    }

    private void start(final Thread ... threads) {
        for (final Thread thread : threads) {
            thread.start();
        }
    }

    private void join(final Thread ... threads) {
        for (final Thread thread : threads) {
            try {
                thread.join();
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException(e.getMessage(), e);
            }
        }
    }

    private void run(final Path path) {
        try {
            FileIndex.of(path, FilePolicy.DISTINCT_SYMLINKS)
                     .entries()
                     .filter(FileEntry::isRegularFile)
                     .map(entry -> path.relativize(entry.path()))
                     .forEach(result::add);
        } catch (RuntimeException e) {
            problems.add(e);
        }
    }
}
