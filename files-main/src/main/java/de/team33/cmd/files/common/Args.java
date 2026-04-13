package de.team33.cmd.files.common;

import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;

public class Args<O extends Enum<O>> {

    private static final String PATTERN = Pattern.quote(":");

    private final List<String> args;
    private final Map<O, String> optionMap;

    private Args(final List<String> args, final Map<O, String> optionMap) {
        this.args = args;
        this.optionMap = optionMap;
    }

    public static <O extends Enum<O>>
    Function<List<String>, Args<O>> stage(final int required, final Class<O> options) {
        return args -> new Stage<>(required, options).apply(args);
    }

    public String get(final int index) {
        return args.get(index);
    }

    public Optional<String> get(final O option) {
        return Optional.ofNullable(optionMap.get(option));
    }

    private static class Stage<O extends Enum<O>> {

        private final int required;
        private final Class<O> optionClass;
        private final EnumSet<O> options;
        private final Map<String, O> keyMap;

        private Stage(final int required, final Class<O> optionClass) {
            this.required = required;
            this.optionClass = optionClass;
            this.options = EnumSet.allOf(optionClass);
            this.keyMap = options.stream().collect(TreeMap::new, Stage::put, Map::putAll);
        }

        private static <O extends Enum<O>>
        void put(final Map<String, O> map, final O option) {
            map.put(option.name().toUpperCase(), option);
        }

        private static List<String> split(final String arg) {
            final String[] array = arg.split(PATTERN, 2);
            if (2 == array.length) {
                return List.of(array[0].toUpperCase(), array[1]);
            } else {
                throw new IllegalArgumentException("Expected KEY:VALUE - but was %s".formatted(arg));
            }
        }

        private void add(final Map<O, String> map, final List<String> entry) {
            final O key = keyMap.get(entry.get(0));
            if (null == key) {
                throw new IllegalArgumentException("Expected one of %s - but was %s".formatted(
                        options, entry.get(0)));
            } else if (map.containsKey(key)) {
                throw new IllegalArgumentException("Duplicate key: %s".formatted(key));
            } else {
                map.put(key, entry.get(1));
            }
        }

        final Args<O> apply(final List<String> args) {
            if (args.size() < required) {
                throw new IllegalArgumentException("Expected at least %d arguments - but was %d".formatted(
                        required, args.size()));
            }
            final Map<O, String> optionMap = args.stream()
                                                 .skip(required)
                                                 .map(Stage::split)
                                                 .collect(() -> new EnumMap<>(optionClass), this::add, Map::putAll);
            return new Args<>(args, optionMap);
        }
    }
}
