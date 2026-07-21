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
        source.testEOT();
        return result;
    }

    private JsonValue parseValue() {
        source.testNotEOT();
        return switch (source.peek()) {
            case '{' -> parseObject();
            case '[' -> parseArray();
            case '"' -> parseString();
            case 't', 'T', 'f', 'F' -> parseBoolean();
            default -> parseNumber();
        };
    }

    private JsonObject parseObject() {
        source.skipExpected('{');
        source.skipWhitespace();
        final JsonObject.Builder builder = parseObjectBody();
        source.skipExpected('}');
        source.skipWhitespace();
        return builder.build();
    }

    private JsonObject.Builder parseObjectBody() {
        final JsonObject.Builder builder = JsonObject.builder();
        char next = source.isEOT() ? 0 : ',';
        while (',' == next) {
            final String name = parseString().value();
            source.skipExpected(':');
            source.skipWhitespace();
            final JsonValue value = parseValue();
            builder.put(name, value);
            next = source.isEOT() ? 0 : source.peek();
            if (',' == next) {
                source.skip();
                source.skipWhitespace();
            }
        }
        return builder;
    }

    private JsonValue parseArray() {
        throw new UnsupportedOperationException("not yet implemented");
    }

    private JsonString parseString() {
        final String stage = source.readStringLiteral();
        source.skipWhitespace();
        return new JsonString(stage);
    }

    private JsonBoolean parseBoolean() {
        final String candidate = source.readUntil(this::isLimitChar);
        final boolean value = Boolean.parseBoolean(candidate);
        source.skipWhitespace();
        return new JsonBoolean(value);
    }

    private JsonNumber parseNumber() {
        final String candidate = source.readUntil(this::isLimitChar);
        final BigDecimal number = new BigDecimal(candidate);
        source.skipWhitespace();
        return new JsonNumber(number);
    }

    private boolean isLimitChar(final char c) {
        return Character.isWhitespace(c) || (0 <= LIMIT_CHARS.indexOf(c));
    }
}
