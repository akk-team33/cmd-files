package de.team33.cmd.files.main.job;

import de.team33.cmd.files.main.common.Output;
import de.team33.cmd.files.main.common.RequestException;
import de.team33.cmd.files.main.finder.Pattern;
import de.team33.patterns.io.alpha.FileEntry;
import de.team33.patterns.io.alpha.FileIndex;
import de.team33.patterns.io.alpha.FilePolicy;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import static de.team33.cmd.files.main.job.Util.cmdLine;
import static de.team33.cmd.files.main.job.Util.cmdName;
import static java.util.Objects.requireNonNullElse;

class DirFinder implements Runnable {

    static final String EXCERPT = "Find directories containing files that match a pattern.";

    private final Output out;
    private final Pattern pattern;
    private final FileIndex index;

    public DirFinder(final Output out, final String expression, final List<Path> paths) {
        this.out = out;
        this.pattern = Pattern.parse(expression);
        this.index = FileIndex.of(paths, FilePolicy.DISTINCT_SYMLINKS);
    }

    public static Runnable job(final Output out, final List<String> args) throws RequestException {
        assert 1 < args.size();
        assert Regular.FINDIR.name().equalsIgnoreCase(args.get(1));
        // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
        if (3 < args.size()) {
            final String expression = args.get(2);
            final List<Path> paths = args.stream().skip(3).map(Path::of).toList();
            return new DirFinder(out, expression, paths);
        }
        throw RequestException.format(Listing.class, "DirFinder.txt", cmdLine(args), cmdName(args));
    }

    @Override
    public final void run() {
        final Counter total = new Counter(null);
        final Counter directories = new Counter(FileEntry::isDirectory);
        final Set<Path> found = new HashSet<>();
        index.entries()
             .peek(total::add)
             .peek(directories::add)
             .filter(pattern.matcher())
             .forEach(entry -> {
                 final Path parent = Optional.ofNullable(entry.path().getParent())
                                             .orElseGet(entry::path);
                 if (found.add(parent)) {
                     out.printf("%s%n", parent);
                 }
             });
        out.printf("%n" +
                   "%,12d directories found.%n" +
                   "%,12d directories and a total of%n" +
                   "%,12d entries examined.%n%n",
                   found.size(), directories.value, total.value);
    }

    private static class Counter {
        static final Predicate<? super FileEntry> EACH = any -> true;

        private long value;
        private final Predicate<? super FileEntry> filter;

        private Counter(Predicate<? super FileEntry> filter) {
            this.filter = requireNonNullElse(filter, EACH);
        }

        private void add(final FileEntry entry) {
            if (filter.test(entry)) {
                value += 1;
            }
        }
    }
}
