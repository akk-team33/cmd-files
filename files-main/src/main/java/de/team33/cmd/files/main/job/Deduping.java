package de.team33.cmd.files.main.job;

import de.team33.cmd.files.main.common.HashId;
import de.team33.cmd.files.main.common.Output;
import de.team33.cmd.files.main.common.RequestException;
import de.team33.cmd.files.main.common.TimeId;
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
import java.util.*;
import java.util.regex.Pattern;

import static de.team33.cmd.files.main.job.Util.cmdLine;
import static de.team33.cmd.files.main.job.Util.cmdName;
import static java.util.function.Predicate.not;

class Deduping implements Runnable {

    static final String EXCERPT = "Relocate duplicated files located in a given directory.";
    private static final FilePolicy POLICY = FilePolicy.DISTINCT_SYMLINKS;

    private final Set<Path> createDir = new HashSet<>();
    private final Output out;
    private final Path mainPath;
    private final Path doubletPath;
    private final Path indexPath;
    private final Map<String, Entry> index;

    private Deduping(final Output out, final Path path) {
        this.out = out;
        this.mainPath = path.toAbsolutePath().normalize();
        this.doubletPath = Paths.get(trash(mainPath));
        this.indexPath = mainPath.resolve("(deduped).txt");

        this.index = readIndex(indexPath);
    }

    private static Map<String, Entry> readIndex(final Path indexPath) {
        try {
            return Files.readAllLines(indexPath, StandardCharsets.UTF_8)
                        .stream()
                        .map(Entry::parse)
                        .collect(HashMap::new, (map, entry) -> map.put(entry.hash, entry), Map::putAll);
        } catch (final IOException ignored) {
            return new HashMap<>();
        }
    }

    private void writeIndex() {
        try (final BufferedWriter writer = Files.newBufferedWriter(indexPath,
                                                                   StandardOpenOption.CREATE,
                                                                   StandardOpenOption.TRUNCATE_EXISTING)) {
            for (final Entry entry : index.values()) {
                writer.append(entry.hash)
                      .append(Entry.SEPARATOR)
                      .append(entry.time);
                writer.newLine();
            }
            writer.flush();
        } catch (final IOException e) {
            throw new IllegalStateException("could not write index <" + indexPath + ">", e);
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
        FileIndex.of(mainPath, POLICY)
                 .skipPath(doubletPath::equals)
                 .entries()
                 .filter(FileEntry::isRegularFile)
                 .filter(not(entry -> indexPath.equals(entry.path())))
                 .filter(not(this::isUnique))
                 .map(FileEntry::path)
                 .forEach(this::move);
        writeIndex();
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
        } catch (final IOException e) {
            out.printf("failed:%n" +
                       "    Message   : %s%n" +
                       "    Exception : %s%n", e.getMessage(), e.getClass().getCanonicalName());
            //stats.incMoveFailed();
        }
    }

    private boolean isUnique(final FileEntry entry) {
        final String hashId = HashId.coreValueOf(entry.path());
        final String timeId = TimeId.coreValueOf(entry);
        if (index.containsKey(hashId)) {
            return index.get(hashId).time.equals(timeId);
        } else {
            index.put(hashId, new Entry(hashId, timeId));
            return true;
        }
    }

    private record Entry(String hash, String time) {

        static final String SEPARATOR = ":";
        static final Pattern PATTERN = Pattern.compile(Pattern.quote(SEPARATOR));

        static Entry parse(final String entry) {
            final String[] split = PATTERN.split(entry);
            return new Entry(split[0], split[1]);
        }
    }
}
