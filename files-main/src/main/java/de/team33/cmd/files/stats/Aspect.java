package de.team33.cmd.files.stats;

import java.util.stream.Stream;

public interface Aspect<T> {

    void reset();

    void increment(T trigger);

    Stream<String> lines();
}
