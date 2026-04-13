package de.team33.cmd.files.common;

import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;

import static java.util.stream.Collectors.toMap;

public class Args {

    private final List<String> args;
    private final Map<Key, String> map;

    private Args(final List<String> args, final Stage stage) {
        this.args = args;
        this.map = stage.map(args);
    }

    public static Function<List<String>, Args> stage(final int required, final Set<? extends Key> keys) {
        return new Stage(required, keys);
    }

    public final String get(final int index) {
        return args.get(index);
    }

    public final Optional<String> get(final Key key) {
        return Optional.ofNullable(map.get(key));
    }

    @Override
    public String toString() {
        return List.of(args, map).toString();
    }

    public interface Key {

        String name();

        default boolean matches(final String key) {
            return name().equalsIgnoreCase(key);
        }
    }

    private static final class Stage implements Function<List<String>, Args> {

        public static final String COLON = Pattern.quote(":");
        private final int required;
        private final Set<? extends Key> keys;

        private Stage(final int required, final Set<? extends Key> keys) {
            this.required = required;
            this.keys = keys;
        }

        private static List<String> split(final String arg) {
            final String[] split = arg.split(COLON, 2);
            if (2 == split.length) {
                return List.of(split);
            } else {
                throw new IllegalArgumentException("Expected format KEY:VALUE - but arg was '%s'".formatted(arg));
            }
        }

        private Map<Key, String> map(final List<String> args) {
            return args.stream()
                       .skip(required)
                       .map(Stage::split)
                       .collect(toMap(this::toKey, this::toValue, this::merge, TreeMap::new));
        }

        private String merge(final String first, final String second) {
            throw new IllegalArgumentException(
                    "Expected unique VALUE - but was first: '%s', second: '%s'".formatted(first, second));
        }

        private Key toKey(final List<String> entry) {
            final String key0 = entry.get(0);
            return keys.stream()
                       .filter(key -> key.matches(key0))
                       .findAny()
                       .orElseThrow(() -> new IllegalArgumentException(
                               "Expected one of %s - but key was '%s'".formatted(keys, key0)));
        }

        private String toValue(final List<String> entry) {
            return entry.get(1);
        }

        @Override
        public Args apply(final List<String> args) {
            if (required > args.size()) {
                throw new IllegalArgumentException(
                        "Expected %d arguments - but was %s".formatted(required, args));
            } else {
                return new Args(args, this);
            }
        }
    }
}
