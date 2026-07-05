package de.team33.patterns.directories.iocaste.publics;

import de.team33.patterns.directories.iocaste.PathOrder;
import de.team33.testing.Supply;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PathOrderTest {

    private static final Supply SUPPLY = new Supply();
    private static final Comparator<Path> EXPECTED = Comparator.comparing(Path::toString, PathOrderTest::combined);
    private static final List<UnaryOperator<String>> OPERATORS = List.of(UnaryOperator.identity(),
                                                                         String::toLowerCase,
                                                                         String::toUpperCase);
    private static final String CHARACTERS = "abcdefg_ABCDEFG-01234";
    private static final List<String> STRINGS = triple(Stream.generate(() -> SUPPLY.anyString(8, CHARACTERS))
                                                             .limit(5)
                                                             .toList());

    private static List<String> triple(final List<String> single) {
        return OPERATORS.stream()
                        .flatMap(op -> single.stream().map(op))
                        .toList();
    }

    private static int combined(final String left, final String right) {
        final int result = left.compareToIgnoreCase(right);
        return (0 == result) ? left.compareTo(right) : result;
    }

    private static Stream<Path> paths() {
        return STRINGS.stream().flatMap(PathOrderTest::paths);
    }

    private static Stream<Path> paths(final String name) {
        return STRINGS.stream().map(dir -> Path.of(dir, name));
    }

    static Stream<Case> cases() {
        return Stream.of(caseOf(PathOrder.DEFAULT, EXPECTED),
                         caseOf(PathOrder.BY_NAME, Comparator.comparing(Path::getFileName, EXPECTED)
                                                             .thenComparing(EXPECTED)));
    }

    private static Case caseOf(final Comparator<Path> order, final Comparator<Path> expected) {
        return new Case(order, paths().sorted(expected).toList());
    }

    @ParameterizedTest
    @MethodSource("cases")
    final void test(final Case given) {
        final List<Path> result = paths().sorted(given.order)
                                         .toList();
        // System.out.println(result);
        assertEquals(given.expected, result);
    }

    record Case(Comparator<Path> order, List<Path> expected) {
    }
}