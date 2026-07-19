package de.team33.patterns.io.gamma.json;

import java.util.Objects;

record JsonString(String value) implements JsonValue {

    JsonString {
        Objects.requireNonNull(value);
    }
}
