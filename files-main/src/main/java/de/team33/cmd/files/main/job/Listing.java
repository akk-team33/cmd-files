package de.team33.cmd.files.main.job;

import de.team33.cmd.files.main.common.Context;
import de.team33.cmd.files.main.common.FileType;
import de.team33.patterns.enums.alpha.EnumTool;
import de.team33.patterns.io.deimos.TextIO;
import de.team33.patterns.io.phobos.FileEntry;

import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class Listing implements Runnable {

    public static final String EXCERPT = "List names of files of a specific type";

    private final Context context;
    private final Set<String> aspects;

    private Listing(final Context context, final Aspect aspect, final Path path, final FileType type) {
        final Function<Path, String> mapping = aspect.mapping(type);
        this.context = context;
        this.aspects = FileEntry.evaluated(path)
                                .content()
                                .stream()
                                .filter(type::isTypeOf)
                                .map(mapping)
                                .filter(Objects::nonNull)
                                .collect(Collectors.toCollection(TreeSet::new));
    }

    public static Runnable job(final Context context, final List<String> args) {
        assert 1 < args.size();
        assert Regular.LIST.name().equalsIgnoreCase(args.get(1));
        // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
        final int size = args.size();
        if (5 == size) {
            return new Listing(context, Aspect.of(args.get(2)), Path.of(args.get(3)), FileType.parse(args.get(4)));
        } else {
            return new InfoJob(context, args).printf(TextIO.read(Listing.class, "Listing.txt"));
        }
    }

    @Override
    public void run() {
        aspects.forEach(line -> context.printf("%s%n", line));
    }

    private enum Aspect {

        N(fileType -> fileType::toPureName),
        X(fileType -> fileType::toExtension),
        NX(fileType -> path -> path.getFileName().toString());

        private static final EnumTool<Aspect> TOOL = EnumTool.of(Aspect.class);

        private final Function<FileType, Function<Path, String>> toExtraction;

        Aspect(Function<FileType, Function<Path, String>> toExtraction) {
            this.toExtraction = toExtraction;
        }

        private static Supplier<RuntimeException> newNoSuchElementException(final String value) {
            return () -> new NoSuchElementException("no Aspect specified for '" + value + "'");
        }

        private static Aspect of(final String value) {
            return TOOL.failing(newNoSuchElementException(value))
                       .findAny(item -> value.equalsIgnoreCase(item.name()));
        }

        public Function<Path, String> mapping(final FileType type) {
            return toExtraction.apply(type);
        }
    }
}
