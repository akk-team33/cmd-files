package de.team33.patterns.directories.iocaste;

import java.util.Comparator;

class NamedComparator<T> implements Comparator<T> {

    private final String name;
    private final Comparator<T> backing;

    NamedComparator(final String name, final Comparator<T> backing) {
        this.name = name;
        this.backing = backing;
    }

    @Override
    public final int compare(final T left, final T right) {
        return backing.compare(left, right);
    }

    @Override
    public final String toString() {
        return name;
    }
}
