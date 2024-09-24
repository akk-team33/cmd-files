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
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.BiPredicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static de.team33.cmd.files.main.job.Util.cmdLine;
import static de.team33.cmd.files.main.job.Util.cmdName;

class Copying implements Runnable {

    static final String EXCERPT = "Copy files and their relative file structure.";

    private final Output out;
    private final Set<Strategy> strategies;
    private final Path source;
    private final Path target;
    private final Map<String, Exception> problems = new TreeMap<>();

    public Copying(final Output out, final Set<Strategy> strategies, final Path source, final Path target) {
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
            final Set<Strategy> strategies = Strategy.of(args.get(2));
            final Path source = Path.of(args.get(3));
            final Path target = Path.of(args.get(4));
            return new Copying(out, strategies, source, target);
        }
        throw RequestException.format(Listing.class, "Copying.txt", cmdLine(args), cmdName(args));
    }

    @Override
    public final void run() {
        FileIndex.of(source, FilePolicy.RESOLVE_SYMLINKS)
                 .entries()
                 .forEach(this::copy);
    }

    private void copy(final FileEntry srcEntry) {
        final Path relative = source.relativize(srcEntry.path());
        out.printf("%s ...", relative);
        if (srcEntry.isDirectory()) {
            out.printf(" %s%n", createTargetDir(relative));
        } else if (srcEntry.isRegularFile()) {
            out.printf(" %s%n", copyRegular(srcEntry, relative));
        }
    }

    private String copyRegular(final FileEntry srcEntry, Path relative) {
        final FileEntry tgtEntry = FileEntry.of(target.resolve(relative), FilePolicy.DISTINCT_SYMLINKS);
        if (strategies.stream().anyMatch(strategy -> strategy.canCopy(srcEntry, tgtEntry))) {
            return copyRegular(srcEntry.path(), tgtEntry.path(), relative);
        } else {
            return "skipped";
        }
    }

    private String copyRegular(final Path srcPath, final Path tgtPath, final Path relative) {
        try {
            Files.copy(srcPath, tgtPath, StandardCopyOption.REPLACE_EXISTING);
            return "ok";
        } catch (final IOException e) {
            problems.put(relative.toString(), e);
            return String.format("failed: %s", e.getMessage());
        }
    }

    private String createTargetDir(final Path relative) {
        try {
            Files.createDirectories(target.resolve(relative));
            return "ok";
        } catch (final IOException e) {
            problems.put(relative.toString(), e);
            return String.format("failed: %s", e.getMessage());
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

        private static Set<Strategy> of(final String combo) {
            final String upperCombo = combo.toUpperCase();
            return VALUES.findAll(value -> upperCombo.contains(value.name()))
                         .collect(Collectors.toCollection(NEW_SET));
        }

        private boolean canCopy(final FileEntry srcEntry, final FileEntry tgtEntry) {
            return ability.test(srcEntry, tgtEntry);
        }
    }
}
