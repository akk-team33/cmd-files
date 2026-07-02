package de.team33.cmd.files.sorting;

import de.team33.patterns.enums.pan.Values;
import de.team33.patterns.io.iocaste.FileEntry;

import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

import static java.util.Comparator.comparing;

public class Order {

    private static final String COLON = Pattern.quote(":");
    private static final List<Criterion> CRITERIA = List.of(Criterion.values());
    private static final List<Direction> DIRECTIONS = List.of(Direction.values());

    public static Comparator<FileEntry> parse(final String s) {
        final String[] parts = s.split(COLON, 2);
        final Criterion criterion = Criterion.VALUES.findAny(value -> value.name().equalsIgnoreCase(parts[0]))
                                                    .orElseThrow(() -> noCriterion(parts[0]));
        final Direction direction = (2 > parts.length)
                                    ? Direction.A
                                    : Direction.VALUES.findAny(value -> value.name().equalsIgnoreCase(parts[1]))
                                                      .orElseThrow(() -> noDirection(parts[1]));
        return direction.apply(criterion);
    }

    private static IllegalArgumentException noneOf(final List<?> expected, final String name) {
        return new IllegalArgumentException(
                "expected one of %s - but was %s".formatted(expected, name));
    }

    private static IllegalArgumentException noDirection(final String name) {
        return noneOf(DIRECTIONS, name);
    }

    private static IllegalArgumentException noCriterion(final String name) {
        return noneOf(CRITERIA, name);
    }

    private enum Direction {

        A(UnaryOperator.identity()),
        D(Comparator::reversed);

        private static final Values<Direction> VALUES = Values.of(Direction.class);

        private final UnaryOperator<Comparator<FileEntry>> operator;

        Direction(final UnaryOperator<Comparator<FileEntry>> operator) {
            this.operator = operator;
        }

        public Comparator<FileEntry> apply(final Criterion criterion) {
            return operator.apply(criterion);
        }
    }

    private enum Criterion implements Comparator<FileEntry> {

        P(Impl.PATH_ORDER),
        N(Impl.NAME_ORDER),
        D(Impl.DATE_ORDER),
        S(Impl.SIZE_ORDER);

        private static final Values<Criterion> VALUES = Values.of(Criterion.class);

        private final Comparator<FileEntry> backing;

        Criterion(final Comparator<FileEntry> backing) {
            this.backing = backing;
        }

        @Override
        public int compare(final FileEntry left, final FileEntry right) {
            return backing.compare(left, right);
        }

        private static class Impl {

            private static final Comparator<String> IGNORE_CASE = String::compareToIgnoreCase;
            private static final Comparator<String> RESPECT_CASE = String::compareTo;
            private static final Comparator<String> BASE_ORDER = IGNORE_CASE.thenComparing(RESPECT_CASE);
            private static final Comparator<Path> STRING_ORDER = comparing(Path::toString, BASE_ORDER);
            private static final Comparator<FileEntry> PATH_ORDER = comparing(FileEntry::path, STRING_ORDER);
            private static final Comparator<FileEntry> NAME_ORDER = comparing(FileEntry::name, BASE_ORDER).thenComparing(PATH_ORDER);
            private static final Comparator<FileEntry> DATE_ORDER = comparing(FileEntry::lastModified).thenComparing(PATH_ORDER);
            private static final Comparator<FileEntry> SIZE_ORDER = comparing(FileEntry::size).thenComparing(PATH_ORDER);
        }
    }
}
