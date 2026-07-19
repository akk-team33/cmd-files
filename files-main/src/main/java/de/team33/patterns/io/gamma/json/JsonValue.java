package de.team33.patterns.io.gamma.json;

interface JsonValue {

    JsonValue NULL = new JsonValue() {

        @Override
        public String toString() {
            return "%s.NULL".formatted(JsonValue.class.getSimpleName());
        }
    };
}
