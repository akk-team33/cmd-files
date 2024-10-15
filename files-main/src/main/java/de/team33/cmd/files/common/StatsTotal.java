package de.team33.cmd.files.common;

import de.team33.patterns.io.alpha.FileEntry;
import de.team33.patterns.io.alpha.FileType;

import java.util.Map;
import java.util.TreeMap;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class StatsTotal {

    private final Map<FileType, Counter> totalsByType = new TreeMap<>();
    private int total;

    public final void reset() {
        total = 0;
        totalsByType.clear();
    }

    public final void addTotal(final FileEntry entry) {
        total += 1;
        totalsByType.computeIfAbsent(entry.type(), any -> new Counter())
                    .increment();
    }

    public final Stream<String> lines() {
        final Stream<Supplier<String>> head = Stream.of(() -> "%,12d entries examined in total.".formatted(total));
        final Stream<Supplier<String>> tail = totalsByType.entrySet()
                                                          .stream()
                                                          .map(entry -> () -> line(entry));
        return Stream.concat(head, tail)
                     .map(Supplier::get);
    }

    private String line(final Map.Entry<FileType, Counter> entry) {
        return "%,16d entries of %s".formatted(entry.getValue().value(), entry.getKey());
    }
}
