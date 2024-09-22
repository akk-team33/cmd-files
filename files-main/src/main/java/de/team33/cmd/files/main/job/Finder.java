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

class Finder implements Runnable {

    static final String EXCERPT = "Find files by names using regular expressions.";

    private final Output out;
    private final Pattern pattern;
    private final FileIndex index;

    public Finder(final Output out, final String expression, final List<Path> paths) {
        this.out = out;
        this.pattern = Pattern.parse(expression);
        this.index = FileIndex.of(paths.get(0), FilePolicy.DISTINCT_SYMLINKS); // TODO: index of all paths!
    }

    public static Runnable job(final Output out, final List<String> args) throws RequestException {
        assert 1 < args.size();
        assert Regular.FIND.name().equalsIgnoreCase(args.get(1));
        // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
        if (3 < args.size()) {
            final String expression = args.get(2);
            final List<Path> paths = args.stream().skip(3).map(Path::of).toList();
            return new Finder(out, expression, paths);
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
             .filter(entry -> pattern.matcher().test(entry.name()))
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
