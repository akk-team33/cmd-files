package de.team33.cmd.files.main.job;

import de.team33.cmd.files.main.common.Context;
import de.team33.patterns.io.deimos.TextIO;
import de.team33.patterns.io.phobos.FileEntry;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class Keeping implements Runnable {

    private final Context context;
    private final Set<String> names;
    private final Path path2;
    private final Set<String> type2;
    private final Path path3;

    private Keeping(final Context context, final List<String> args) {
        this.context = context;
        this.names = names(Path.of(args.get(0)), setOf(args.get(1)));

        final int lastIndex;
        if (4 > args.size()) {
            this.path2 = Path.of(args.get(0));
            lastIndex = 2;
        } else {
            this.path2 = Path.of(args.get(2));
            lastIndex = 3;
        }
        this.type2 = setOf(args.get(lastIndex));
        this.path3 = path2.resolve("(moved)");
    }

    private static Set<String> names(final Path path, final Set<String> type) {
        return FileEntry.primary(path)
                        .content()
                        .stream()
                        .map(FileEntry::primary)
                        .filter(FileEntry::isRegularFile)
                        .map(entry -> toPureName(entry, type))
                        .filter(Objects::nonNull)
                        .collect(Collectors.toCollection(TreeSet::new));
    }

    private static String toPureName(final FileEntry entry, final Set<String> type) {
        final String fullName = entry.path().getFileName().toString();
        return type.stream()
                   .map(ext -> "." + ext)
                   .filter(fullName::endsWith)
                   .findAny()
                   .map(ext -> fullName.replace(ext, ""))
                   .orElse(null);
    }

    private static boolean isType(final Set<String> type, final Path path) {
        final String fullName = path.getFileName().toString();
        return type.stream()
                   .map(ext -> "." + ext)
                   .anyMatch(fullName::endsWith);
    }

    private static Set<String> setOf(final String csv) {
        return new TreeSet<>(Arrays.asList(csv.split(",")));
    }

    public static Runnable job(final Context context, final List<String> args) {
        assert 1 < args.size();
        assert Regular.KEEP.name().equalsIgnoreCase(args.get(1));
        // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
        return job(context, args.get(0), args);
    }

    private static Runnable job(final Context context, final String cmdName, final List<String> args) {
        final int size = args.size();
        if (5 <= size && size <= 6) {
            return new Keeping(context, args.subList(2, size));
        } else {
            final String format = TextIO.read(Keeping.class, "Keeping.txt");
            final String cmdLine = String.join(" ", args);
            return () -> context.printf(format, cmdLine, cmdName);
        }
    }

    @Override
    public final void run() {
        FileEntry.primary(path2)
                 .content()
                 .stream()
                 .filter(path -> isType(type2, path))
                 .filter(path -> isNotMatching(path, names))
                 .forEach(this::move);
    }

    private void move(final Path path) {
        final Path fileName = path.getFileName();
        final Path target = path3.resolve(fileName);
        context.printf("moving %s%n-> %s ...", path, target);
        try {
            Files.createDirectories(path3);
            Files.move(path, target);
            context.printf(" ok%n");
        } catch (final IOException e) {
            context.printf(" failed%n");
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    private static boolean isNotMatching(final Path path, final Set<String> names) {
        return names.stream()
                    .noneMatch(name -> path.getFileName()
                                           .toString()
                                           .startsWith(name));
    }
}
