package de.team33.patterns.io.gamma.json;

import java.math.BigDecimal;
import java.util.Objects;

record JsonNumber(BigDecimal value) implements JsonValue {

    JsonNumber {
        Objects.requireNonNull(value);
    }
}