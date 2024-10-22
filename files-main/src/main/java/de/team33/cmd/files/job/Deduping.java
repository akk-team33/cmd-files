package de.team33.cmd.files.job;

import de.team33.cmd.files.cleaning.DirDeletion;
import de.team33.cmd.files.common.Condition;
import de.team33.cmd.files.common.HashId;
import de.team33.cmd.files.common.Output;
import de.team33.cmd.files.common.RequestException;
import de.team33.cmd.files.moving.Guard;
import de.team33.patterns.io.alpha.FileEntry;
import de.team33.patterns.io.alpha.FileIndex;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Stream;

class Deduping implements Runnable {

    static final String EXCERPT = "Relocate duplicated files located in a given directory.";

    private final Stats stats = new Stats();
    private final Set<Path> createDir = new HashSet<>();
    private final Index index;
    private final Output out;
    private final Path mainPath;
    private final Path doubletPath;
    private final DirDeletion deletion;

    private Deduping(final Output out, final Path path) {
        this.out = out;
        this.mainPath = path.toAbsolutePath().normalize();
        this.doubletPath = Paths.get(trash(mainPath));
        this.index = Index.of(mainPath);
        this.deletion = new DirDeletion(out, mainPath, stats);
    }

    private static String trash(final Path path) {
        return path + ".(dupes)";
    }

    static Deduping job(final Condition condition) throws RequestException {
        return Optional.of(condition.args())
                       .filter(args -> 3 == args.size())
                       .map(args -> new Deduping(condition.out(), Path.of(args.get(2))))
                       .orElseThrow(condition.toRequestException(Deduping.class));
    }

