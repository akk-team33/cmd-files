package de.team33.patterns.exceptional.alpha.publics;

import de.team33.patterns.exceptional.alpha.Ignoring;
import de.team33.patterns.exceptional.dione.Conversion;
import de.team33.patterns.exceptional.dione.XSupplier;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Instant;
import java.util.Optional;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class IgnoringTest {

    private static final Instant INSTANT = Instant.now();
    private static final Something SOMETHING = new Something(INSTANT);
    private static final Supplier<Something> WORKING = () -> SOMETHING;
    private static final XSupplier<Something, IOException> FAILING_IOX = () -> {
        throw new IOException();
    };
    private static final Supplier<Something> FAILING_RTX = () -> {
        throw new IllegalArgumentException();
    };

    @Test
    final void test_RTException() {
        final Ignoring<RuntimeException> handler = Ignoring.any(RuntimeException.class);
        assertEquals(Optional.empty(), handler.get(() -> Conversion.supplier(FAILING_IOX).get()));
        assertEquals(Optional.empty(), handler.get(FAILING_RTX::get));
        assertEquals(Optional.of(SOMETHING), handler.get(WORKING::get));
    }

    @Test
    final void test_Exception() {
        final Ignoring<Exception> handler = Ignoring.any(Exception.class);
        assertEquals(Optional.empty(), handler.get(FAILING_IOX));
        assertEquals(Optional.empty(), handler.get(FAILING_RTX::get));
        assertEquals(Optional.of(SOMETHING), handler.get(WORKING::get));
    }

    @Test
    final void test_IOException() {
        final Ignoring<IOException> handler = Ignoring.any(IOException.class);
        assertEquals(Optional.empty(), handler.get(FAILING_IOX));
        assertThrows(IllegalArgumentException.class, () -> handler.get(FAILING_RTX::get));
        assertEquals(Optional.of(SOMETHING), handler.get(WORKING::get));
    }

    @Test
    final void test_IOException_RTException() {
        final Ignoring<IOException> handler = Ignoring.any(IOException.class,
                                                           RuntimeException.class);
        assertEquals(Optional.empty(), handler.get(FAILING_IOX));
        assertEquals(Optional.empty(), handler.get(FAILING_RTX::get));
        assertEquals(Optional.of(SOMETHING), handler.get(WORKING::get));
    }

    record Something(Instant time) {
    }
}
