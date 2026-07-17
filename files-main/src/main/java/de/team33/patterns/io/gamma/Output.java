package de.team33.patterns.io.gamma;

import java.io.IOException;

@FunctionalInterface
public interface Output<T> {

    void write(T origin) throws IOException;
}
