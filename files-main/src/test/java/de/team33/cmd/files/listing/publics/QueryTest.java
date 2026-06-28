package de.team33.cmd.files.listing.publics;

import de.team33.cmd.files.listing.Depth;
import de.team33.cmd.files.listing.NameFilter;
import de.team33.cmd.files.listing.Query;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.file.Path;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class QueryTest /*extends ModifyingTestBase*/ {

    static Stream<Arguments> parseParameters() {
        return Stream.of(Arguments.of("", "", Depth.FLAT, "*"),
                         Arguments.of("*", "", Depth.FLAT, "*"),
                         Arguments.of("**", "", Depth.DEEP, "*"),
                         Arguments.of("**/*", "", Depth.DEEP, "*"),

                         Arguments.of(":", "", Depth.FLAT, ":"),
                         Arguments.of(":*", "", Depth.FLAT, ":*"),
                         Arguments.of("**/:", "", Depth.DEEP, ":"),
                         Arguments.of("**/:*", "", Depth.DEEP, ":*"),

                         Arguments.of(".", "", Depth.FLAT, "*"),
                         Arguments.of(".*", "", Depth.FLAT, ".*"),
                         Arguments.of("**/.", "", Depth.DEEP, "."),
                         Arguments.of("**/.*", "", Depth.DEEP, ".*"),

                         Arguments.of(".", "", Depth.FLAT, "*"),
                         Arguments.of("./*", "", Depth.FLAT, "*"),
                         Arguments.of("./**", "", Depth.DEEP, "*"),
                         Arguments.of("./**/*", "", Depth.DEEP, "*"),

                         Arguments.of("..", "..", Depth.FLAT, "*"),
                         Arguments.of("../*", "..", Depth.FLAT, "*"),
                         Arguments.of("../**", "..", Depth.DEEP, "*"),
                         Arguments.of("../**/*", "..", Depth.DEEP, "*"),

                         Arguments.of("target", "target", Depth.FLAT, "*"),
                         Arguments.of("target/*", "target", Depth.FLAT, "*"),
                         Arguments.of("target/**", "target", Depth.DEEP, "*"),
                         Arguments.of("target/**/*", "target", Depth.DEEP, "*"));
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

    record Expected(Path basePath, Depth depth, NameFilter nameFilter) {

        public static Expected of(final String basePath, final Depth depth, final String nameExpression) {
            final Path path = Path.of(basePath).toAbsolutePath().normalize();
            return new Expected(path, depth, NameFilter.parse(nameExpression));
        }
    }
}