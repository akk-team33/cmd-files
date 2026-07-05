package de.team33.patterns.directories.iocaste;

import de.team33.testing.Supply;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Comparator;
import java.util.List;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StringOrderTest {

    private static final Supply SUPPLY = new Supply();
    private static final List<UnaryOperator<String>> OPERATORS = List.of(UnaryOperator.identity(),
                                                                         String::toLowerCase,
                                                                         String::toUpperCase);
    private static final List<String> STRINGS = triple(Stream.generate(SUPPLY::anyString)
                                                             .limit(33)
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

    static Stream<Case> cases() {
        return Stream.of(caseOf(StringOrder.RESPECT_CASE, String::compareTo),
                         caseOf(StringOrder.IGNORE_CASE, String::compareToIgnoreCase),
                         caseOf(StringOrder.DEFAULT, StringOrderTest::combined));
    }

    private static Case caseOf(final Comparator<String> order, final Comparator<String> expected) {
        return new Case(order, STRINGS.stream().sorted(expected).toList());
    }

    @ParameterizedTest
    @MethodSource("cases")
    final void test(final Case given) {
        final List<String> result = STRINGS.stream()
                                           .sorted(given.order)
                                           .toList();
        // System.out.println(result);
        assertEquals(given.expected, result);
    }

    record Case(Comparator<String> order, List<String> expected) {
    }
}