package de.team33.tools.io;

import de.team33.patterns.hashing.pandia.Hash;

import java.io.Closeable;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

public class Registry implements Closeable {

    private static final String COLON = ":";

    private final Map<String, Map<String, String>> map = new ConcurrentHashMap<>();
    private final Path path;

    public Registry(final Path path) {
        this.path = path.toAbsolutePath().normalize();
    }

    private Map<String, String> subMap(final Hash hash) {
        final var key = "%02x".formatted(hash.bytes()[0]);
        return map.computeIfAbsent(key, this::newSubMap);
    }

    private Map<String, String> newSubMap(final String key) {
        try (final Stream<String> stream = Files.lines(keyPath(key), StandardCharsets.UTF_8)) {
            return stream.map(line -> line.split(COLON, 2))
                         .collect(ConcurrentHashMap::new, (map, entry) -> map.put(entry[0], entry[1]), Map::putAll);
        } catch (final NoSuchFileException e) {
            return new ConcurrentHashMap<>();
        } catch (final IOException e) {
            throw new IllegalStateException(Optional.ofNullable(e.getMessage())
                                                    .orElseGet(e::toString), e);
        }
    }

    private Path keyPath(final String key) {
        return path.resolve(key + ".map");
    }

    public void confirm(final Hash hash, final String name) {
        subMap(hash).put(hash.toHexString(), name);
    }

    public boolean register(final Hash hash, final String name) {
        return null == subMap(hash).putIfAbsent(hash.toHexString(), name);
    }

    @Override
    public final void close() {
        map.forEach(this::write);
    }

    private void write(final String key, final Map<String, String> subMap) {
        final var list = subMap.entrySet().stream()
                               .map(entry -> entry.getKey() + COLON + entry.getValue())
                               .toList();
        try {
            final Path keyPath = keyPath(key);
            Files.createDirectories(keyPath.getParent());
            Files.write(keyPath, list, StandardCharsets.UTF_8);
        } catch (final IOException e) {
            throw new IllegalStateException(Optional.ofNullable(e.getMessage())
                                                    .orElseGet(e::toString), e);
        }
    }
}
