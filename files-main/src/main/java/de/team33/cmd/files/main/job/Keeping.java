package de.team33.cmd.files.main.job;

import de.team33.cmd.files.main.common.Context;
import de.team33.patterns.io.deimos.TextIO;
import de.team33.patterns.io.phobos.FileEntry;
import de.team33.patterns.io.phobos.FileIndex;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class Keeping implements Runnable {

    private final Context context;
    private final Path path1;
    private final Set<String> type1;
    private final Path path2;
    private final Set<String> type2;

    private Keeping(final Context context, final List<String> args) {
        this.context = context;
        this.path1 = Path.of(args.get(0)).toAbsolutePath().normalize();
        this.type1 = setOf(args.get(1));
        final int lastIndex;
        if (4 > args.size()) {
            this.path2 = path1;
            lastIndex = 2;
        } else {
            this.path2 = Path.of(args.get(2)).toAbsolutePath().normalize();
            lastIndex = 3;
        }
        this.type2 = setOf(args.get(lastIndex));
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
        final Set<String> names = FileIndex.evaluated(path1)
                                           .skipEntry(FileEntry::isDirectory)
                                           .stream()
                                           .map(this::toPureName)
                                           .filter(Objects::nonNull)
                                           .collect(Collectors.toCollection(TreeSet::new));
        context.printf("names: %s", names);
    }

    private String toPureName(final FileEntry entry) {
        final String fullName = entry.path().getFileName().toString();
        return type1.stream()
                    .map(ext -> "." + ext)
                    .filter(fullName::endsWith)
                    .findAny()
                    .map(ext -> fullName.replace(ext, ""))
                    .orElse(null);
    }
}
