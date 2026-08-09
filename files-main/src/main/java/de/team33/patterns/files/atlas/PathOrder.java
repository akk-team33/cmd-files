package de.team33.patterns.files.atlas;

import java.nio.file.Path;
import java.util.Comparator;

public interface PathOrder extends Comparator<Path> {

    PathOrder DEFAULT = new Named("DEFAULT", Comparator.comparing(Path::toString, StringOrder.DEFAULT));
    PathOrder BY_NAME = new Named("BY_NAME", Comparator.comparing(Path::getFileName, DEFAULT)
                                                       .thenComparing(DEFAULT));

    class Named extends NamedComparator<Path> implements PathOrder {
        private Named(final String name, final Comparator<Path> backing) {
            super(String.join(".", PathOrder.class.getSimpleName(), name), backing);
        }
    }
}
