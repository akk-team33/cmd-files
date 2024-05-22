package de.team33.cmd.files.main.job;

import de.team33.cmd.files.main.common.Context;
import de.team33.cmd.files.main.common.FileType;
import de.team33.patterns.io.deimos.TextIO;
import de.team33.patterns.io.phobos.FileEntry;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static java.util.function.Predicate.not;

public class Keeping implements Runnable {

    public static final String EXCERPT = "Compare files of different types and strike a balance " +
                                         "based on their file names.";

    private final Context context;
    private final Set<Path> toBeMoved;
    private final Path movePath;

    private Keeping(final Context context, final String path, final String type1, final String type2) {
        this(context, path, type1, path, type2);
    }

    private Keeping(final Context context,
                    final String path1, final String type1,
                    final String path2, final String type2) {
        this(context, Path.of(path1), FileType.parse(type1), Path.of(path2), FileType.parse(type2));
    }

    private Keeping(final Context context,
                    final Path path1, final FileType type1,
                    final Path path2, final FileType type2) {
        final Set<String> nameToKeep = pureNamesOf(path1, type1);
        context.printf("[names to keep]%n%s%n", String.format(String.join("%n", nameToKeep)));
        this.context = context;
        this.toBeMoved = toBeMoved(path2, type2, nameToKeep);
        this.movePath = Path.of(path2.toString() + ".moved");
    }

    private static Set<Path> toBeMoved(final Path parent, final FileType type, final Set<String> names) {
        return FileEntry.evaluated(parent)
                        .content()
                        .stream()
                        .filter(type::isTypeOf)
                        .filter(not(path -> isMatching(path, names)))
                        .collect(Collectors.toCollection(TreeSet::new));
    }

    private static Set<String> pureNamesOf(final Path parent, final FileType type) {
        return FileEntry.evaluated(parent)
                        .content()
                        .stream()
                        .filter(Files::isRegularFile)
                        .map(type::toPureName)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toCollection(TreeSet::new));
    }

    public static Runnable job(final Context context, final List<String> args) {
        assert 1 < args.size();
        assert Regular.KEEP.name().equalsIgnoreCase(args.get(1));
        // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
        final int size = args.size();
        if (5 == size) {
            return new Keeping(context, args.get(2), args.get(3), args.get(4));
        } else if (6 == size) {
            return new Keeping(context, args.get(2), args.get(3), args.get(4), args.get(5));
        } else {
            final String format = TextIO.read(Keeping.class, "Keeping.txt");
            final String cmdLine = String.join(" ", args);
            return () -> context.printf(format, cmdLine, args.get(0));
        }
    }

    @Override
    public final void run() {
        toBeMoved.forEach(this::move);
    }

    private void move(final Path path) {
        final Path fileName = path.getFileName();
        final Path target = movePath.resolve(fileName);
        context.printf("moving %s%n-> %s ...", path, target);
        try {
            Files.createDirectories(movePath);
            Files.move(path, target);
            context.printf(" ok%n");
        } catch (final IOException e) {
            context.printf(" failed%n");
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    private static boolean isMatching(final Path path, final Set<String> names) {
        return names.stream()
                    .anyMatch(name -> path.getFileName()
                                          .toString()
                                          .startsWith(name));
    }
}
