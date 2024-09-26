package de.team33.cmd.files.main.balancing;

import de.team33.patterns.io.alpha.FileEntry;
import de.team33.patterns.io.alpha.FileIndex;
import de.team33.patterns.io.alpha.FilePolicy;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Relatives {

    public static final FilePolicy POLICY = FilePolicy.DISTINCT_SYMLINKS;

    private final FileIndex index;
    private final Function<String, Relative> toRelative;

    private Relatives(final Path srcRoot, final Path tgtRoot) {
        this.index = FileIndex.of(List.of(srcRoot, tgtRoot), POLICY);
        this.toRelative = relative -> new Relative(relative, srcRoot, tgtRoot, POLICY);
    }

    public static Set<String> collect(final Path srcRoot, final Path tgtRoot) {
        return new Relatives(srcRoot, tgtRoot).collect();
    }

    public static Stream<Relative> stream(final Path srcRoot, final Path tgtRoot) {
        return new Relatives(srcRoot, tgtRoot).stream();
    }

    private Stream<Relative> stream() {
        return collect().stream().map(toRelative);
    }

    private Set<String> collect() {
        return index.entries()
                    .parallel()
                    .filter(FileEntry::isRegularFile)
                    .map(FileEntry::path)
                    .flatMap(this::relatives)
                    .map(Path::toString)
                    .collect(Collectors.toCollection(TreeSet::new));
    }

    private Stream<Path> relatives(final Path path) {
        return index.roots()
                    .stream()
                    .map(FileEntry::path)
                    .filter(path::startsWith)
                    .map(root -> root.relativize(path));
    }
}
