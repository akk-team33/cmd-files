package de.team33.patterns.io.gamma;

import java.io.IOException;

/**
 * Represents a destination that accepts values of a specific type.
 * <p>
 * Implementations write supplied values, potentially performing I/O
 * operations.
 *
 * @param <T> the type of values accepted by this output
 */
@FunctionalInterface
public interface Output<T> {

    /**
     * Writes the given value to this output.
     *
     * @param value the value to write
     * @throws IOException if an I/O error occurs while writing
     */
    void write(T value) throws IOException;
}
