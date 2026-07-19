package de.team33.patterns.io.gamma.json;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static java.util.Objects.requireNonNull;

final class JsonObject implements JsonValue {

    private final Map<String, JsonValue> values;

    private JsonObject(final Map<String, JsonValue> values) {
        this.values = Map.copyOf(values);
    }

    static Builder builder() {
        return new Builder();
    }

    /**
     * Returns the value associated with the given name or {@link JsonValue#NULL}
     * if no value is associated with the name.
     *
     * @param name the member name
     * @return the associated JSON value or {@link JsonValue#NULL}
     */
    final JsonValue get(final String name) {
        return Optional.ofNullable(values.get(name))
                       .orElse(JsonValue.NULL);
    }

    final Set<String> names() {
        return values.keySet();
    }

    @Override
    public final boolean equals(final Object obj) {
        return this == obj || (obj instanceof JsonObject other && values.equals(other.values));
    }

    @Override
    public final int hashCode() {
        return values.hashCode();
    }

    @Override
    public final String toString() {
        return values.toString();
    }

    static final class Builder {

        private final Map<String, JsonValue> values = new HashMap<>();

        final Builder put(final String name, final JsonValue value) {
            values.put(requireNonNull(name), requireNonNull(value));
            return this;
        }

        final JsonObject build() {
            return new JsonObject(values);
        }
    }
}
