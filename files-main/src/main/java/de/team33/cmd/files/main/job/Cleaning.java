package de.team33.cmd.files.main.job;

import de.team33.cmd.files.main.common.Output;
import de.team33.cmd.files.main.common.RequestException;
import de.team33.cmd.files.main.finder.Pattern;
import de.team33.patterns.io.alpha.FileEntry;
import de.team33.patterns.io.alpha.FileIndex;
import de.team33.patterns.io.alpha.FilePolicy;

import java.nio.file.Path;
import java.util.List;
import java.util.function.Predicate;

import static de.team33.cmd.files.main.job.Util.cmdLine;
import static de.team33.cmd.files.main.job.Util.cmdName;
import static java.util.Objects.requireNonNullElse;

class Cleaning implements Runnable {

    static final String EXCERPT = "Remove empty directories within given directories.";

    private final Output out;
    private final FileIndex index;

    private Cleaning(final Output out, final List<Path> paths) {
        this.out = out;
        this.index = FileIndex.of(paths, FilePolicy.DISTINCT_SYMLINKS);
    }

    public static Runnable job(final Output out, final List<String> args) throws RequestException {
        assert 1 < args.size();
        assert Regular.CLEAN.name().equalsIgnoreCase(args.get(1));
        // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
        if (2 < args.size()) {
            final List<Path> paths = args.stream().skip(2).map(Path::of).toList();
            return new Cleaning(out, paths);
        }
        throw RequestException.format(Listing.class, "Finder.txt", cmdLine(args), cmdName(args));
    }

    @Override
    public final void run() {
        final Counter total = new Counter(null);
        final Counter directories = new Counter(FileEntry::isDirectory);
        final Counter found = new Counter(null);
        index.entries()
             .peek(total::add)
             .peek(directories::add)
             .peek(found::add)
             .forEach(entry -> out.printf("%s%n", entry.path()));
        out.printf("%n" +
                   "%,12d entries found.%n" +
                   "%,12d directories and a total of%n" +
                   "%,12d entries examined.%n%n",
                   found.value, directories.value, total.value);
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
