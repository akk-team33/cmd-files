package de.team33.patterns.config.alpha;

import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static de.team33.patterns.config.alpha.Util.notYetImplemented;

final class ConfigResource {

    private static final Map<Path, ConfigResource> MAP = new ConcurrentHashMap<>();

    private final Path path;

    private ConfigResource(final Path path) {
        this.path = path;
    }

    static ConfigResource by(final Path path) {
        final Path normalPath = path.toAbsolutePath().normalize();
        return MAP.computeIfAbsent(normalPath, ConfigResource::new);
    }

    public <T> void write(final T config) {
        notYetImplemented();
    }

    public <T> T read(final Class<T> configClass) {
        return notYetImplemented();
    }
}
