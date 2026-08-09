package de.team33.patterns.config.alpha;

import de.team33.patterns.io.thalassa.IO;

import java.nio.file.Path;
import java.util.EnumMap;
import java.util.Map;

class IOMapping<T extends Record> {

    private final Class<T> recordClass;
    private final Map<ConfigLevel, IO<T>> backing;
    private final Map<ConfigLevel, Path> paths;

    IOMapping(final Class<T> recordClass, final Pathing pathing, final Naming naming) {
        this.recordClass = recordClass;
        this.backing = new EnumMap<>(ConfigLevel.class);
        this.paths = new EnumMap<>(ConfigLevel.class);

        for (final ConfigLevel level : ConfigLevel.values()) {
            backing.put(level, level.newIO(recordClass, pathing, naming));
            paths.put(level, level.path(pathing, naming));
        }
    }

    final Class<T> recordClass() {
        return recordClass;
    }

    final Path path(final ConfigLevel level) {
        return paths.get(level);
    }

    final IO<T> get(final ConfigLevel level) {
        return backing.get(level);
    }
}