    @Override
    public final void run() {
        stats.reset();
        out.printf("%s ...%n", mainPath);
        FileIndex.of(mainPath)
                 .skipPath(doubletPath::equals)
                 .entries()
                 .peek(stats::incExamined)
                 .filter(FileEntry::isRegularFile)
                 .filter(Guard::unprotected)
                 .filter(index::isDuplicated)
                 .map(FileEntry::path)
                 .forEach(this::move);
        index.write();
        deletion.clean(FileEntry.of(mainPath).entries());
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

    private record Entry(String hash, int level, Instant time) {

        static final String SEPARATOR = ":";
        static final Pattern PATTERN = Pattern.compile(Pattern.quote(SEPARATOR));

        static Entry parse(final String entry) {
            final String[] split = PATTERN.split(entry, 3);
            return new Entry(split[0], Integer.parseInt(split[1]), Instant.parse(split[2]));
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

    private static class Index {

        private static final String PRFX_HEADER = "#";
        private static final String PRFX_ID = PRFX_HEADER + "dedupe-id:";
        private static final String PRFX_LEVEL = PRFX_HEADER + "dedupe-past-level:";

        private final Path path;
        private final String pathId;
        private final int pastLevel;
        private final int nextLevel;
        private final boolean isNext;
        private final Map<String, Entry> entries;

        private Index(final Path path, final String pathId, final int pastLevel,
                      final boolean isNext, final Map<String, Entry> entries) {
            this.path = path;
            this.pathId = pathId;
            this.pastLevel = pastLevel;
            this.nextLevel = pastLevel + 1;
            this.isNext = isNext;
            this.entries = entries;
        }

        private static <X extends Throwable> X addSuppressed(final X x, final Throwable t) {
            x.addSuppressed(t);
            return x;
        }

        private static String existingPathId(final Path path, final IOException e0) {
            try {
                return Files.readString(path)
                            .trim();
            } catch (final IOException ex) {
                final IOException e = (e0 == null) ? ex : addSuppressed(e0, ex);
                throw IOFault.by("Could not read pathId", path, e);
            }
        }

        private static String pathId(final Path mainPath) {
            final Path path = mainPath.resolve(Guard.DEDUPE_PATH_ID);
            try {
                Files.write(path,
                            List.of(UUID.randomUUID().toString()),
                            StandardOpenOption.CREATE_NEW);
                return existingPathId(path, null);
            } catch (final IOException e) {
                // Assuming the file already exists
                return existingPathId(path, e);
            }
        }

        private static List<String> existingHeader(final Path path, final IOException e0) {
            try (final Stream<String> lines = Files.lines(path, StandardCharsets.UTF_8)) {
                final List<String> result = lines.takeWhile(line -> line.startsWith(PRFX_HEADER))
                                                 .toList();
                if (2 == result.size()) {
                    return result;
                } else {
                    throw new IllegalStateException(("Illegal header in index file%n" +
                                                     "    Path: %s%n" +
                                                     "    Header: %s%n").formatted(path, result));
                }
            } catch (final IOException ex) {
                throw newReadException(path, (null == e0) ? ex : addSuppressed(e0, ex));
            }
        }

        private static List<String> header(final Path path, final String pathId) {
            try {
                Files.write(path,
                            List.of(PRFX_ID + pathId, PRFX_LEVEL + 0),
                            StandardOpenOption.CREATE_NEW);
                return existingHeader(path, null);
            } catch (final IOException e) {
                // Assuming the file already exists
                return existingHeader(path, e);
            }
        }

        private static IllegalStateException newReadException(final Path path, final Throwable cause) {
            return IOFault.by("Could not read index file", path, cause);
        }

        private static void putEntry(final Map<String, Entry> map, Entry entry) {
            map.put(entry.hash(), entry);
        }

        private static Map<String, Entry> readEntries(final int pastLevel, final Path path) {
            try (final Stream<String> lines = Files.lines(path, StandardCharsets.UTF_8)) {
                return lines.skip(2)
                            .map(Entry::parse)
                            .filter(entry -> entry.level() <= pastLevel)
                            .collect(TreeMap::new, Index::putEntry, Map::putAll);
            } catch (final IOException e) {
                throw newReadException(path, e);
            }
        }

        private static boolean isNext(final Path mainPath) {
            final Path path = mainPath.resolve(Guard.DEDUPE_NEXT);
            try {
                Files.delete(path);
                return true;
            } catch (final NoSuchFileException ignored) {
                return false;
            } catch (IOException e) {
                throw IOFault.by("Could not delete symbolic file", path, e);
            }
        }

        static Index of(final Path mainPath) {
            final boolean isNext = isNext(mainPath);
            final String pathId = pathId(mainPath);
            final Path path = mainPath.resolve(Guard.DEDUPED_INDEX);
            final List<String> header = header(path, pathId);
            final String headerId = header.get(0).substring(PRFX_ID.length());
            final int pastLevel = (headerId.equals(pathId) ? 0 : 1) +
                                  Integer.parseInt(header.get(1).substring(PRFX_LEVEL.length()));
            final Map<String, Entry> entries = readEntries(pastLevel, path);
            return new Index(path, pathId, pastLevel, isNext, entries);
        }

        final void write() {
            try (final BufferedWriter out = Files.newBufferedWriter(path, StandardCharsets.UTF_8,
                                                                    StandardOpenOption.CREATE,
                                                                    StandardOpenOption.TRUNCATE_EXISTING)) {
                out.append(PRFX_ID)
                   .append(String.valueOf(pathId));
                out.newLine();
                out.append(PRFX_LEVEL)
                   .append(String.valueOf(pastLevel));
                out.newLine();
                for (final Entry entry : entries.values()) {
                    out.append(entry.hash())
                       .append(Entry.SEPARATOR)
                       .append(String.valueOf(entry.level()))
                       .append(Entry.SEPARATOR)
                       .append(String.valueOf(entry.time()));
                    out.newLine();
                }
            } catch (final IOException e) {
                throw IOFault.by("Could not write index file", path, e);
            }
        }

        final boolean isUpdateEntry(final Entry indexEntry, final Instant fileTime) {
            return isNext && (indexEntry.level == pastLevel) && indexEntry.time().equals(fileTime);
        }

        final boolean isDuplicated(final FileEntry entry) {
            final String fileHash = HashId.coreValueOf(entry.path());
            final Instant fileTime = entry.lastModified().truncatedTo(ChronoUnit.SECONDS);
            final Entry idxEntry = entries.get(fileHash);
            if (null == idxEntry || isUpdateEntry(idxEntry, fileTime)) {
                entries.put(fileHash, new Entry(fileHash, nextLevel, fileTime));
                return false;
            } else {
                return true;
            }
        }
    }
}
