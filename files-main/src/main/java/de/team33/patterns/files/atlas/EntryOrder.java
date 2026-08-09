package de.team33.patterns.files.atlas;

import de.team33.patterns.files.pluto.FileEntry;

import java.util.Comparator;

public interface EntryOrder extends Comparator<FileEntry> {

    EntryOrder BY_PATH = new Named("BY_PATH", Comparator.comparing(FileEntry::path, PathOrder.DEFAULT));
    EntryOrder BY_NAME = new Named("BY_NAME", Comparator.comparing(FileEntry::name, StringOrder.DEFAULT)
                                                        .thenComparing(BY_PATH));
    EntryOrder BY_SIZE = new Named("BY_SIZE", Comparator.comparing(FileEntry::size)
                                                        .thenComparing(BY_PATH));
    EntryOrder BY_UPDATE = new Named("BY_UPDATE", Comparator.comparing(FileEntry::lastModified)
                                                            .thenComparing(BY_PATH));

    class Named extends NamedComparator<FileEntry> implements EntryOrder {
        private Named(final String name, final Comparator<FileEntry> backing) {
            super(String.join(".", EntryOrder.class.getSimpleName(), name), backing);
        }
    }
}
