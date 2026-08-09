package de.team33.cmd.files.job;

import de.team33.patterns.streamable.galatea.Streamer;

import java.util.Arrays;
import java.util.List;

record RegistrarConfig(String[] namePatterns, String[] excludePatterns) implements FilteringNames {

    static final RegistrarConfig EMPTY = new RegistrarConfig(null, null);
    static final RegistrarConfig DEFAULT = EMPTY;

    private static String[] join(final String[] head, final String tail) {
        final Streamer<String> streamer = (null == head) ? Streamer.empty()
                                                         : Streamer.of(head);
        return streamer.add(tail)
                       .stream()
                       .toArray(String[]::new);
    }

    final RegistrarConfig addNamePattern(final String tail) {
        return (null == tail) ? this
                              : new RegistrarConfig(join(namePatterns, tail), excludePatterns);
    }

    final RegistrarConfig addExcludePattern(final String tail) {
        return (null == tail) ? this
                              : new RegistrarConfig(namePatterns, join(excludePatterns, tail));
    }

    private List<Object> toList() {
        return Arrays.asList((null == namePatterns) ? null : List.of(namePatterns),
                             (null == excludePatterns) ? null : List.of(excludePatterns));
    }

    @Override
    public boolean equals(final Object obj) {
        return this == obj || obj instanceof RegistrarConfig other && toList().equals(other.toList());
    }

    @Override
    public int hashCode() {
        return toList().hashCode();
    }
}
