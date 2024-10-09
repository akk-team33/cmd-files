package de.team33.cmd.files.job;

import de.team33.cmd.files.cleaning.DirDeletion;
import de.team33.cmd.files.common.HashId;
import de.team33.cmd.files.common.Output;
import de.team33.cmd.files.common.RequestException;
import de.team33.cmd.files.moving.Guard;
import de.team33.patterns.exceptional.alpha.Ignoring;
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
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

import static java.util.function.Predicate.not;

class Deduping implements Runnable {

    static final String EXCERPT = "Relocate duplicated files located in a given directory.";
    private static final FilePolicy POLICY = FilePolicy.DISTINCT_SYMLINKS;

    private final Stats stats = new Stats();
    private final Set<Path> createDir = new HashSet<>();
    private final Map<String, Entry> index;
    private final Output out;
    private final Path mainPath;
    private final Path doubletPath;
    private final Path prevIndexPath;
    private final Path postIndexPath;
    private final DirDeletion deletion;

    private Deduping(final Output out, final Path path) {
        this.out = out;
        this.mainPath = path.toAbsolutePath().normalize();
        this.doubletPath = Paths.get(trash(mainPath));
        this.prevIndexPath = mainPath.resolve(Guard.DEDUPED_PAST);
        this.postIndexPath = mainPath.resolve(Guard.DEDUPED_NEXT);
        this.index = readIndex(prevIndexPath);
        this.deletion = new DirDeletion(out, mainPath, stats);
    }

    private static Map<String, Entry> readIndex(final Path indexPath) {
        Ignoring.any(IOException.class).get(() -> Files.createFile(indexPath));
        try {
            return Files.readAllLines(indexPath, StandardCharsets.UTF_8)
                        .stream()
                        .map(Entry::parse)
                        .collect(HashMap::new, (map, entry) -> map.put(entry.hash, entry), Map::putAll);
        } catch (final IOException ignored) {
            return new HashMap<>();
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
        throw RequestException.format(Moving.class, "Deduping.txt", Util.cmdLine(args), Util.cmdName(args));
    }

    private void writeIndex() {
        try (final BufferedWriter writer = Files.newBufferedWriter(postIndexPath,
                                                                   StandardOpenOption.CREATE,
                                                                   StandardOpenOption.TRUNCATE_EXISTING)) {
            for (final Entry entry : index.values()) {
                writer.append(entry.hash())
                      .append(Entry.SEPARATOR)
                      .append(Optional.ofNullable(entry.time())
                                      .map(Instant::toString)
                                      .orElse(""));
                writer.newLine();
            }
            writer.flush();
        } catch (final IOException e) {
            throw new IllegalStateException("could not write index <" + postIndexPath + ">", e);
        }
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
                 .filter(Guard::unprotected)
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
        if (index.containsKey(hashId)) {
            return false;
        } else {
            index.put(hashId, new Entry(hashId, entry.lastModified()));
            return true;
        }
    }

    private record Entry(String hash, Instant time) {

        static final String SEPARATOR = ":";
        static final Pattern PATTERN = Pattern.compile(Pattern.quote(SEPARATOR));

        static Entry parse(final String entry) {
            final String[] split = PATTERN.split(entry, 2);
            return new Entry(split[0], Ignoring.any(DateTimeParseException.class,
                                                    ArrayIndexOutOfBoundsException.class)
                                               .get(() -> Instant.parse(split[1]))
                                               .orElse(null));
        }
    }

    private static class Stats implements DirDeletion.Stats {

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
