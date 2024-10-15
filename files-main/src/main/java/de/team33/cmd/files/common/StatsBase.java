package de.team33.cmd.files.common;

import de.team33.patterns.io.alpha.FileEntry;
import de.team33.patterns.io.alpha.FileType;

import java.util.Map;
import java.util.TreeMap;

public class StatsBase {

    private final Map<FileType, Counter> totalsByType = new TreeMap<>();
    private int total;

    public final void addTotal(final FileEntry entry) {
        total += 1;
        totalsByType.computeIfAbsent(entry.type(), any -> new Counter())
                    .increment();
    }
}
