package de.team33.cmd.files.listing.publics;

import de.team33.cmd.files.listing.NameFilter;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class NameFilterTest {

    private static final List<List<String>> PARSE_CASES =
            List.of(List.of("", "(?!\\.)", ""),
                    List.of(":", "", ""),
                    List.of(".", "\\Q.\\E", "\\."),
                    List.of("*", "(?!\\.).*", ".*"),
                    List.of(":*", ".*", ".*"),
                    List.of(".*", "\\Q.\\E.*", "\\..*"),
                    List.of("?", "(?!\\.).", "."),
                    List.of(":?", ".", "."),
                    List.of(".?", "\\Q.\\E.", "\\.."),
                    List.of("*.*", "(?!\\.).*\\Q.\\E.*", ".*\\..*"),
                    List.of(":*.*", ".*\\Q.\\E.*", ".*\\..*"),
                    List.of(".*.*", "\\Q.\\E.*\\Q.\\E.*", "\\..*\\..*"),
                    List.of("*.???", "(?!\\.).*\\Q.\\E...", ".*\\...."),
                    List.of(":*.???", ".*\\Q.\\E...", ".*\\...."),
                    List.of(".*.???", "\\Q.\\E.*\\Q.\\E...", "\\..*\\...."),
                    List.of("*.jpg", "(?!\\.).*\\Q.jpg\\E", ".*\\.jpg"),
                    List.of(":*.jpg", ".*\\Q.jpg\\E", ".*\\.jpg"),
                    List.of(".*.jpg", "\\Q.\\E.*\\Q.jpg\\E", "\\..*\\.jpg"),
                    List.of("file*.*", "(?!\\.)\\Qfile\\E.*\\Q.\\E.*", "file.*\\..*"),
                    List.of(":file*.*", "\\Qfile\\E.*\\Q.\\E.*", "file.*\\..*"),
                    List.of(".file*.*", "\\Q.file\\E.*\\Q.\\E.*", "\\.file.*\\..*"),
                    List.of("*name.*", "(?!\\.).*\\Qname.\\E.*", ".*name\\..*"),
                    List.of(":*name.*", ".*\\Qname.\\E.*", ".*name\\..*"),
                    List.of(".*name.*", "\\Q.\\E.*\\Qname.\\E.*", ".*name\\..*"));
    private static final List<String> FILE_NAMES = List.of(
            "",
            ".",
            ":",
            "fileName",
            ".fileNAME",
            "filename.JPG",
            ".FILENAME.jpg",
            "filename.jpeg",
            ".FILENAME.JPEG",
            "fIlenAme.txt",
            ".FIleNAme.TXT");
    @SuppressWarnings("unused")
    private static final String TEST_FORMAT = "Given:%n" +
                                              "    pattern : '%s'%n" +
                                              "    sample :  '%s'%n" +
                                              "    result :  <%s>%n";

    static Stream<Arguments> parseCases() {
        return PARSE_CASES.stream()
                          .map(list -> Arguments.of(list.get(0), list.get(1)));
    }

    static Stream<TestCase> testCases() {
        return PARSE_CASES.stream().flatMap(NameFilterTest::testCases);
    }

    private static Stream<TestCase> testCases(final List<String> parseCase) {
        final Pattern rxPattern = Pattern.compile(parseCase.get(2), Pattern.CASE_INSENSITIVE);
        final String wcPattern = parseCase.get(0);
        return FILE_NAMES.stream().map(sample -> new TestCase(wcPattern, sample,
                                                              expectation(wcPattern, rxPattern, sample)));
    }

    private static boolean expectation(final String wcPattern, final Pattern rxPattern, final String sample) {
        return precondition(wcPattern, sample) && rxPattern.matcher(sample).matches();
    }

    private static boolean precondition(final String wcPattern, final String sample) {
        if (wcPattern.startsWith(".")) {
            return sample.startsWith(".");
        } else if (wcPattern.startsWith(":")) {
            return true;
        } else {
            return !sample.startsWith(".");
        }
    }

    @ParameterizedTest
    @MethodSource("parseCases")
    final void equals(final String pattern, final String other) {
        final NameFilter expected = NameFilter.parse(pattern);
        //noinspection EqualsWithItself
        assertEquals(expected, expected);
        assertEquals(expected, NameFilter.parse(pattern));
        assertNotEquals(expected, NameFilter.parse(other));
        //noinspection AssertBetweenInconvertibleTypes
        assertNotEquals(expected, other);
    }

    @ParameterizedTest
    @MethodSource("parseCases")
    final void hashCode(final String pattern, @SuppressWarnings("unused") final String other) {
        assertEquals(NameFilter.parse(pattern).hashCode(), NameFilter.parse(pattern).hashCode());
    }

    @ParameterizedTest
    @MethodSource("parseCases")
    final void parse(final String pattern, final String expected) {
        final NameFilter filter = NameFilter.parse(pattern);
        assertEquals(expected, filter.toString());
    }

    @ParameterizedTest
    @MethodSource("testCases")
    final void test(final TestCase given) {
        // System.out.printf(TEST_FORMAT, given.pattern, given.sample, given.expected);
        final boolean result = given.filter().test(given.sample);
        assertEquals(given.expected, result);
    }

    @ParameterizedTest
    @MethodSource("testCases")
    final void testPath(final TestCase given) {
        // cases that won't work ...
        if ("".equals(given.sample) || ".".equals(given.sample)) return;

        final Path path = Path.of("target", given.sample);
        final boolean result = given.filter().test(path);
        assertEquals(given.expected, result);
    }

    record TestCase(String pattern, String sample, boolean expected) {

        public final NameFilter filter() {
            return NameFilter.parse(pattern);
        }
    }
}