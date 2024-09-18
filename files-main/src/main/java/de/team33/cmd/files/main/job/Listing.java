package de.team33.cmd.files.main.job;

import de.team33.cmd.files.main.common.FileType;
import de.team33.cmd.files.main.common.Output;
import de.team33.cmd.files.main.common.RequestException;
import de.team33.patterns.enums.alpha.Values;
import de.team33.patterns.io.alpha.FileEntry;
import de.team33.patterns.io.alpha.FilePolicy;

import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static de.team33.cmd.files.main.job.Util.cmdLine;
import static de.team33.cmd.files.main.job.Util.cmdName;

class Listing implements Runnable {

    static final String EXCERPT = "List names of files of a specific type";

    private final Output context;
    private final Set<String> aspects;

    private Listing(final Output context, final Aspect aspect, final Path path, final FileType type) {
        final Function<FileEntry, String> mapping = aspect.mapping(type);
        this.context = context;
        this.aspects = FileEntry.of(path, FilePolicy.RESOLVE_SYMLINKS)
                                .entries()
                                .stream()
                                .filter(type::isTypeOf)
                                .map(mapping)
                                .filter(Objects::nonNull)
                                .collect(Collectors.toCollection(TreeSet::new));
    }

    static Runnable job(final Output out, final List<String> args) throws RequestException {
        assert 1 < args.size();
        assert Regular.LIST.name().equalsIgnoreCase(args.get(1));
        // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
        final int size = args.size();
        if (5 == size) {
            return new Listing(out, Aspect.of(args.get(2)), Path.of(args.get(3)), FileType.parse(args.get(4)));
        } else {
            throw RequestException.format(Listing.class, "Listing.txt", cmdLine(args), cmdName(args));
        }
    }

    @Override
    public final void run() {
        aspects.forEach(line -> context.printf("%s%n", line));
    }

    private enum Aspect {

        N(fileType -> fileType::toPureName),
        X(fileType -> fileType::toExtension),
        NX(fileType -> FileEntry::name);

        private static final Values<Aspect> VALUES = Values.of(Aspect.class);

        private final Function<FileType, Function<FileEntry, String>> toExtraction;

        Aspect(Function<FileType, Function<FileEntry, String>> toExtraction) {
            this.toExtraction = toExtraction;
        }

        private static Supplier<RuntimeException> newNoSuchElementException(final String value) {
            return () -> new NoSuchElementException("no Aspect specified for '" + value + "'");
        }

        private static Aspect of(final String value) {
            return VALUES.findAny(item -> value.equalsIgnoreCase(item.name()))
                         .orElseThrow(newNoSuchElementException(value));
        }

        Function<FileEntry, String> mapping(final FileType type) {
            return toExtraction.apply(type);
        }
    }
}
