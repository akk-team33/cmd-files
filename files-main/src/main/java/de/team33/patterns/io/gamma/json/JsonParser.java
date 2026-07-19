package de.team33.patterns.io.gamma.json;

class JsonParser {

    private final String source;

    private int start;

    private JsonParser(final String source) {
        this.source = source;
        this.start = 0;
    }

    static JsonObject parse(final String source) {
        return new JsonParser(source).parseRoot();
    }

    private JsonObject parseRoot() {
        skipWhitespace();
        testNotEOT();
        final JsonObject result = source.charAt(start) == '{' ? parseObject() : fail();
        testEOT();
        return result;
    }

    private JsonObject parseObject() {
        // starts with '{' ...
        start += 1;
        skipWhitespace();
        final JsonObject.Builder builder = parseObjectBody();
        if (source.charAt(start) == '}') {
            start += 1;
            skipWhitespace();
            return builder.build();
        }
        throw new UnsupportedOperationException("not yet implemented");
    }

    private JsonObject.Builder parseObjectBody() {
        throw new UnsupportedOperationException("not yet implemented");
    }

    private <T> T fail() {
        throw new IllegalArgumentException("unexpected char at %d: '%c'".formatted(start, source.charAt(start)));
    }

    private void testEOT() {
        if (start < source.length()) {
            throw new IllegalArgumentException("unexpected text at index %d".formatted(start));
        }
    }

    private void testNotEOT() {
        if (start >= source.length()) {
            throw new IllegalArgumentException("unexpected end of source text at index %d".formatted(start));
        }
    }

    private void skipWhitespace() {
        while (Character.isWhitespace(source.charAt(start))) {
            start += 1;
        }
    }
}
