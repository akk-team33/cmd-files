package de.team33.patterns.exceptional.alpha;

import de.team33.patterns.exceptional.dione.XSupplier;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

/**
 * A tool for executing methods that can throw certain exceptions that shell be ignored.
 *
 * @param <X> A main {@link Exception} type that shell be ignored, may be a checked {@link Exception} type.
 */
public class Ignoring<X extends Exception> {

    static final String UNEXPECTED = "Unexpected checked Exception:%n" +
                                     "    Type    : %s%n" +
                                     "    Message : %s%n";

    private final Set<Class<? extends Exception>> ignorable;

    public Ignoring(final Class<X> xClass, final Collection<Class<? extends RuntimeException>> rtxClasses) {
        this.ignorable = Set.copyOf(Stream.concat(Stream.of(xClass), rtxClasses.stream()).toList());
    }

    @SafeVarargs
    public static <X extends Exception> Ignoring<X> any(final Class<X> xClass,
                                                        final Class<? extends RuntimeException>... rtxClasses) {
        return new Ignoring<>(xClass, List.of(rtxClasses));
    }

    public final <R> Optional<R> get(final XSupplier<R, ? extends X> method) {
        try {
            return Optional.ofNullable(method.get());
        } catch (final Error err) {
            throw err;
        } catch (final Throwable ex) {
            if (ignorable.stream().anyMatch(xClass -> xClass.isAssignableFrom(ex.getClass()))) {
                return Optional.empty();
            } else if (ex instanceof RuntimeException rx) {
                throw rx;
            } else {
                // Can't actually occur at all.
                throw new IllegalStateException(UNEXPECTED.formatted(ex.getClass().getCanonicalName(),
                                                                     ex.getMessage()),
                                                ex);
            }
        }
    }
}
