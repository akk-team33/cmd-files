package de.team33.cmd.files.job;

import de.team33.cmd.files.common.Counter;
import de.team33.cmd.files.common.Output;
import de.team33.cmd.files.common.RequestException;
import de.team33.cmd.files.matching.NameMatcher;
import de.team33.patterns.io.adrastea.FileEntry;
import de.team33.patterns.io.adrastea.LinkHandling;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static de.team33.cmd.files.job.Util.cmdLine;
import static de.team33.cmd.files.job.Util.cmdName;
import static de.team33.patterns.io.adrastea.LinkHandling.ORIGINAL;

class DirFinder implements Runnable {

    static final String EXCERPT = "Find directories containing files that match a pattern.";
    private static final FileEntry.Streamer STREAMER = FileEntry.streamer(LinkHandling.ORIGINAL);

    private final Output out;
    private final NameMatcher nameMatcher;
    private final List<FileEntry> index;

    private DirFinder(final Output out, final String expression, final List<Path> paths) {
        this.out = out;
        this.nameMatcher = NameMatcher.parse(expression);
        this.index = paths.stream()
                          .map(path -> FileEntry.of(path, ORIGINAL))
                          .toList();
    }

    public static Runnable job(final Output out, final List<String> args) throws RequestException {
        assert 1 < args.size();
        assert Command.FINDIR.name().equalsIgnoreCase(args.get(1));
        // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
        if (3 < args.size()) {
            final String expression = args.get(2);
            final List<Path> paths = args.stream().skip(3).map(Path::of).toList();
            return new DirFinder(out, expression, paths);
        }
        throw RequestException.format(DirFinder.class, "DirFinder.txt", cmdLine(args), cmdName(args));
    }

    @Override
    public final void run() {
        final Stats stats = new Stats();
        index.stream()
             .flatMap(STREAMER::stream)
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
