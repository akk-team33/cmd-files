package de.team33.cmd.files.job;

import de.team33.patterns.streamable.galatea.Streamer;

import java.util.Arrays;
import java.util.List;

record MovingConfig(String[] namePatterns, String[] excludePatterns) implements FilteringNames {

    static final MovingConfig EMPTY = new MovingConfig(null, null);
    static final MovingConfig DEFAULT = EMPTY;

    private static String[] join(final String[] head, final String tail) {
        final Streamer<String> streamer = (null == head) ? Streamer.empty()
                                                         : Streamer.of(head);
        return streamer.add(tail)
                       .stream()
                       .toArray(String[]::new);
    }

    final MovingConfig addNamePattern(final String tail) {
        return (null == tail) ? this
                              : new MovingConfig(join(namePatterns, tail), excludePatterns);
    }

    final MovingConfig addExcludePattern(final String tail) {
        return (null == tail) ? this
                              : new MovingConfig(namePatterns, join(excludePatterns, tail));
    }

    private List<Object> toList() {
        return Arrays.asList((null == namePatterns) ? null : List.of(namePatterns),
                             (null == excludePatterns) ? null : List.of(excludePatterns));
    }

    @Override
    public boolean equals(final Object obj) {
        return this == obj || obj instanceof MovingConfig other && toList().equals(other.toList());
    }

    @Override
    public int hashCode() {
        return toList().hashCode();
    }
}
