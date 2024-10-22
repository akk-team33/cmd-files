package de.team33.cmd.files.job;

import de.team33.cmd.files.common.Condition;
import de.team33.cmd.files.common.FileType;
import de.team33.cmd.files.common.Output;
import de.team33.cmd.files.common.RequestException;
import de.team33.patterns.io.alpha.FileEntry;
import de.team33.patterns.io.alpha.FilePolicy;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static java.util.function.Predicate.not;

class Keeping implements Runnable {

    static final String EXCERPT = "Compare files of different types and strike a balance " +
                                  "based on their file names.";

    private final Output out;
    private final Set<Path> toBeMoved;
    private final Path movePath;

    private Keeping(final Output out, final String path, final String type1, final String type2) {
        this(out, path, type1, path, type2);
    }

    private Keeping(final Output out,
                    final String path1, final String type1,
                    final String path2, final String type2) {
        this(out, Path.of(path1), FileType.parse(type1), Path.of(path2), FileType.parse(type2));
    }

    private Keeping(final Output out,
                    final Path path1, final FileType type1,
                    final Path path2, final FileType type2) {
        final Set<String> nameToKeep = pureNamesOf(path1, type1);
        out.printf("[names to keep]%n%s%n", String.format(String.join("%n", nameToKeep)));
        this.out = out;
        this.toBeMoved = toBeMoved(path2, type2, nameToKeep);
        this.movePath = Path.of(path2.toString() + ".moved");
    }

    private static Set<Path> toBeMoved(final Path parent, final FileType type, final Set<String> names) {
        return FileEntry.of(parent, FilePolicy.RESOLVE_SYMLINKS)
                        .entries()
                        .stream()
                        .filter(type::isTypeOf)
                        .filter(not(path -> isMatching(path, names)))
                        .map(FileEntry::path)
                        .collect(Collectors.toCollection(TreeSet::new));
    }

    private static Set<String> pureNamesOf(final Path parent, final FileType type) {
        return FileEntry.of(parent, FilePolicy.RESOLVE_SYMLINKS)
                        .entries()
                        .stream()
                        .filter(FileEntry::isRegularFile)
                        .map(type::toPureName)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toCollection(TreeSet::new));
    }

    static Keeping job(final Condition condition) throws RequestException {
        final List<String> args = condition.args();
        final int size = args.size();
        if (5 == size) {
            return new Keeping(condition.out(), args.get(2), args.get(3), args.get(4));
        } else if (6 == size) {
            return new Keeping(condition.out(), args.get(2), args.get(3), args.get(4), args.get(5));
        } else {
            throw condition.toRequestException(Keeping.class).get();
        }
    }

    private static boolean isMatching(final FileEntry entry, final Set<String> names) {
        return names.stream()
                    .anyMatch(name -> entry.name().startsWith(name));
    }

    @Override
    public final void run() {
        toBeMoved.forEach(this::move);
    }

    private void move(final Path path) {
        final Path fileName = path.getFileName();
        final Path target = movePath.resolve(fileName);
        out.printf("moving %s%n-> %s ...", path, target);
        try {
            Files.createDirectories(movePath);
            Files.move(path, target);
            out.printf(" ok%n");
        } catch (final IOException e) {
            out.printf(" failed%n");
            throw new IllegalStateException(e.getMessage(), e);
        }
    }
}
