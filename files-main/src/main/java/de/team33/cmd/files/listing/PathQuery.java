package de.team33.cmd.files.listing;

import de.team33.patterns.directories.iocaste.FileEntry;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

public final class PathQuery {

    private static final String DEEP_VISIBLE_WILDCARD = "**";
    private static final String DEEP_ALL_WILDCARD = ":**";
    private static final String STD_NAME_PATTERN = "*";

    private final FileEntry baseEntry;
    private final Recursion recursion;
    private final NameFilter nameFilter;

    private PathQuery(FileEntry baseEntry, Recursion recursion, NameFilter nameFilter) {
        this.baseEntry = baseEntry;
        this.recursion = recursion;
        this.nameFilter = nameFilter;
    }

    public static PathQuery compose(final Path basePath, final Recursion recursion, final String namePattern) {
        return compose(FileEntry.resolved(basePath), recursion, namePattern);
    }

    static PathQuery compose(final FileEntry baseEntry, final Recursion recursion, final String namePattern) {
        return new PathQuery(baseEntry, recursion, NameFilter.parse(namePattern));
    }

    public static PathQuery parse(final String queryString) {
        return parse(Path.of(queryString));
    }

    private static PathQuery parse(final Path queryPath) {
        final FileEntry queryEntry = FileEntry.resolved(queryPath);
        if (queryEntry.isDirectory()) {
            return compose(queryEntry, Recursion.NONE, STD_NAME_PATTERN);
        } else {
            return parse(queryEntry);
        }
    }

    private static PathQuery parse(final FileEntry queryEntry) {
        final String queryTail = queryEntry.name();
        final Path queryHead = queryEntry.path().getParent();
        return switch (queryTail) {
            case DEEP_VISIBLE_WILDCARD -> compose(queryHead, Recursion.VISIBLE, STD_NAME_PATTERN);
            case DEEP_ALL_WILDCARD -> compose(queryHead, Recursion.ALL, STD_NAME_PATTERN);
            default -> parse(queryHead, queryTail);
        };
    }

    private static PathQuery parse(final Path queryHead, final String queryTail) {
        return switch (queryHead.getFileName().toString()) {
            case DEEP_VISIBLE_WILDCARD -> compose(queryHead.getParent(), Recursion.VISIBLE, queryTail);
            case DEEP_ALL_WILDCARD -> compose(queryHead.getParent(), Recursion.ALL, queryTail);
            default -> compose(queryHead, Recursion.NONE, queryTail);
        };
    }

    public final Stream<FileEntry> stream() {
        return recursion.stream(baseEntry)
                        .filter(nameFilter::test);
    }

    private List<Object> toList() {
        return List.of(baseEntry.path(), recursion, nameFilter);
    }

    @Override
    public final boolean equals(Object obj) {
        return (this == obj) || ((obj instanceof PathQuery other) && toList().equals(other.toList()));
    }

    @Override
    public final int hashCode() {
        return toList().hashCode();
    }

    @Override
    public final String toString() {
        return "PathFilter" + toList();
    }
}

