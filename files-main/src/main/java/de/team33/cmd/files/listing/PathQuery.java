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
    private final Depth depth;
    private final NameFilter nameFilter;

    private PathQuery(FileEntry baseEntry, Depth depth, NameFilter nameFilter) {
        this.baseEntry = baseEntry;
        this.depth = depth;
        this.nameFilter = nameFilter;
    }

    public static PathQuery compose(final Path basePath, final Depth depth, final String namePattern) {
        return compose(FileEntry.resolved(basePath), depth, namePattern);
    }

    static PathQuery compose(final FileEntry baseEntry, final Depth depth, final String namePattern) {
        return new PathQuery(baseEntry, depth, NameFilter.parse(namePattern));
    }

    public static PathQuery parse(final String queryString) {
        return parse(Path.of(queryString));
    }

    private static PathQuery parse(final Path queryPath) {
        final FileEntry queryEntry = FileEntry.resolved(queryPath);
        if (queryEntry.isDirectory()) {
            return compose(queryEntry, Depth.FLAT, STD_NAME_PATTERN);
        } else {
            return parse(queryEntry);
        }
    }

    private static PathQuery parse(final FileEntry queryEntry) {
        final String queryTail = queryEntry.name();
        final Path queryHead = queryEntry.path().getParent();
        return switch (queryTail) {
            case DEEP_VISIBLE_WILDCARD -> compose(queryHead, Depth.DEEP_VISIBLE, STD_NAME_PATTERN);
            case DEEP_ALL_WILDCARD -> compose(queryHead, Depth.DEEP, STD_NAME_PATTERN);
            default -> parse(queryHead, queryTail);
        };
    }

    private static PathQuery parse(final Path queryHead, final String queryTail) {
        return switch (queryHead.getFileName().toString()) {
            case DEEP_VISIBLE_WILDCARD -> compose(queryHead.getParent(), Depth.DEEP_VISIBLE, queryTail);
            case DEEP_ALL_WILDCARD -> compose(queryHead.getParent(), Depth.DEEP, queryTail);
            default -> compose(queryHead, Depth.FLAT, queryTail);
        };
    }

    public final Stream<FileEntry> stream() {
        return depth.stream(baseEntry)
                    .filter(nameFilter::test);
    }

    private List<Object> toList() {
        return List.of(baseEntry.path(), depth, nameFilter);
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

