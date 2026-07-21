package de.team33.patterns.io.gamma.json;

import java.util.Objects;

class Source {

    private final String text;
    private int index;

    Source(final String text) {
        this(text, 0);
    }

    private Source(final String text, final int index) {
        this.text = Objects.requireNonNull(text);
        this.index = index;
    }

    final String text() {
        return text;
    }

    final int index() {
        return index;
    }

    final boolean hasMore() {
        return index < text.length();
    }

    final void failIfMore() {
        if (hasMore()) {
            throw new IllegalArgumentException(
                    "expected end of source text at index %d".formatted(index));
        }
    }

    final void failIfEOT() {
        if (!hasMore()) {
            throw new IllegalArgumentException(
                    "unexpected end of source text at index %d".formatted(index));
        }
    }

    final char peek() {
        return text.charAt(index);
    }

    final void skip() {
        index += 1;
    }

    final void skipExpected(final char expected) {
        failIfEOT();
        final char c = peek();
        if (expected == c) {
            skip();
        } else {
            throw new IllegalArgumentException(
                    "expected '%c' - but was '%c' at index %d".formatted(expected, c, index));
        }
    }

    final void skipWhitespace() {
        while (hasMore() && Character.isWhitespace(peek())) {
            skip();
        }
    }

    final String readUntil(final CharPredicate predicate) {
        final Source fork = fork();
        while (fork.hasMore() && !predicate.test(fork.peek())) {
            fork.skip();
        }
        final String result = text.substring(index, fork.index);
        index = fork.index;
        return result;
    }

    final String readStringLiteral() {
        final StringBuilder body = new StringBuilder();

        skipExpected('"');

        CharLiteral next = readCharLiteral();
        while (next.isPresent()) {
            body.append(next.toChar());
            next = readCharLiteral();
        }

        skipExpected('"');

        return body.toString();
    }

    private Source fork() {
        return new Source(text, index);
    }

    private CharLiteral readCharLiteral() {
        int value = hasMore() ? peek() : -1;

        if ('"' == value) {
            value = -1;
        } else if ('\\' == value) {
            value = escChar();
        }

        if (0 <= value) {
            skip();
        }

        return new CharLiteral(value);
    }

    private int escChar() {
        index += 1;
        failIfEOT();

        return switch (peek()) {
            case '\\' -> '\\';
            case '"' -> '"';
            case 'b' -> '\b';
            case 'f' -> '\f';
            case 'n' -> '\n';
            case 'r' -> '\r';
            case 't' -> '\t';
            default -> -1;
        };
    }

    interface CharPredicate {

        boolean test(char c);
    }

    private record CharLiteral(int value) {

        boolean isPresent() {
            return 0 <= value;
        }

        char toChar() {
            return (char) value;
        }
    }
}
