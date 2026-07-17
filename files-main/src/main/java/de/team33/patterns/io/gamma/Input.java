package de.team33.patterns.io.gamma;

import java.io.IOException;

@FunctionalInterface
public interface Input<T> {

    T read() throws IOException;
}
