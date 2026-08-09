package de.team33.cmd.files.listing.publics;

import de.team33.cmd.files.listing.PathQuery;
import de.team33.cmd.files.listing.Recursion;
import de.team33.patterns.files.pluto.FileEntry;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static java.util.function.Predicate.not;
import static org.junit.jupiter.api.Assertions.*;

class PathQueryTest {

    static Stream<ParseCase> parseCases() {
        return Stream.of(parseCase("/", "/", Recursion.FLAT, "*"),
                         parseCase("/**", "/", Recursion.VISIBLE, "*"),
                         parseCase("/path/to/:**/*.img", "/path/to", Recursion.DEEP, "*.img"),
                         parseCase("C:\\", "C:/", Recursion.FLAT, "*"),
                         parseCase("C:\\**", "C:/", Recursion.VISIBLE, "*"),
                         parseCase("C:\\path\\to\\:**\\:*.img", "C:/path/to", Recursion.DEEP, ":*.img"),
                         parseCase("", ".", Recursion.FLAT, "*"),
                         parseCase("*", ".", Recursion.FLAT, "*"),
                         parseCase("**", ".", Recursion.VISIBLE, "*"),
                         parseCase(":**", ".", Recursion.DEEP, "*"),
                         parseCase("**/", ".", Recursion.VISIBLE, "*"),
                         parseCase(":**/", ".", Recursion.DEEP, "*"),
                         parseCase("**/*", ".", Recursion.VISIBLE, "*"),
                         parseCase(":**/*.java", ".", Recursion.DEEP, "*.java"),
                         parseCase("**/:*.txt", ".", Recursion.VISIBLE, ":*.txt"),
                         parseCase(":**/:a*", ".", Recursion.DEEP, ":a*"),
                         parseCase("**/.*", ".", Recursion.VISIBLE, ".*"),
                         parseCase(":**/.*", ".", Recursion.DEEP, ".*"),
                         parseCase("..", "..", Recursion.FLAT, "*"),
                         parseCase("../*", "..", Recursion.FLAT, "*"),
                         parseCase("../**", "..", Recursion.VISIBLE, "*"),
                         parseCase("../:**", "..", Recursion.DEEP, "*"),
                         parseCase("../**/", "..", Recursion.VISIBLE, "*"),
                         parseCase("../:**/", "..", Recursion.DEEP, "*"),
                         parseCase("../**/*", "..", Recursion.VISIBLE, "*"),
                         parseCase("../:**/*", "..", Recursion.DEEP, "*"),
                         parseCase("../**/:*", "..", Recursion.VISIBLE, ":*"),
                         parseCase("../:**/:*", "..", Recursion.DEEP, ":*"),
                         parseCase("../**/.*", "..", Recursion.VISIBLE, ".*"),
                         parseCase("../:**/.*", "..", Recursion.DEEP, ".*"),
                         parseCase("path/to/../xo/:**/.*", "path/xo", Recursion.DEEP, ".*"));
    }

    private static ParseCase parseCase(final String pattern,
                                       final String basePath, final Recursion recursion, final String namePattern) {
        return new ParseCase(pattern, PathQuery.compose(Path.of(basePath), recursion, namePattern));
    }

    static Stream<StreamCase> streamCases() {
        return parseCases().filter(not(given -> "/**".equals(given.pattern)))
                           .map(PathQueryTest::streamCase);
    }

    private static StreamCase streamCase(final ParseCase parseCase) {
        return new StreamCase(PathQuery.parse(parseCase.pattern),
                              parseCase.expected.stream()
                                                .map(FileEntry::path)
                                                .toList());
    }

    @ParameterizedTest
    @MethodSource("parseCases")
    final void parse(final ParseCase given) {
        final PathQuery result = PathQuery.parse(given.pattern);
        assertEquals(given.expected, result);
    }

    @ParameterizedTest
    @MethodSource("parseCases")
    final void equals(final ParseCase given) {
        // just for test coverage ...
        // noinspection EqualsWithItself
        assertEquals(given.expected, given.expected);

        assertEquals(given.expected, PathQuery.parse(given.pattern));
        assertNotSame(given.expected, PathQuery.parse(given.pattern));
        assertNotEquals(given.expected, PathQuery.parse(UUID.randomUUID().toString()));

        // just for test coverage ...
        // noinspection AssertBetweenInconvertibleTypes
        assertNotEquals(given.expected, UUID.randomUUID().toString());
    }

    @ParameterizedTest
    @MethodSource("parseCases")
    final void hashCode(final ParseCase given) {
        final PathQuery result = PathQuery.parse(given.pattern);
        assertEquals(given.expected.hashCode(), result.hashCode());
    }

    @ParameterizedTest
    @MethodSource("streamCases")
    final void stream(final StreamCase given) {
        final List<Path> result = given.query.stream()
                                             .map(FileEntry::path)
                                             .toList();
        assertEquals(given.expected, result);
    }

    record StreamCase(PathQuery query, List<Path> expected) {
    }

    record ParseCase(String pattern, PathQuery expected) {
    }
}