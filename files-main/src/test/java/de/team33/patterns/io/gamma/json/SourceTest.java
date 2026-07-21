package de.team33.patterns.io.gamma.json;

import de.team33.testing.Supply;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class SourceTest {

    private static final Supply SUPPLY = new Supply();

    private static Source skip(final Source source, final int index) {
        for (int i = 0; i < index; ++i) {
            source.skip();
        }
        return source;
    }

    static Stream<AnyMoreCase> anyMoreCases() {
        return Stream.of(new AnyMoreCase("", 0, false),
                         new AnyMoreCase(SUPPLY.anyString(), Integer.MAX_VALUE, false),
                         new AnyMoreCase(SUPPLY.anyString(), 0, true));
    }

    static Stream<PeekCase> peekCases() {
        return Stream.of(new PeekCase("", 0),
                         new PeekCase(SUPPLY.anyString(), Integer.MAX_VALUE),
                         new PeekCase(SUPPLY.anyString(), 0),
                         new PeekCase("more than 10 characters", 10));
    }

    static Stream<ReadStringLiteralCase> readStringLiteralCases() {
        return Stream.of(new ReadStringLiteralCase("", null),
                         new ReadStringLiteralCase(SUPPLY.anyString(), null),
                         new ReadStringLiteralCase("\"this\\tcontains\\nseveral\\rescape\\fsequences\"",
                                                   "this\tcontains\nseveral\rescape\fsequences"),
                         readStringLiteralCase(SUPPLY.anyString()));
    }

    private static ReadStringLiteralCase readStringLiteralCase(final String expected) {
        return new ReadStringLiteralCase("\"%s\"".formatted(expected), expected);
    }

    @ParameterizedTest
    @MethodSource("anyMoreCases")
    final void hasMore(final AnyMoreCase given) {
        final boolean result = given.source().hasMore();
        assertEquals(given.expected, result);
    }

    @ParameterizedTest
    @MethodSource("anyMoreCases")
    final void failIfMore(final AnyMoreCase given) {
        try {
            given.source().failIfMore();
            assertFalse(given.expected);
        } catch (final IllegalArgumentException e) {
            assertTrue(given.expected);
        }
    }

    @ParameterizedTest
    @MethodSource("anyMoreCases")
    final void failIfEOT(final AnyMoreCase given) {
        try {
            given.source().failIfEOT();
            assertTrue(given.expected);
        } catch (final IllegalArgumentException e) {
            assertFalse(given.expected);
        }
    }

    @ParameterizedTest
    @MethodSource("peekCases")
    final void peek(final PeekCase given) {
        final Source source = given.source();
        try {
            final char result = source.peek();
            assertEquals(given.expected(), result);
        } catch (final IndexOutOfBoundsException e) {
            assertNull(given.expected());
        }
        assertEquals(given.index, source.index());
    }

    @ParameterizedTest
    @MethodSource("peekCases")
    final void skip(final PeekCase given) {
        final Source source = given.source();
        final int expected = given.index + 1;
        source.skip();
        assertEquals(expected, source.index());
    }

    @Test
    final void skipExpected_EOT() {
        final String text = SUPPLY.anyString();
        final int index = text.length();
        final Source source = skip(new Source(text), index);
        final char sample = SUPPLY.anyChar();

        try {
            source.skipExpected(sample);
            fail("expected to fail - but index was %d -> %d".formatted(index, source.index()));
        } catch (final IllegalArgumentException e) {
            // as expected
        }
    }

    @Test
    final void skipExpected_positive() {
        final String text = SUPPLY.anyString();
        final int index = SUPPLY.anyInt(text.length());
        final Source source = skip(new Source(text), index);
        final char sample = text.charAt(index);

        source.skipExpected(sample);
        assertEquals(index + 1, source.index());
    }

    @Test
    final void skipExpected_negative() {
        final String text = SUPPLY.anyString();
        final int index = SUPPLY.anyInt(text.length());
        final Source source = skip(new Source(text), index);
        final char exclude = text.charAt(index);
        char sample = SUPPLY.anyChar();
        while (exclude == sample) {
            sample = SUPPLY.anyChar();
        }

        try {
            source.skipExpected(sample);
            fail("expected to fail - but index was %d -> %d".formatted(index, source.index()));
        } catch (final IllegalArgumentException e) {
            // as expected
        }
    }

    @Test
    final void skipWhitespace() {
        final String head = SUPPLY.anyString();
        final int index = head.length();
        final String text = head + SUPPLY.anyString(10, " \n\r\t");
        final Source source = skip(new Source(text), index);

        source.skipWhitespace();
        assertEquals(index + 10, source.index());
    }

    @Test
    final void readUntil() {
        final String expected = SUPPLY.anyString();
        final String text = expected + ' ' + SUPPLY.anyString();

        final String result = new Source(text).readUntil(c -> c == ' ');
        assertEquals(expected, result);
    }

    @ParameterizedTest
    @MethodSource("readStringLiteralCases")
    final void readStringLiteral(final ReadStringLiteralCase given) {
        try {
            final String result = given.source().readStringLiteral();
            assertNotNull(given.expected);
            assertEquals(given.expected, result);
        } catch (final IllegalArgumentException e) {
            assertNull(given.expected);
            // -> as expected
        }
    }

    interface SourceCase {

        String text();

        default int index() {
            return 0;
        }

        default Source source() {
            return skip(new Source(text()), index());
        }
    }

    record ReadStringLiteralCase(String text, String expected) implements SourceCase {
    }

    record SkipExpectedCase(String text, int index) implements SourceCase {

        SkipExpectedCase {
            index = Integer.min(index, text.length());
        }

        char positiveSample() {
            return text.charAt(index);
        }

        char negativeSample() {
            char c = SUPPLY.anyChar();
            while (c == positiveSample()) {
                c = SUPPLY.anyChar();
            }
            return c;
        }
    }

    record PeekCase(String text, int index) implements SourceCase {

        PeekCase {
            index = Integer.min(index, text.length());
        }

        Character expected() {
            return (index < text.length()) ? text.charAt(index) : null;
        }
    }

    record AnyMoreCase(String text, int index, boolean expected) implements SourceCase {

        AnyMoreCase {
            index = Integer.min(index, text.length());
        }
    }
}