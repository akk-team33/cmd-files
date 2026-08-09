package de.team33.cmd.files.balancing;

import de.team33.patterns.files.pluto.FileEntry;
import de.team33.patterns.files.styx.Styx;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toCollection;

public class Relatives {

    private final Function<String, Relative> toRelative;
    private final List<FileEntry> entries;

    private Relatives(final Path srcRoot, final Path tgtRoot) {
        this.entries = List.of(FileEntry.original(srcRoot), FileEntry.original(tgtRoot));
        this.toRelative = relative -> new Relative(relative, srcRoot, tgtRoot);
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
        return entries.stream()
                      .flatMap(Styx::stream)
                      .parallel()
                      .filter(FileEntry::isRegularFile)
                      .map(FileEntry::path)
                      .flatMap(this::relatives)
                      .map(Path::toString)
                      .collect(toCollection(TreeSet::new));
    }

    private Stream<Path> relatives(final Path path) {
        return entries.stream()
                      .map(FileEntry::path)
                      .filter(path::startsWith)
                      .map(root -> root.relativize(path));
    }
}
