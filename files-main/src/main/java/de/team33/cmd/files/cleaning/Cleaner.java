package de.team33.cmd.files.cleaning;

import de.team33.cmd.files.common.Output;
import de.team33.patterns.io.iocaste.FileEntry;
import de.team33.patterns.io.iocaste.LinkHandling;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class Cleaner {

    private static final FileEntry.Lister LISTER = FileEntry.lister(LinkHandling.ORIGINAL);

    private final Output out;
    private final Stats stats;

    public Cleaner(final Output out, final Stats stats) {
        this.out = out;
        this.stats = stats;
    }

    public boolean clean(final FileEntry entry) {
        return entry.isDirectory() && clean(LISTER.list(entry)) && clean(entry.path());
    }

    private boolean clean(final List<FileEntry> entries) {
        return entries.stream()
                      .map(this::clean)
                      .reduce(true, Boolean::logicalAnd);
    }

    private boolean clean(final Path path) {
        out.printf("%s ... ", path);
        try {
            Files.delete(path);
            out.printf("deleted%n");
            stats.incDeleted();
            return true;
        } catch (final IOException e) {
            out.printf("failed:%n" +
                       "    Message   : %s%n" +
                       "    Exception : %s%n", e.getMessage(), e.getClass().getCanonicalName());
            stats.incDeleteFailed();
            return false;
        }
    }

    public interface Stats {

        void incDeleted();

        void incDeleteFailed();
    }
}
