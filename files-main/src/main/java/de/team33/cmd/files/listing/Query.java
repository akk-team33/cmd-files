package de.team33.cmd.files.listing;

import de.team33.patterns.io.adrastea.FileEntry;

import java.nio.file.Path;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class Query {

    private static final String DEEP_WILDCARD = "**";
    private static final NameFilter STD_NAME_FILTER = NameFilter.parse("*");

    private final FileEntry baseEntry;
    private final Depth depth;
    private final NameFilter nameFilter;

    private Query(final FileEntry baseEntry, final Depth depth, final NameFilter nameFilter) {
        this.baseEntry = baseEntry;
        this.depth = depth;
        this.nameFilter = nameFilter;
    }

    public static Query parse(final String queryString) {
        return parse(Path.of(queryString));
    }

    private static Query parse(final Path queryPath) {
        final FileEntry queryEntry = FileEntry.resolved(queryPath);
        if (queryEntry.isDirectory()) {
            return compose(queryEntry.path(), Depth.FLAT, STD_NAME_FILTER);
        } else {
            return parse(queryEntry);
        }
    }

    private static Query parse(final FileEntry queryEntry) {
        final String queryTail = queryEntry.name();
        final Path queryHead = queryEntry.path().getParent();
        if (DEEP_WILDCARD.equals(queryTail)) {
            return compose(queryHead, Depth.DEEP, STD_NAME_FILTER);
        } else {
            return parse(queryHead, queryTail);
        }
    }

    private static Query parse(final Path queryHead, final String namePattern) {
        if (queryHead.endsWith(DEEP_WILDCARD)) {
            return compose(queryHead.getParent(), Depth.DEEP, namePattern);
        } else {
            return compose(queryHead, Depth.FLAT, namePattern);
        }
    }

    public static Query compose(final Path basePath, final Depth depth, final String namePattern) {
        return compose(basePath, depth, NameFilter.parse(namePattern));
    }

    public static Query compose(final Path basePath, final Depth depth, final NameFilter nameFilter) {
        return new Query(FileEntry.resolved(basePath), depth, nameFilter);
    }

    public final Path basePath() {
        return baseEntry.path();
    }

    public final Depth depth() {
        return depth;
    }

    public final NameFilter nameFilter() {
        return nameFilter;
    }

    public final Stream<FileEntry> stream() {
        return depth.stream(baseEntry)
                    .filter(filter());
    }

    private Predicate<FileEntry> filter() {
        return nameFilter::test;
    }

    private List<?> toList() {
        return List.of(baseEntry.path(), depth, nameFilter);
    }

    @Override
    public final boolean equals(final Object obj) {
        return (this == obj) || ((obj instanceof Query other) && toList().equals(other.toList()));
    }

    @Override
    public final int hashCode() {
        return toList().hashCode();
    }

    @Override
    public final String toString() {
        return toList().toString();
    }
}
