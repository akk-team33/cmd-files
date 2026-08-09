package de.team33.patterns.config.alpha;

import de.team33.patterns.records.triton.Descriptor;
import de.team33.patterns.records.triton.Triton;

import java.util.HashMap;
import java.util.Map;

import static de.team33.patterns.config.alpha.Util.shouldNotHappen;

final class Merger<T extends Record> {

    private final Descriptor<T> descriptor;

    private Merger(final Class<T> configClass) throws NoSuchMethodException {
        this.descriptor = Triton.descriptor(configClass);
    }

    static <T extends Record> Merger<T> by(final Class<T> configClass) {
        try {
            return new Merger<>(configClass);
        } catch (final NoSuchMethodException e) {
            throw shouldNotHappen(e);
        }
    }

    final T merge(final T left, final T right) {
        if (null == right) {
            return left;
        } else if (null == left) {
            return right;
        } else {
            return merge(Triton.toMap(left), Triton.toMap(right));
        }
    }

    private T merge(final Map<String, Object> leftMap, final Map<String, Object> rightMap) {
        final var map = descriptor.names()
                                  .stream()
                                  .map(name -> merge(name, leftMap.get(name), rightMap.get(name)))
                                  .collect(HashMap::new, this::put, Map::putAll);
        return Triton.toRecord(descriptor.recordType(), map);
    }

    private void put(final Map<String, Object> map, Entry entry) {
        map.put(entry.name, entry.value);
    }

    private Entry merge(final String name, final Object left, final Object right) {
        final var value = (null == right) ? left : merge(descriptor.type(name), left, right);
        return new Entry(name, value);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private Object merge(final Class type, final Object left, final Object right) {
        if (type.isRecord()) {
            return by(type).merge((Record) left, (Record) right);
        } else {
            return right;
        }
    }

    private record Entry(String name, Object value) {
    }
}
