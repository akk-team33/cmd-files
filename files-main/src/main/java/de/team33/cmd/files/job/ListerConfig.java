package de.team33.cmd.files.job;

import de.team33.patterns.streamable.galatea.Streamer;

import java.util.Arrays;
import java.util.List;

record ListerConfig(String[] namePatterns, String[] excludePatterns, String types, String order)
        implements FilteringNames, FilteringTypes, OrderingEntries {

    static final ListerConfig EMPTY = new ListerConfig(null, null, null, null);
    static final ListerConfig DEFAULT = new ListerConfig(null, null, "A", null);

    private static String[] join(final String[] head, final String tail) {
        final Streamer<String> streamer = (null == head) ? Streamer.empty()
                                                         : Streamer.of(head);
        return streamer.add(tail)
                       .stream()
                       .toArray(String[]::new);
    }

    final ListerConfig addNamePattern(final String tail) {
        return (null == tail) ? this
                              : new ListerConfig(join(namePatterns, tail), excludePatterns, types, order);
    }

    final ListerConfig addExcludePattern(final String tail) {
        return (null == tail) ? this
                              : new ListerConfig(namePatterns, join(excludePatterns, tail), types, order);
    }

    final ListerConfig setTypes(final String value) {
        return (null == value) ? this : new ListerConfig(namePatterns, excludePatterns, value, order);
    }

    final ListerConfig setOrder(final String value) {
        return (null == value) ? this : new ListerConfig(namePatterns, excludePatterns, types, value);
    }

    private List<Object> toList() {
        return Arrays.asList((null == namePatterns) ? null : List.of(namePatterns),
                             (null == excludePatterns) ? null : List.of(excludePatterns),
                             types, order);
    }

    @Override
    public boolean equals(final Object obj) {
        return this == obj || obj instanceof ListerConfig other && toList().equals(other.toList());
    }

    @Override
    public int hashCode() {
        return toList().hashCode();
    }
}
