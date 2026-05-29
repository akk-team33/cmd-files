package de.team33.cmd.files.balancing;

import de.team33.patterns.io.adrastea.FileEntry;
import de.team33.patterns.io.adrastea.LinkHandling;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Relatives {

    private static final FileEntry.Streamer STREAMER = FileEntry.streamer(LinkHandling.ORIGINAL);
    private final Function<String, Relative> toRelative;
    private final List<FileEntry> entries;

    public static Set<String> collect(final Path srcRoot, final Path tgtRoot) {
        return new Relatives(srcRoot, tgtRoot).collect();
    }

    public static Stream<Relative> stream(final Path srcRoot, final Path tgtRoot) {
        return new Relatives(srcRoot, tgtRoot).stream();
    }

    private Stream<Relative> stream() {
        return collect().stream().map(toRelative);
    }

    private Relatives(final Path srcRoot, final Path tgtRoot) {
        this.entries = List.of(FileEntry.original(srcRoot), FileEntry.original(tgtRoot));
        this.toRelative = relative -> new Relative(relative, srcRoot, tgtRoot);
    }

    private Set<String> collect() {
        return entries.stream()
                      .flatMap(STREAMER::stream)
                      .parallel()
                      .filter(FileEntry::isRegularFile)
                      .map(FileEntry::path)
                      .flatMap(this::relatives)
                      .map(Path::toString)
                      .collect(Collectors.toCollection(TreeSet::new));
    }

    private Stream<Path> relatives(final Path path) {
        return entries.stream()
                      .map(FileEntry::path)
                      .filter(path::startsWith)
                      .map(root -> root.relativize(path));
    }
}
