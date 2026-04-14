package de.team33.cmd.files.common;

import java.util.function.Predicate;

/**
 * A utility that provides special {@link Predicate}s.
 */
public class Filter {

    private static final String CLASS_NAME = Filter.class.getSimpleName();
    private static final String POSITIVE_NAME = CLASS_NAME + ".POSITIVE";
    private static final String NEGATIVE_NAME = CLASS_NAME + ".NEGATIVE";

    /**
     * Results in a unique (singleton) {@link Predicate}
     * such that {@link Predicate#test(Object) positive().test(anything)} always returns {@code true}.
     * <p>
     * Furthermore, ...
     * <ul>
     *     <li>{@link Predicate#and(Predicate) positive().and(other)} always results in {@code other}.</li>
     *     <li>{@link Predicate#or(Predicate) positive().or(other)} always results in {@code positive()}.</li>
     *     <li>{@link Predicate#negate() positive().negate()} always results in {@link #negative()}.</li>
     * </ul>
     */
    @SuppressWarnings("unchecked")
    public static <T> Predicate<T> positive() {
        return POSITIVE;
    }    @SuppressWarnings("rawtypes")
    private static final Predicate POSITIVE = new Predicate() {

        @Override
        public boolean test(final Object o) {
            return true;
        }

        @Override
        public Predicate and(final Predicate other) {
            return other;
        }

        @Override
        public Predicate or(final Predicate other) {
            return this;
        }

        @Override
        public Predicate negate() {
            return NEGATIVE;
        }

        @Override
        public String toString() {
            return POSITIVE_NAME;
        }
    };

    /**
     * Results in a unique (singleton) {@link Predicate}
     * such that {@link Predicate#test(Object) negative().test(anything)} always returns {@code false}.
     * <p>
     * Furthermore, ...
     * <ul>
     *      <li>{@link Predicate#and(Predicate) negative().and(other)} always results in {@code negative()}.</li>
     *      <li>{@link Predicate#or(Predicate) negative().or(other)} always results in {@code other}.</li>
     *      <li>{@link Predicate#negate() negative().negate()} always results in {@link #positive()}.</li>
     *  </ul>
     */
    @SuppressWarnings("unchecked")
    public static <T> Predicate<T> negative() {
        return NEGATIVE;
    }    @SuppressWarnings("rawtypes")
    private static final Predicate NEGATIVE = new Predicate() {

        @Override
        public boolean test(final Object o) {
            return false;
        }

        @Override
        public Predicate and(final Predicate other) {
            return this;
        }

        @Override
        public Predicate or(final Predicate other) {
            return other;
        }

        @Override
        public Predicate negate() {
            return POSITIVE;
        }

        @Override
        public String toString() {
            return NEGATIVE_NAME;
        }
    };




}
