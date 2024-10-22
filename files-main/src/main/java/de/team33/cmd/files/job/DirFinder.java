package de.team33.cmd.files.job;

import de.team33.cmd.files.common.Condition;
import de.team33.cmd.files.common.Counter;
import de.team33.cmd.files.common.Output;
import de.team33.cmd.files.common.RequestException;
import de.team33.cmd.files.matching.NameMatcher;
import de.team33.patterns.io.alpha.FileEntry;
import de.team33.patterns.io.alpha.FileIndex;

import java.nio.file.Path;
import java.util.*;

class DirFinder implements Runnable {

    static final String EXCERPT = "Find directories containing files that match a pattern.";

    private final Output out;
    private final NameMatcher nameMatcher;
    private final FileIndex index;

    private DirFinder(final Output out, final String expression, final List<Path> paths) {
        this.out = out;
        this.nameMatcher = NameMatcher.parse(expression);
        this.index = FileIndex.of(paths);
    }

    public static Runnable job(final Condition condition) throws RequestException {
        return Optional.of(condition.args())
                       .filter(args -> 3 < args.size())
                       .map(args -> new DirFinder(condition.out(),
                                                  args.get(2),
                                                  args.stream()
                                                      .skip(3)
                                                      .map(Path::of)
                                                      .toList()))
                       .orElseThrow(condition.toRequestException(DirFinder.class));
    }

    @Override
    public final void run() {
        final Stats stats = new Stats();
        index.entries()
             .peek(stats::addTotal)
             .filter(nameMatcher::matches)
             .map(FileEntry::path)
             .map(Path::getParent)
             .filter(Objects::nonNull)
             .filter(stats::addFound)
             .forEach(parent -> out.printf("%s%n", parent));
        out.printf("%n" +
                   "%,12d directories found.%n" +
                   "%,12d directories and a total of%n" +
                   "%,12d entries examined.%n%n",
                   stats.found.size(), stats.directories.value(), stats.total.value());
    }

    private static class Stats {

        final Counter total = new Counter();
        final Counter directories = new Counter();
        final Set<Path> found = new HashSet<>();

        final void addTotal(final FileEntry entry) {
            total.increment();
            if (entry.isDirectory()) {
                directories.increment();
            }
        }

        final boolean addFound(final Path path) {
            return found.add(path);
        }
    }
}
