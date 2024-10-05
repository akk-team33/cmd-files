package de.team33.cmd.files.job;

import de.team33.cmd.files.common.Output;
import de.team33.cmd.files.common.RequestException;
import de.team33.patterns.io.alpha.FileEntry;
import de.team33.patterns.io.alpha.FilePolicy;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

class DirCopying implements Runnable {

    static final String EXCERPT = "Copy the subdirectory structure from one directory to another.";
    private static final FilePolicy POLICY = FilePolicy.RESOLVE_SYMLINKS;

    private final Output out;
    private final Path source;
    private final Path target;

    private DirCopying(final Output out, final Path source, final Path target) {
        this.out = out;
        this.source = source;
        this.target = target;
    }

    static DirCopying job(final Output out, final List<String> args) throws RequestException {
        assert 1 < args.size();
        assert Regular.DCOPY.name().equalsIgnoreCase(args.get(1));
        // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
        if (4 == args.size()) {
            final Path source = Path.of(args.get(2));
            final Path target = Path.of(args.get(3));
            return new DirCopying(out, source, target);
        }
        throw RequestException.format(DirCopying.class, "DirCopying.txt", Util.cmdLine(args), Util.cmdName(args));
    }

    @Override
    public void run() {
        copy(FileEntry.of(source, POLICY).entries());
    }

    private void copy(final List<FileEntry> entries) {
        entries.forEach(this::copy);
    }

    private void copy(final FileEntry entry) {
        if (entry.isDirectory()) {
            copy(entry.entries());
            final Path relative = source.relativize(entry.path());
            out.printf("%s ...", relative);
            try {
                Files.createDirectories(target.resolve(relative));
                out.printf(" created%n", relative);
            } catch (final IOException e) {
                out.printf(" failed:%n" +
                           "    Message   : %s%n" +
                           "    Exception : %s%n", e.getMessage(), e.getClass().getCanonicalName());
            }
        }
    }
}
