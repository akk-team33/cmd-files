package de.team33.cmd.files.matching;

import de.team33.cmd.files.common.Filter;
import de.team33.patterns.enums.pan.Values;
import de.team33.patterns.io.adrastea.FileEntry;

import java.util.TreeSet;
import java.util.function.Predicate;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toCollection;

public enum TypeFilter {

    F(FileEntry::isRegularFile),
    D(FileEntry::isDirectory),
    S(FileEntry::isSpecialFile),
    M(FileEntry::isMissing),
    L(FileEntry::isSymbolicLink),
    A(Filter.positive());

    private static final Values<TypeFilter> VALUES = Values.of(TypeFilter.class);

    private final Predicate<FileEntry> filter;

    TypeFilter(final Predicate<FileEntry> filter) {
        this.filter = filter;
    }

    public static Predicate<FileEntry> parse(final String pattern) {
        final var set = IntStream.range(0, pattern.length())
                                 .mapToObj(index -> pattern.substring(index, index + 1))
                                 .map(TypeFilter::of)
                                 .collect(toCollection(TreeSet::new));
        if (set.contains(A)) {
            return Filter.positive();
        } else {
            return set.stream()
                      .map(value -> value.filter)
                      .reduce(Filter.negative(), Predicate::or);
        }
    }

    private static TypeFilter of(final String name) {
        return VALUES.findFirst(value -> value.name().equalsIgnoreCase(name))
                     .orElseThrow(() -> newException(name));
    }

    private static IllegalArgumentException newException(final String name) {
        return new IllegalArgumentException(
                "Expected one of %s - but was %s".formatted(VALUES.stream().toList(), name));
    }
}
