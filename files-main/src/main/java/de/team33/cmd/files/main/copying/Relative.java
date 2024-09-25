package de.team33.cmd.files.main.copying;

import de.team33.patterns.io.alpha.FileEntry;
import de.team33.patterns.io.alpha.FileIndex;
import de.team33.patterns.io.alpha.FilePolicy;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;

public class Relative {

    private final Set<Path> result;
    private final FileIndex index;

    private Relative(final List<Path> roots) {
        this.result = Collections.synchronizedSet(new TreeSet<>());
        this.index = FileIndex.of(roots, FilePolicy.DISTINCT_SYMLINKS);
    }

    public static Set<Path> collect(final Path ... roots) {
        return new Relative(Arrays.asList(roots)).process().result;
    }

    private Relative process() {
        index.entries()
             .parallel()
             .filter(FileEntry::isRegularFile)
             .map(FileEntry::path)
             .flatMap(this::relative)
             .forEach(result::add);
        return this;
    }

    private Stream<Path> relative(final Path entry) {
        return index.roots()
                    .stream()
                    .map(FileEntry::path)
                    .filter(entry::startsWith)
                    .map(root -> root.relativize(entry));
    }
}
