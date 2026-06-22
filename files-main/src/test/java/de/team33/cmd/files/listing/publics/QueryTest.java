package de.team33.cmd.files.listing.publics;

import de.team33.cmd.files.listing.Depth;
import de.team33.cmd.files.listing.Query;
import de.team33.cmd.files.testing.ModifyingTestBase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.file.Path;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class QueryTest extends ModifyingTestBase {

    QueryTest() {
        super(RELATIVE, InitMode.FILL_LEFT_ONLY);
    }

    private static <E> Optional<E> opt(final E[] array, final int index) {
        return (index < array.length) ? Optional.of(array[index]) : Optional.empty();
    }

    private void parse(final Path path, final String pattern) {
        final Expected expected = new Expected(path, pattern);
        final Path queryPath = path.resolve(Path.of(pattern));
        final Query query = Query.parse(queryPath.toString());

        assertEquals(expected.subQueryString, query.subQueryString);
        assertEquals(expected.depth, query.depth);
        assertEquals(expected.basePath, query.basePath);
    }

    @Test
    final void parse_empty() {
        parse(Path.of(""), "");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "?", "wc:?", "rx:.", "**/?", "**/wc:?", "**/rx:.",
            "*", "wc:*", "rx:.*", "**/*", "**/wc:*", "**/rx:.*", "**", ""})
    final void parse_left(final String pattern) {
        parse(leftPath(), pattern);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "?", "wc:?", "rx:.", "**/?", "**/wc:?", "**/rx:.",
            "*", "wc:*", "rx:.*", "**/*", "**/wc:*", "**/rx:.*", "**", ""})
    final void parse_root(final String pattern) {
        parse(Path.of("/"), pattern);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "?", "wc:?", "rx:.", "**/?", "**/wc:?", "**/rx:.",
            "*", "wc:*", "rx:.*", "**/*", "**/wc:*", "**/rx:.*", "**", ""})
    final void parse_cwd(final String pattern) {
        parse(Path.of("."), pattern);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "?", "wc:?", "rx:.", "**/?", "**/wc:?", "**/rx:.",
            "*", "wc:*", "rx:.*", "**/*", "**/wc:*", "**/rx:.*", "**"})
    final void parse_empty(final String pattern) {
        parse(Path.of(""), pattern);
    }

    static class Expected {

        final Depth depth;
        final String subQueryString;
        final Path basePath;

        Expected(final Path path, final String pattern) {
            this.basePath = path.toAbsolutePath().normalize();
            if (pattern.isEmpty()) {
                this.subQueryString = "*";
                this.depth = Depth.FLAT;
            } else {
                if (pattern.startsWith("**")) {
                    this.subQueryString = opt(pattern.split("/"), 1).orElse("*");
                    this.depth = Depth.DEEP;
                } else {
                    this.subQueryString = pattern;
                    this.depth = Depth.FLAT;
                }
            }
        }
    }
}