package de.team33.patterns.io.gamma;

import java.io.IOException;

/**
 * Represents a source that provides values of a specific type.
 * <p>
 * Implementations read and return the next available value, potentially
 * performing I/O operations.
 *
 * @param <T> the type of values produced by this input
 */
@FunctionalInterface
public interface Input<T> {

    /**
     * Reads and returns the next value from this input.
     *
     * @return the value that was read
     * @throws IOException if an I/O error occurs while reading
     */
    T read() throws IOException;
}
