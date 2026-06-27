package de.team33.cmd.files.listing.publics;

import de.team33.cmd.files.listing.NameQuery;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class NameQueryTest {

    private static final List<String> FILE_NAMES = List.of(
            "",
            ".",
            ":",
            "fileName",
            ".fileNAME",
            "filename.JPG",
            ".FILENAME.jpg",
            "fIlenAme.txt",
            ".FIleNAme.TXT");

    static Stream<Arguments> matchesCases() {
        return Stream.of(BaseCase.values())
                     .flatMap(NameQueryTest::matchesCases);
    }

    private static Stream<Arguments> matchesCases(final BaseCase baseCase) {
        return FILE_NAMES.stream()
                         .map(fileName -> Arguments.of(baseCase, fileName));
    }

    @Test
    void parse() {
        final NameQuery query = NameQuery.parse("*");
        assertNotNull(query);
    }

    @ParameterizedTest
    @MethodSource("matchesCases")
    final void matches(final BaseCase baseCase, final String fileName) {
        final Given given = baseCase.given(fileName);
        final boolean result = given.query().matches(given.fileName());
        System.out.printf("%s: '%s' -> '%s' -> %s%n", baseCase, baseCase.queryString, given.fileName, result);
        assertEquals(given.expected(), result);
    }

    enum BaseCase {

        EMPTY("", ""),
        DOT(".", "\\."),
        COLON(":", ""),
        ALL(":*", ".*"),
        ALL_VISIBLE("*", ".*"),
        ALL_HIDDEN(".*", "\\..*"),
        JPG(":*.jpg", ".*\\.jpg"),
        JPG_VISIBLE("*.jpg", ".*\\.jpg"),
        JPG_HIDDEN(".*.jpg", "\\..*\\.jpg"),
        FILE(":file*.*", "file.*\\..*"),
        NAME_VISIBLE("*name.*", ".*name\\..*"),
        FILENAME_HIDDEN(".filename*", "\\.filename.*");

        private final String queryString;
        private final NameQuery query;
        private final Pattern pattern;

        BaseCase(final String queryString, final String regExp) {
            this.queryString = queryString;
            this.query = NameQuery.parse(queryString);
            this.pattern = Pattern.compile(regExp, Pattern.CASE_INSENSITIVE);
        }

        final Given given(final String fileName) {
            return new Given(query, fileName, matches(fileName));
        }

        private boolean matches(final String fileName) {
            return precondition(fileName) && pattern.matcher(fileName).matches();
        }

        private boolean precondition(final String fileName) {
            if (queryString.startsWith(".")) {
                return fileName.startsWith(".");
            } else if (queryString.startsWith(":")) {
                return true;
            } else {
                return !fileName.startsWith(".");
            }
        }
    }

    record Given(NameQuery query, String fileName, boolean expected) {
    }
}