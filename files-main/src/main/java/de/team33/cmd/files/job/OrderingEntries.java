package de.team33.cmd.files.job;

import de.team33.cmd.files.sorting.Order;
import de.team33.patterns.files.pluto.FileEntry;

import java.util.Comparator;
import java.util.Optional;

interface OrderingEntries {

    String order();

    default Comparator<FileEntry> entryOrder() {
        return Optional.ofNullable(order())
                       .map(Order::parse)
                       .orElse(null);
    }
}
