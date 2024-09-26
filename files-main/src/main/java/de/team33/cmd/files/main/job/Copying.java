package de.team33.cmd.files.main.job;

import de.team33.cmd.files.main.common.Output;
import de.team33.cmd.files.main.common.RequestException;
import de.team33.patterns.enums.alpha.Values;
import de.team33.patterns.io.alpha.FileEntry;
import de.team33.patterns.io.alpha.FileIndex;
import de.team33.patterns.io.alpha.FilePolicy;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static de.team33.cmd.files.main.job.Util.cmdLine;
import static de.team33.cmd.files.main.job.Util.cmdName;

class Copying implements Runnable {

    static final String EXCERPT = "Copy files and their relative file structure.";
    private static final FilePolicy POLICY = FilePolicy.DISTINCT_SYMLINKS;

    private final Output out;
    private final Set<Strategy> strategies;
    private final Path source;
    private final Path target;
    private final Map<String, Exception> problems = new TreeMap<>();

    private Copying(final Output out, final Set<Strategy> strategies, final Path source, final Path target) {
        this.out = out;
        this.strategies = strategies;
        this.source = source;
        this.target = target;
    }

    public static Runnable job(final Output out, final List<String> args) throws RequestException {
        assert 1 < args.size();
        assert Regular.COPY.name().equalsIgnoreCase(args.get(1));
        // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
        if (5 == args.size()) {
            final Set<Strategy> strategies = Strategy.parse(args.get(2));
            final Path source = Path.of(args.get(3));
            final Path target = Path.of(args.get(4));
            return new Copying(out, strategies, source, target);
        }
        throw RequestException.format(Listing.class, "Copying.txt", cmdLine(args), cmdName(args));
    }

    private Stream<Path> relatives(final Path path) {
        return Stream.of(source, target)
                     .filter(path::startsWith)
                     .map(root -> root.relativize(path));
    }

    @Override
    public final void run() {
        FileIndex.of(List.of(source, target), POLICY)
                 .entries()
                 .parallel()
                 .filter(FileEntry::isRegularFile)
                 .map(FileEntry::path)
                 .flatMap(this::relatives)
                 .map(Path::toString)
                 .collect(Collectors.toCollection(TreeSet::new))
                 .forEach(this::copy);
    }

    private void copy(final String relative) {
        out.printf("%s ...", relative);
        final FileEntry srcEntry = FileEntry.of(source.resolve(relative), POLICY);
        if (srcEntry.isRegularFile()) {
            out.printf(" %s%n", copyRegular(srcEntry, relative));
        } else {
            out.printf(" source is %s%n", srcEntry.type());
        }
    }

    private String copyRegular(final FileEntry srcEntry, final String relative) {
        final FileEntry tgtEntry = FileEntry.of(target.resolve(relative), FilePolicy.DISTINCT_SYMLINKS);
        if (strategies.stream().anyMatch(strategy -> strategy.canCopy(srcEntry, tgtEntry))) {
            return copyRegular(srcEntry, tgtEntry.path(), relative);
        } else {
            return "skipped";
        }
    }

    private final Set<Path> directories = new HashSet<>(0);

    private String copyRegular(final FileEntry srcEntry, final Path tgtPath, final String relative) {
        try {
            final Path tgtParent = tgtPath.getParent();
            if (directories.add(tgtParent)) {
                Files.createDirectories(tgtParent);
            }
            Files.copy(srcEntry.path(), tgtPath, StandardCopyOption.REPLACE_EXISTING);
            Files.setLastModifiedTime(tgtPath, FileTime.from(srcEntry.lastModified()));
            return "ok";
        } catch (final IOException e) {
            problems.put(relative, e);
            return String.format("failed:%n" +
                                 "   exception : %s%n" +
                                 "   message   : %s", e.getClass().getCanonicalName(), e.getMessage());
        }
    }

    private enum Strategy {
        C((left, right) -> !right.exists()),
        U((left, right) -> right.isRegularFile() && left.lastModified().compareTo(right.lastAccess()) > 0),
        R((left, right) -> false), // TODO
        D((left, right) -> false);

        private static final Values<Strategy> VALUES = Values.of(Strategy.class);
        private static final Supplier<EnumSet<Strategy>> NEW_SET = () -> EnumSet.noneOf(Strategy.class);

        private final BiPredicate<FileEntry, FileEntry> ability;

        Strategy(BiPredicate<FileEntry, FileEntry> ability) {
            this.ability = ability;
        }

        private static Set<Strategy> parse(final String strategy) {
            final String upperStrategy = strategy.toUpperCase();
            return VALUES.findAll(value -> upperStrategy.contains(value.name()))
                         .collect(Collectors.toCollection(NEW_SET));
        }

        private boolean canCopy(final FileEntry srcEntry, final FileEntry tgtEntry) {
            return ability.test(srcEntry, tgtEntry);
        }
    }
}
