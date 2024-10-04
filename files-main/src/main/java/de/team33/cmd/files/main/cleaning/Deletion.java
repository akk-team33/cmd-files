package de.team33.cmd.files.main.cleaning;

import de.team33.cmd.files.main.common.Output;
import de.team33.patterns.io.alpha.FileEntry;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class Deletion {

    private final Output out;
    private final Path mainPath;
    private final Stats stats;

    public Deletion(final Output out, final Path mainPath, final Stats stats) {
        this.out = out;
        this.mainPath = mainPath;
        this.stats = stats;
    }

    public boolean clean(final List<FileEntry> entries) {
        return entries.stream()
                      .map(this::clean)
                      .reduce(true, Boolean::logicalAnd);
    }

    private boolean clean(final FileEntry entry) {
        return entry.isDirectory() && clean(entry.entries()) && clean(entry.path());
    }

    private boolean clean(final Path path) {
        out.printf("%s ... ", mainPath.relativize(path));
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
