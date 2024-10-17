package de.team33.cmd.files.stats;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class Aggregat<T> {

    @SuppressWarnings("rawtypes")
    private static final Aspect EMPTY = new Aspect() {
        @Override
        public void reset() {}

        @Override
        public void increment(final Object trigger) {}

        @Override
        public Stream<String> lines() {
            return Stream.of("");
        }
    };
    private static final AtomicInteger NEXT_EMPTY = new AtomicInteger();

    private final Map<String, Aspect<T>> map = new LinkedHashMap<>();

    public static <T> Aggregat<T> head(final String name, final Aspect<T> aspect) {
        return new Aggregat<T>().add(name, aspect);
    }

    public static <T> Aggregat<T> head(final Enum<?> key, final Aspect<T> aspect) {
        return head(key.name(), aspect);
    }

    public static <T> Aggregat<T> headEmpty() {
        return head(nextEmpty(), empty());
    }

    @SuppressWarnings("unchecked")
    private static <T> Aspect<T> empty() {
        return EMPTY;
    }

    private static String nextEmpty() {
        return "EMPTY-" + NEXT_EMPTY.getAndIncrement();
    }

    private Supplier<NoSuchElementException> newAspectNotFound(final String name) {
        return () -> new NoSuchElementException("Aspect not found: \"%s\"".formatted(name));
    }

    public final Aggregat<T> add(final String name, final Aspect<T> aspect) {
        map.put(name, aspect);
        return this;
    }

    public final Aggregat<T> add(final Enum<?> key, final Aspect<T> aspect) {
        return add(key.name(), aspect);
    }

    public final Aggregat<T> addEmpty() {
        return add(nextEmpty(), empty());
    }

    public final void reset() {
        map.values().forEach(Aspect::reset);
    }

    public final void increment(final String name, final T trigger) {
        Optional.ofNullable(map.get(name))
                .orElseThrow(newAspectNotFound(name))
                .increment(trigger);
    }

    public final Stream<String> lines() {
        return map.values().stream().flatMap(Aspect::lines);
    }
}
