package de.team33.cmd.files.listing.publics;

import de.team33.cmd.files.listing.Depth;
import de.team33.cmd.files.listing.NameFilter;
import de.team33.cmd.files.listing.Query;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static de.team33.cmd.files.listing.Depth.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class QueryTest /*extends ModifyingTestBase*/ {

    private static final List<List<String>> PARSE_CASES =
            List.of(List.of("", ".", FLAT.name(), "*"),
                    List.of("*", ".", FLAT.name(), "*"),
                    List.of("**", ".", DEEP.name(), "*"),
                    List.of("**/*", ".", DEEP.name(), "*"));

    static Stream<Arguments> parseParameters() {
        return Stream.of(Arguments.of("", "", FLAT, "*"),
                         Arguments.of("*", "", FLAT, "*"),
                         Arguments.of("**", "", DEEP, "*"),
                         Arguments.of("**/*", "", DEEP, "*"),

                         Arguments.of(":", "", FLAT, ":"),
                         Arguments.of(":*", "", FLAT, ":*"),
                         Arguments.of("**/:", "", DEEP, ":"),
                         Arguments.of("**/:*", "", DEEP, ":*"),

                         Arguments.of(".", "", FLAT, "*"),
                         Arguments.of(".*", "", FLAT, ".*"),
                         Arguments.of("**/.", "", DEEP, "."),
                         Arguments.of("**/.*", "", DEEP, ".*"),

                         Arguments.of(".", "", FLAT, "*"),
                         Arguments.of("./*", "", FLAT, "*"),
                         Arguments.of("./**", "", DEEP, "*"),
                         Arguments.of("./**/*", "", DEEP, "*"),

                         Arguments.of("..", "..", FLAT, "*"),
                         Arguments.of("../*", "..", FLAT, "*"),
                         Arguments.of("../**", "..", DEEP, "*"),
                         Arguments.of("../**/*", "..", DEEP, "*"),

                         Arguments.of("target", "target", FLAT, "*"),
                         Arguments.of("target/*", "target", FLAT, "*"),
                         Arguments.of("target/**", "target", DEEP, "*"),
                         Arguments.of("target/**/*", "target", DEEP, "*"));
    }

    static Stream<ParseCase> parseCases() {
        return PARSE_CASES.stream()
                          .map(QueryTest::parseCase);
    }

    private static ParseCase parseCase(final List<String> list) {
        final Query expected = Query.compose(Path.of(list.get(1)), valueOf(list.get(2)), list.get(3));
        return new ParseCase(list.get(0), expected);
    }

    @ParameterizedTest
    @MethodSource("parseCases")
    final void parse(final ParseCase given) {
        final Query query = Query.parse(given.queryString);
        assertEquals(given.expected, query);
    }

    @ParameterizedTest
    @MethodSource("parseParameters")
    final void parse(final String queryString, final String basePath, final Depth depth, final String nameExpression) {
        final Expected expected = Expected.of(basePath, depth, nameExpression);
        System.out.printf(("Given :%n" +
                           "    queryString : '%s'%n" +
                           "    expected :%n" +
                           "        basePath :    <%s>%n" +
                           "        depth:        %s%n" +
                           "        nameFilter:   %s%n"),
                          queryString, expected.basePath, expected.depth, expected.nameFilter);

        final Query query = Query.parse(queryString);

        assertEquals(expected.basePath, query.basePath());
        assertEquals(expected.depth, query.depth());
        assertEquals(expected.nameFilter, query.nameFilter());
    }

    record ParseCase(String queryString, Query expected) {
    }

    record Expected(Path basePath, Depth depth, NameFilter nameFilter) {

        public static Expected of(final String basePath, final Depth depth, final String nameExpression) {
            final Path path = Path.of(basePath).toAbsolutePath().normalize();
            return new Expected(path, depth, NameFilter.parse(nameExpression));
        }
    }
}