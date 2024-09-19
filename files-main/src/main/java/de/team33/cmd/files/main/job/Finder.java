package de.team33.cmd.files.main.job;

import de.team33.cmd.files.main.common.Output;
import de.team33.cmd.files.main.common.RequestException;
import de.team33.patterns.io.alpha.FileIndex;
import de.team33.patterns.io.alpha.FilePolicy;

import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;

import static de.team33.cmd.files.main.job.Util.cmdLine;
import static de.team33.cmd.files.main.job.Util.cmdName;

class Finder implements Runnable {

    static final String EXCERPT = "Find files by names using regular expressions.";

    private final Output out;
    private final Pattern pattern;
    private final FileIndex index;

    public Finder(final Output out, final String expression, final List<Path> paths) {
        this.out = out;
        this.pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
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
        index.entries()
             .filter(entry -> pattern.matcher(entry.name()).matches())
             .forEach(entry -> out.printf("%s%n", entry.path()));
        out.printf("%n");
    }
}
