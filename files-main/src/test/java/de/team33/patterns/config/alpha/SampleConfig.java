package de.team33.patterns.config.alpha;

import java.time.Instant;
import java.util.List;

public record SampleConfig(String string, Entry entry, List<Item> items) {

    public record Entry(Instant creation, long update) {
    }

    public record Item(int start, int limit) {
    }
}
