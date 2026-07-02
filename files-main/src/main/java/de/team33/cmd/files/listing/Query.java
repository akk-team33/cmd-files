package de.team33.cmd.files.listing;

import de.team33.cmd.files.matching.NameMatcher;
import de.team33.patterns.io.iocaste.FileEntry;
import de.team33.patterns.io.iocaste.LinkHandling;

import java.nio.file.Path;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class Query {

    private static final String DEEP_WILDCARD = "**";
    private static final FileEntry.Lister LISTER = FileEntry.lister(LinkHandling.ORIGINAL);
    private static final FileEntry.Streamer STREAMER = FileEntry.streamer(LISTER);

    private final FileEntry baseEntry;
    private final Depth depth;
    private final String subQueryString;

    private Query(final Path basePath, final Depth depth, final String subQueryString) {
        this.baseEntry = FileEntry.resolved(basePath);
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

    public final FileEntry baseEntry() {
        return baseEntry;
    }

    public final Path basePath() {
        return baseEntry.path();
    }

    public final Depth depth() {
        return depth;
    }

    public final String subQueryString() {
        return subQueryString;
    }

    public Stream<FileEntry> stream() {
        return depth.stream(baseEntry)
                    .filter(filter());
    }

    private Predicate<FileEntry> filter() {
        return NameMatcher.parse(subQueryString)
                          .toFileEntryFilter();
    }
}
