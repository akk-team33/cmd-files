package de.team33.patterns.files.atlas;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import static java.lang.Integer.signum;
import static org.junit.jupiter.api.Assertions.assertEquals;

class OrderTest {

    private static final List<String> NAMES =
            List.of("abc", "def", "ABC", "123");
    private static final Comparator<String> IGNORE_CASE =
            String::compareToIgnoreCase;
    private static final Comparator<String> ORDER_STRING =
            IGNORE_CASE.thenComparing(String::compareTo);
    private static final Comparator<Path> ORDER_PATH_BY_STRING =
            Comparator.comparing(Path::toString, ORDER_STRING);
    private static final Comparator<Path> ORDER_PATH_BY_NAME =
            Comparator.comparing(Path::getFileName, ORDER_PATH_BY_STRING)
                      .thenComparing(ORDER_PATH_BY_STRING);

    private static Stream<Path> paths() {
        return NAMES.stream().flatMap(OrderTest::paths);
    }

    private static Stream<Path> paths(final String name) {
        return NAMES.stream().map(dir -> Path.of(dir, name));
    }

    static Stream<PathCase> pathCases() {
        return Stream.of(List.of(PathOrder.DEFAULT, ORDER_PATH_BY_STRING),
                         List.of(PathOrder.BY_NAME, ORDER_PATH_BY_NAME)/**/)
                     .flatMap(OrderTest::pathCases)/*
                     .filter(pathCase -> 0 == pathCase.expected)*/;
    }

    private static Stream<PathCase> pathCases(final List<Comparator<Path>> orders) {
        return paths().flatMap(left -> pathCases(orders, left));
    }

    private static Stream<PathCase> pathCases(final List<Comparator<Path>> orders, final Path left) {
        return paths().map(right -> new PathCase(orders.get(0), left, right,
                                                 Integer.signum(orders.get(1).compare(left, right))));
    }

    @ParameterizedTest
    @MethodSource("pathCases")
    final void pathOrder(final PathCase given) {
        final var result = given.order.compare(given.left, given.right);
        assertEquals(given.expected, signum(result));
    }

    record PathCase(Comparator<Path> order, Path left, Path right, int expected) {
    }
}