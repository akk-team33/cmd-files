package de.team33.cmd.files.main.job;

import de.team33.cmd.files.main.common.Counter;
import de.team33.cmd.files.main.common.Output;
import de.team33.cmd.files.main.common.RequestException;
import de.team33.patterns.io.alpha.FileEntry;
import de.team33.patterns.io.alpha.FilePolicy;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static de.team33.cmd.files.main.job.Util.cmdLine;
import static de.team33.cmd.files.main.job.Util.cmdName;

class Cleaning implements Runnable {

    static final String EXCERPT = "Remove empty directories within given directories.";
    private static final FilePolicy POLICY = FilePolicy.DISTINCT_SYMLINKS;

    private final Output out;
    private final List<FileEntry> entries;
    private final Stats stats = new Stats();

    private Cleaning(final Output out, final List<FileEntry> entries) {
        this.out = out;
        this.entries = entries;
    }

    public static Runnable job(final Output out, final List<String> args) throws RequestException {
        assert 1 < args.size();
        assert Regular.CLEAN.name().equalsIgnoreCase(args.get(1));
        // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
        if (2 < args.size()) {
            final List<FileEntry> entries = args.stream()
                                                .skip(2)
                                                .map(Path::of)
                                                .map(path -> FileEntry.of(path, POLICY))
                                                .toList();
            return new Cleaning(out, entries);
        }
        throw RequestException.format(Listing.class, "Cleaning.txt", cmdLine(args), cmdName(args));
    }

    @Override
    public final void run() {
        stats.reset();
        clean(entries);
        out.printf("%n" +
                   "%,12d directories and a total of%n" +
                   "%,12d entries examined.%n%n" +
                   "%,12d directories deleted%n" +
                   "%,12d attempts failed%n%n",
                   stats.totalDirs.value(), stats.total.value(), stats.deleted.value(), stats.failed.value());
    }

    private boolean clean(final List<FileEntry> entries) {
        return entries.stream()
                      .map(this::clean)
                      .reduce(true, Boolean::logicalAnd);
    }

    private boolean clean(final FileEntry entry) {
        stats.addTotal(entry);
        if (entry.isDirectory() && clean(entry.entries())) {
            final Path path = entry.path();
            out.printf("%s ...", path);
            try {
                Files.delete(path);
                out.printf(" deleted%n");
                stats.addDeleted();
                return true;
            } catch (final IOException e) {
                out.printf(" failed:%n" +
                           "    Message: %s%n" +
                           "    Exception: %s%n", e.getMessage(), e.getClass().getCanonicalName());
                stats.addFailed();
                return false;
            }
        } else {
            return false;
        }
    }

    private static class Stats {

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

        final void addTotal(final FileEntry entry) {
            total.increment();
            if (entry.isDirectory()) {
                totalDirs.increment();
            }
        }

        final void addDeleted() {
            deleted.increment();
        }

        final void addFailed() {
            failed.increment();
        }
    }
}
