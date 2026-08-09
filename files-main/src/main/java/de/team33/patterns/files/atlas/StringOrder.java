package de.team33.patterns.files.atlas;

import java.util.Comparator;

interface StringOrder extends Comparator<String> {

    StringOrder IGNORE_CASE = new Named("IGNORE_CASE", String::compareToIgnoreCase);
    StringOrder RESPECT_CASE = new Named("RESPECT_CASE", String::compareTo);
    StringOrder DEFAULT = new Named("DEFAULT", IGNORE_CASE.thenComparing(RESPECT_CASE));

    class Named extends NamedComparator<String> implements StringOrder {
        private Named(final String name, final Comparator<String> backing) {
            super(String.join(".", StringOrder.class.getSimpleName(), name), backing);
        }
    }
}
