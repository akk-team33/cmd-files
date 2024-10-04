package de.team33.cmd.files.main.job;

import de.team33.cmd.files.main.cleaning.Deletion;
import de.team33.cmd.files.main.common.HashId;
import de.team33.cmd.files.main.common.Output;
import de.team33.cmd.files.main.common.RequestException;
import de.team33.patterns.io.alpha.FileEntry;
import de.team33.patterns.io.alpha.FileIndex;
import de.team33.patterns.io.alpha.FilePolicy;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import static de.team33.cmd.files.main.job.Util.cmdLine;
import static de.team33.cmd.files.main.job.Util.cmdName;
import static java.util.function.Predicate.not;

class Deduping implements Runnable {

    static final String EXCERPT = "Relocate duplicated files located in a given directory.";
    private static final FilePolicy POLICY = FilePolicy.DISTINCT_SYMLINKS;

    private final Stats stats = new Stats();
    private final Set<Path> createDir = new HashSet<>();
    private final Set<String> index;
    private final Output out;
    private final Path mainPath;
    private final Path doubletPath;
    private final Path prevIndexPath;
    private final Path postIndexPath;
    private final Deletion deletion;

    private Deduping(final Output out, final Path path) {
        this.out = out;
        this.mainPath = path.toAbsolutePath().normalize();
        this.doubletPath = Paths.get(trash(mainPath));
        this.prevIndexPath = mainPath.resolve("(deduped-prev).txt");
        this.postIndexPath = mainPath.resolve("(deduped-post).txt");
        this.index = readIndex(prevIndexPath);
        this.deletion = new Deletion(out, mainPath, stats);
    }

    private static Set<String> readIndex(final Path indexPath) {
        try {
            return Files.readAllLines(indexPath, StandardCharsets.UTF_8)
                        .stream()
                        .map(Entry::parse)
                        .collect(HashSet::new, HashSet::add, Set::addAll);
        } catch (final IOException ignored) {
            return new HashSet<>();
        }
    }

    private void writeIndex() {
        try (final BufferedWriter writer = Files.newBufferedWriter(postIndexPath,
                                                                   StandardOpenOption.CREATE,
                                                                   StandardOpenOption.TRUNCATE_EXISTING)) {
            for (final String value : index) {
                writer.append(value);
                writer.newLine();
            }
            writer.flush();
        } catch (final IOException e) {
            throw new IllegalStateException("could not write index <" + postIndexPath + ">", e);
        }
    }

    private static String trash(final Path path) {
        return path + ".(dupes)";
    }

    public static Deduping job(final Output out, final List<String> args) throws RequestException {
        assert 1 < args.size();
        assert Regular.DEDUPE.name().equalsIgnoreCase(args.get(1));
        // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
        if (3 == args.size()) {
            return new Deduping(out, Path.of(args.get(2)));
        }
        throw RequestException.format(Moving.class, "Deduping.txt", cmdLine(args), cmdName(args));
    }

    @Override
    public final void run() {
        stats.reset();
        out.printf("%s ...%n", mainPath);
        FileIndex.of(mainPath, POLICY)
                 .skipPath(doubletPath::equals)
                 .entries()
                 .peek(stats::incExamined)
                 .filter(FileEntry::isRegularFile)
                 .filter(not(entry -> prevIndexPath.equals(entry.path())))
                 .filter(not(entry -> postIndexPath.equals(entry.path())))
                 .filter(not(this::isUnique))
                 .map(FileEntry::path)
                 .forEach(this::move);
        writeIndex();
        deletion.clean(FileEntry.of(mainPath, POLICY).entries());
        out.printf("%n" +
                   "%,12d directories and a total of%n" +
                   "%,12d entries examined.%n%n" +
                   "%,12d files moved to %s%n" +
                   "%,12d movements failed%n" +
                   "%,12d empty directories deleted%n" +
                   "%,12d deletions failed%n%n",
                   stats.directories, stats.examined,
                   stats.moved, mainPath.relativize(doubletPath), stats.failed,
                   stats.deletedDirs, stats.deletionFailed);
    }

    private void move(final Path path) {
        final Path relative = mainPath.relativize(path);
        out.printf("%s ... ", relative);
        final Path newPath = doubletPath.resolve(relative);

        try {
            final Path newParent = newPath.getParent();
            if (createDir.add(newParent)) {
                Files.createDirectories(newParent);
            }
            Files.move(path, newPath);
            out.printf("moved%n");
            stats.incMoved();
        } catch (final IOException e) {
            out.printf("failed:%n" +
                       "    Message   : %s%n" +
                       "    Exception : %s%n", e.getMessage(), e.getClass().getCanonicalName());
            stats.incFailed();
        }
    }

    private boolean isUnique(final FileEntry entry) {
        final String hashId = HashId.coreValueOf(entry.path());
        return index.add(hashId);
    }

    private static class Entry {

        static final String SEPARATOR = ":";
        static final Pattern PATTERN = Pattern.compile(Pattern.quote(SEPARATOR));

        static String parse(final String entry) {
            final String[] split = PATTERN.split(entry);
            return split[0];
        }
    }

    private static class Stats implements Deletion.Stats {

        private int examined;
        private int directories;
        private int moved;
        private int failed;
        private int deletedDirs;
        private int deletionFailed;

        final void reset() {
            examined = 0;
            directories = 0;
            moved = 0;
            failed = 0;
            deletedDirs = 0;
            deletionFailed = 0;
        }

        private void incExamined(final FileEntry entry) {
            this.examined += 1;
            if (entry.isDirectory()) {
                this.directories += 1;
            }
        }

        private void incMoved() {
            this.moved += 1;
        }

        private void incFailed() {
            this.failed += 1;
        }

        @Override
        public void incDeleted() {
            this.deletedDirs += 1;
        }

        @Override
        public void incDeleteFailed() {
            this.deletionFailed += 1;
        }
    }
}
