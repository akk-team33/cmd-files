package de.team33.patterns.io.gamma.json;

import java.math.BigDecimal;

class JsonParser {

    private static final String LIMIT_CHARS = ",}]";

    private final String source;
    private int index;

    private JsonParser(final String source) {
        this.source = source;
        this.index = 0;
    }

    static JsonValue parse(final String source) {
        return new JsonParser(source).parseRoot();
    }

    private JsonValue parseRoot() {
        skipWhitespace();
        final JsonValue result = parseValue();
        testEOT();
        return result;
    }

    private JsonValue parseValue() {
        testNotEOT();
        return switch (source.charAt(index)) {
            case '{' -> parseObject();
            case '[' -> parseArray();
            case '"' -> parseString();
            case 't', 'T', 'f', 'F' -> parseBoolean();
            default -> parseNumber();
        };
    }

    private JsonObject parseObject() {
        skipExpected('{');
        skipWhitespace();
        final JsonObject.Builder builder = parseObjectBody();
        skipExpected('}');
        skipWhitespace();
        return builder.build();
    }

    private JsonObject.Builder parseObjectBody() {
        final JsonObject.Builder builder = JsonObject.builder();
        char next = isEOT() ? 0 : ',';
        while ((',' == next)) {
            final String name = parseString().value();
            skipExpected(':');
            skipWhitespace();
            final var value = parseValue();
            builder.put(name, value);
            next = isEOT() ? 0 : source.charAt(index);
            if (',' == next) {
                index += 1;
                skipWhitespace();
            }
        }
        return builder;
    }

    private JsonValue parseArray() {
        throw new UnsupportedOperationException("not yet implemented");
    }

    private JsonString parseString() {
        final StringLiteral stage = new StringLiteral();
        return new JsonString(stage.body());
    }

    private void skipExpected(final char expected) {
        final char c = source.charAt(index);
        if (expected == c) {
            index += 1;
        } else {
            throw new IllegalArgumentException(
                    "expected '%c' - but was '%c' at index %d".formatted(expected, c, index));
        }
    }

    private JsonBoolean parseBoolean() {
        final int limit = findLimit();
        final boolean value = Boolean.parseBoolean(source.substring(index, limit));
        index = limit;
        skipWhitespace();
        return new JsonBoolean(value);
    }

    private JsonNumber parseNumber() {
        final int limit = findLimit();
        final BigDecimal number = new BigDecimal(source.substring(index, limit));
        index = limit;
        skipWhitespace();
        return new JsonNumber(number);
    }

    private int findLimit() {
        int next = index + 1;
        while (!isLimitIndex(next)) {
            next += 1;
        }
        return next;
    }

    private boolean isLimitIndex(final int next) {
        return isEOT() || isLimitChar(source.charAt(next));
    }

    private boolean isLimitChar(final char c) {
        return Character.isWhitespace(c) || (0 <= LIMIT_CHARS.indexOf(c));
    }

    private void testEOT() {
        if (!isEOT()) {
            throw new IllegalArgumentException("expected end of source text at index %d".formatted(index));
        }
    }

    private void testNotEOT() {
        if (isEOT()) {
            throw new IllegalArgumentException("unexpected end of source text at index %d".formatted(index));
        }
    }

    private boolean isEOT() {
        return index >= source.length();
    }

    private void skipWhitespace() {
        while (index < source.length() && Character.isWhitespace(source.charAt(index))) {
            index += 1;
        }
    }

    private class CharLiteral {

        private int value;

        CharLiteral() {
            this.value = isEOT() ? -1 : source.charAt(index);
            if ('"' == value) {
                this.value = -1;
            } else if ('\\' == value) {
                this.value = escChar();
            }
            index += ((0 > value) ? 0 : 1);
        }

        private int escChar() {
            index += 1;
            testNotEOT();
            final char next = source.charAt(index);
            return switch (next) {
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

        final boolean isPresent() {
            return 0 <= value;
        }

        final char toChar() {
            return (char) value;
        }
    }

    private class StringLiteral {

        private final StringBuilder body = new StringBuilder();

        StringLiteral() {
            skipExpected('"');
            CharLiteral next = new CharLiteral();
            while (next.isPresent()) {
                body.append(next.toChar());
                next = new CharLiteral();
            }
            skipExpected('"');
            skipWhitespace();
        }

        final String body() {
            return body.toString();
        }
    }
}
