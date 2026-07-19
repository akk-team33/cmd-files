package de.team33.patterns.io.gamma.json;

import java.io.IOException;

public class JsonMapper<T extends Record> {

    private final Class<T> recordClass;
    private final String source;

    private JsonMapper(final Class<T> recordClass, final String source) {
        this.recordClass = recordClass;
        this.source = source;
    }

    public static <T extends Record> T map(final Class<T> recordClass, final String source) throws IOException {
        return new JsonMapper<>(recordClass, source).map();
    }

    private T map() throws IOException {
        final JsonObject value = JsonParser.parse(source);
        throw new UnsupportedOperationException("not yet implemented");
    }
}
