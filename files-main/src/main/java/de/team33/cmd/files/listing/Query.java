package de.team33.cmd.files.listing;

import de.team33.patterns.io.adrastea.FileEntry;

import java.nio.file.Path;

public class Query {

    private static final String DEEP_WILDCARD = "**";

    private final Path basePath;
    private final Depth depth;
    private final String subQueryString;

    private Query(final Path basePath, final Depth depth, final String subQueryString) {
        this.basePath = basePath;
        this.depth = depth;
        this.subQueryString = subQueryString;
    }

    public static Query parse(final String queryString) {
        return parse(Path.of(queryString));
    }

    private static Query parse(final Path queryPath) {
        final FileEntry queryEntry = FileEntry.resolved(queryPath);
        if (queryEntry.isDirectory()) {
            return new Query(queryEntry.path(), Depth.FLAT, "*");
        } else {
            return parse(queryEntry);
        }
    }

    private static Query parse(final FileEntry queryEntry) {
        final String queryTail = queryEntry.name();
        final Path queryHead = queryEntry.path().getParent();
        if (DEEP_WILDCARD.equals(queryTail)) {
            return new Query(queryHead, Depth.DEEP, "*");
        } else {
            return parse(queryHead, queryTail);
        }
    }

    private static Query parse(final Path queryHead, final String queryName) {
        if (queryHead.endsWith(DEEP_WILDCARD)) {
            return new Query(queryHead.getParent(), Depth.DEEP, queryName);
        } else {
            return new Query(queryHead, Depth.FLAT, queryName);
        }
    }

    public final Path basePath() {
        return basePath;
    }

    public final Depth depth() {
        return depth;
    }

    public final String subQueryString() {
        return subQueryString;
    }
}
