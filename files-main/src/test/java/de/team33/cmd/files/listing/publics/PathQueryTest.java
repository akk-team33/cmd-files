package de.team33.cmd.files.listing.publics;

import de.team33.cmd.files.listing.Depth;
import de.team33.cmd.files.listing.PathQuery;
import de.team33.patterns.directories.iocaste.FileEntry;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class PathQueryTest {

    static Stream<ParseCase> parseCases() {
        return Stream.of(parseCase("", ".", Depth.FLAT, "*"),
                         parseCase("*", ".", Depth.FLAT, "*"),
                         parseCase("**", ".", Depth.DEEP_VISIBLE, "*"),
                         parseCase(":**", ".", Depth.DEEP, "*"),
                         parseCase("**/", ".", Depth.DEEP_VISIBLE, "*"),
                         parseCase(":**/", ".", Depth.DEEP, "*"),
                         parseCase("**/*", ".", Depth.DEEP_VISIBLE, "*"),
                         parseCase(":**/*.java", ".", Depth.DEEP, "*.java"),
                         parseCase("**/:*.txt", ".", Depth.DEEP_VISIBLE, ":*.txt"),
                         parseCase(":**/:a*", ".", Depth.DEEP, ":a*"),
                         parseCase("**/.*", ".", Depth.DEEP_VISIBLE, ".*"),
                         parseCase(":**/.*", ".", Depth.DEEP, ".*"),
                         parseCase("..", "..", Depth.FLAT, "*"),
                         parseCase("../*", "..", Depth.FLAT, "*"),
                         parseCase("../**", "..", Depth.DEEP_VISIBLE, "*"),
                         parseCase("../:**", "..", Depth.DEEP, "*"),
                         parseCase("../**/", "..", Depth.DEEP_VISIBLE, "*"),
                         parseCase("../:**/", "..", Depth.DEEP, "*"),
                         parseCase("../**/*", "..", Depth.DEEP_VISIBLE, "*"),
                         parseCase("../:**/*", "..", Depth.DEEP, "*"),
                         parseCase("../**/:*", "..", Depth.DEEP_VISIBLE, ":*"),
                         parseCase("../:**/:*", "..", Depth.DEEP, ":*"),
                         parseCase("../**/.*", "..", Depth.DEEP_VISIBLE, ".*"),
                         parseCase("../:**/.*", "..", Depth.DEEP, ".*"));
    }

    private static ParseCase parseCase(final String pattern,
                                       final String basePath, final Depth depth, final String namePattern) {
        return new ParseCase(pattern, PathQuery.compose(Path.of(basePath), depth, namePattern));
    }

    static Stream<StreamCase> streamCases() {
        return parseCases().map(PathQueryTest::streamCase);
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