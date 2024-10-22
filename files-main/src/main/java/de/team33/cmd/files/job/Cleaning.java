package de.team33.cmd.files.job;

import de.team33.cmd.files.cleaning.DirDeletion;
import de.team33.cmd.files.common.Condition;
import de.team33.cmd.files.common.Counter;
import de.team33.cmd.files.common.Output;
import de.team33.cmd.files.common.RequestException;
import de.team33.patterns.io.alpha.FileEntry;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

class Cleaning implements Runnable {

    static final String EXCERPT = "Remove empty directories within given directories.";

    private final Stats stats = new Stats();
    private final Output out;
    private final List<FileEntry> entries;
    private final DirDeletion deletion;

    private Cleaning(final Output out, final List<FileEntry> entries) {
        this.out = out;
        this.entries = entries;
        this.deletion = new DirDeletion(out, Path.of(".").toAbsolutePath().normalize(), stats);
    }

    public static Cleaning job(final Condition condition) throws RequestException {
        final List<FileEntry> entries = Optional.of(condition.args())
                                                .filter(args -> (2 < args.size()))
                                                .map(args -> args.stream()
                                                                 .skip(2)
                                                                 .map(Path::of)
                                                                 .map(FileEntry::of)
                                                                 .toList())
                                                .orElseThrow(condition.toRequestException(Cleaning.class));
        return new Cleaning(condition.out(), entries);
    }

    @Override
    public final void run() {
        stats.reset();
        deletion.clean(entries);
        out.printf("%n" +
                   "%,12d directories and a total of%n" +
                   "%,12d entries examined.%n%n" +
                   "%,12d directories deleted%n" +
                   "%,12d attempts failed%n%n",
                   stats.totalDirs.value(), stats.total.value(), stats.deleted.value(), stats.failed.value());
    }

    private static class Stats implements DirDeletion.Stats {

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
