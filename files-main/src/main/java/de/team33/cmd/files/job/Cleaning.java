package de.team33.cmd.files.job;

import de.team33.cmd.files.cleaning.Cleaner;
import de.team33.cmd.files.common.Args;
import de.team33.cmd.files.common.Counter;
import de.team33.cmd.files.common.Output;
import de.team33.cmd.files.common.RequestException;
import de.team33.cmd.files.listing.Option;
import de.team33.patterns.files.iocaste.FileEntry;

import java.nio.file.Path;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import static de.team33.cmd.files.job.Util.cmdLine;
import static de.team33.cmd.files.job.Util.cmdName;

class Cleaning implements Runnable {

    static final String EXCERPT = "Remove empty directories within given directories.";

    private static final Set<Option> OPTIONS = EnumSet.noneOf(Option.class);
    private static final Function<List<String>, Args> ARGS = Args.stage(3, OPTIONS);

    private final Stats stats = new Stats();
    private final Output out;
    private final FileEntry entry;
    private final Cleaner cleaner;

    private Cleaning(final Output out, final FileEntry entry) {
        this.out = out;
        this.entry = entry;
        this.cleaner = new Cleaner(out, stats);
    }

    static Cleaning job(final Output out, final List<String> args) throws RequestException {
        try {
            return job(out, ARGS.apply(args));
        } catch (final IllegalArgumentException e) {
            throw RequestException.format(Cleaning.class, "Cleaning.txt", cmdLine(args), cmdName(args));
        }
    }

    private static Cleaning job(final Output out, final Args args) {
        return new Cleaning(out, FileEntry.original(Path.of(args.get(2))));
    }

    @Override
    public final void run() {
        stats.reset();
        cleaner.clean(entry);
        out.printf("%n" +
                   "%,12d directories and a total of%n" +
                   "%,12d entries examined.%n%n" +
                   "%,12d directories deleted%n" +
                   "%,12d attempts failed%n%n",
                   stats.totalDirs.value(), stats.total.value(), stats.deleted.value(), stats.failed.value());
    }

    private static class Stats implements Cleaner.Stats {

        private final Counter total = new Counter();
        private final Counter totalDirs = new Counter();
        private final Counter deleted = new Counter();
        private final Counter failed = new Counter();

        final void reset() {
            total.reset();
            totalDirs.reset();
            deleted.reset();
            failed.reset();
        }

        @Override
        public void incDeleted() {
            deleted.increment();
        }

        @Override
        public void incDeleteFailed() {
            failed.increment();
        }
    }
}
