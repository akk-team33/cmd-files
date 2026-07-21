package de.team33.patterns.io.gamma.json;

import java.math.BigDecimal;

class JsonParser {

    private static final String LIMIT_CHARS = ",}]";

    private final Source source;

    private JsonParser(final String source) {
        this.source = new Source(source);
    }

    static JsonValue parse(final String source) {
        return new JsonParser(source).parseRoot();
    }

    private JsonValue parseRoot() {
        source.skipWhitespace();
        final JsonValue result = parseValue();
        source.failIfMore();
        return result;
    }

    private JsonValue parseValue() {
        source.failIfEOT();
        return switch (source.peek()) {
            case '{' -> parseObject();
            case '[' -> parseArray();
            case '"' -> parseString();
            case 'n' -> parseNull();
            case 't', 'f' -> parseBoolean();
            default -> parseNumber();
        };
    }

    private JsonObject parseObject() {
        source.skipExpected('{');
        source.skipWhitespace();
        final JsonObject.Builder builder = JsonObject.builder();
        if (source.hasMore() && '}' == source.peek()) {
            source.skip();
        } else {
            parseObjectBody(builder);
            source.skipExpected('}');
        }
        source.skipWhitespace();
        return builder.build();
    }

    private void parseObjectBody(final JsonObject.Builder builder) {
        char next = source.hasMore() ? ',' : 0;
        while (',' == next) {
            parseMember(builder);
            next = source.hasMore() ? source.peek() : 0;
            if (',' == next) {
                source.skip();
                source.skipWhitespace();
            }
        }
    }

    private void parseMember(JsonObject.Builder builder) {
        final String name = parseString().value();

        source.skipExpected(':');
        source.skipWhitespace();

        final JsonValue value = parseValue();
        builder.put(name, value);
    }

    private JsonValue parseArray() {
        throw new UnsupportedOperationException("not yet implemented");
    }

    private JsonString parseString() {
        final String stage = source.readStringLiteral();
        source.skipWhitespace();
        return new JsonString(stage);
    }

    private JsonValue parseNull() {
        final String candidate = source.readUntil(this::isLimitChar).trim();
        @SuppressWarnings("SwitchStatementWithTooFewBranches")
        final JsonValue result = switch (candidate) {
            case "null" -> JsonValue.NULL;
            default -> throw new IllegalArgumentException(
                    "expected null - but was %s".formatted(candidate));
        };
        source.skipWhitespace();
        return result;
    }

    private JsonBoolean parseBoolean() {
        final String candidate = source.readUntil(this::isLimitChar).trim();
        final boolean value = switch (candidate) {
            case "true" -> true;
            case "false" -> false;
            default -> throw new IllegalArgumentException(
                    "expected one of {true, false} - but was %s".formatted(candidate));
        };
        source.skipWhitespace();
        return new JsonBoolean(value);
    }

    private JsonNumber parseNumber() {
        final String candidate = source.readUntil(this::isLimitChar).trim();
        final BigDecimal number = new BigDecimal(candidate);
        source.skipWhitespace();
        return new JsonNumber(number);
    }

    private boolean isLimitChar(final char c) {
        return Character.isWhitespace(c) || (0 <= LIMIT_CHARS.indexOf(c));
    }
}
