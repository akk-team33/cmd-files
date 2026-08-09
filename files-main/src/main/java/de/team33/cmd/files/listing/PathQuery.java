package de.team33.cmd.files.listing;

import de.team33.patterns.files.pluto.FileEntry;

import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public final class PathQuery {

    private static final System.Logger LOGGER = System.getLogger(PathQuery.class.getCanonicalName());

    private static final String DEEP_VISIBLE_WILDCARD = "**";
    private static final String DEEP_ALL_WILDCARD = ":**";
    private static final String STD_NAME_PATTERN = "*";
    private static final Report NO_REPORT = entry -> {
        // nothing to do by default
    };
    private static final Pattern SEPARATOR = Pattern.compile("[\\\\/]");
    private static final Pattern WIN_ROOT = Pattern.compile("[a-zA-Z]:");
    private final FileEntry baseEntry;
    private final Recursion recursion;
    private final NameFilter nameFilter;
    private final Report report;

    private PathQuery(final FileEntry baseEntry, final Recursion recursion,
                      final NameFilter nameFilter, final Report report) {
        this.baseEntry = baseEntry;
        this.recursion = recursion;
        this.nameFilter = nameFilter;
        this.report = report;
    }

    public static PathQuery compose(final Path basePath, final Recursion recursion, final String namePattern) {
        return compose(FileEntry.resolved(basePath), recursion, namePattern);
    }

    private static PathQuery compose(final FileEntry baseEntry, final Recursion recursion, final String namePattern) {
        return new PathQuery(baseEntry, recursion, NameFilter.parse(namePattern), NO_REPORT);
    }

    private static PathQuery compose(final Path root, final List<String> pathItems,
                                     final Recursion recursion, final String namePattern) {
        return compose(pathItems.stream().map(Path::of).reduce(root, Path::resolve), recursion, namePattern);
    }

    private static List<String> headOf(final List<String> items) {
        return items.subList(0, items.size() - 1);
    }

    private static List<String> tailOf(final List<String> items) {
        return items.subList(1, items.size());
    }

    public static PathQuery parse(final String queryString) {
        try {
            final var entry = FileEntry.resolved(Path.of(queryString));
            if (entry.isDirectory()) {
                return compose(entry, Recursion.FLAT, STD_NAME_PATTERN);
            }
        } catch (Exception e) {
            LOGGER.log(System.Logger.Level.INFO, e);
        }
        final List<String> items = SEPARATOR.splitAsStream(queryString).toList();
        if (items.isEmpty()) {
            return parse(Path.of("/"), items);
        } else {
            final String head = items.get(0);
            if (head.isEmpty() && 1 < items.size()) {
                return parse(Path.of("/"), tailOf(items));
            } else if (WIN_ROOT.matcher(head).matches()) {
                return parse(Path.of(head.toUpperCase()), tailOf(items));
            } else {
                return parse(Path.of(""), items);
            }
        }
    }

    private static PathQuery parse(final Path root, final List<String> items) {
        if (items.isEmpty()) {
            return compose(root, Recursion.FLAT, STD_NAME_PATTERN);
        }
        final Split split = Split.of(items);
        return parse(root, split);
    }

    private static PathQuery parse(final Path root, final Split split) {
        return switch (split.name) {
            case DEEP_VISIBLE_WILDCARD -> compose(root, split.parent, Recursion.VISIBLE, STD_NAME_PATTERN);
            case DEEP_ALL_WILDCARD -> compose(root, split.parent, Recursion.DEEP, STD_NAME_PATTERN);
            default -> parse(root, Split.of(split.parent), split.name);
        };
    }

    private static PathQuery parse(final Path root, final Split split, final String namePattern) {
        final String name = (null == split.name) ? "" : split.name;
        return switch (name) {
            case DEEP_VISIBLE_WILDCARD -> compose(root, split.parent, Recursion.VISIBLE, namePattern);
            case DEEP_ALL_WILDCARD -> compose(root, split.parent, Recursion.DEEP, namePattern);
            default -> compose(root, split.path, Recursion.FLAT, namePattern);
        };
    }

    public final PathQuery reporting(final Report report) {
        return new PathQuery(baseEntry, recursion, nameFilter, report);
    }

    public final FileEntry baseEntry() {
        return baseEntry;
    }

    public final Recursion recursion() {
        return recursion;
    }

    public final Stream<FileEntry> stream() {
        return recursion.stream(baseEntry)
                        .peek(report::addTotal)
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

    private record Split(List<String> path, List<String> parent, String name) {
        static Split of(final List<String> items) {
            if (items.isEmpty()) {
                return new Split(items, null, null);
            } else {
                return new Split(items, headOf(items), items.get(items.size() - 1));
            }
        }
    }
}

