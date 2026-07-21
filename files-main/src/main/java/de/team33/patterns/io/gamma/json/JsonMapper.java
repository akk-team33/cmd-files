package de.team33.patterns.io.gamma.json;

import de.team33.patterns.enums.pan.Values;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.RecordComponent;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Set;
import java.util.stream.Stream;

public class JsonMapper<T extends Record> {

    private final Class<T> recordClass;
    private final String source;

    private JsonMapper(final Class<T> recordClass, final String source) {
        this.recordClass = recordClass;
        this.source = source;
    }

    public static <T extends Record> T map(final Class<T> recordClass, final String source) throws IOException {
        return new JsonMapper<>(recordClass, source).map();
    }

    private static Object mapRecord(final Class<?> recordClass, final JsonObject jsonObject) {
        final RecordComponent[] recordComponents = recordClass.getRecordComponents();
        final Object[] initials = map(jsonObject, recordComponents);
        final Class<?>[] types = Stream.of(recordComponents)
                                       .map(RecordComponent::getType)
                                       .toArray(Class<?>[]::new);
        try {
            final Constructor<?> constructor = recordClass.getDeclaredConstructor(types);
            constructor.setAccessible(true);
            return constructor.newInstance(initials);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException e) {
            throw new IllegalStateException(e);
        }
    }

    private static Object[] map(final JsonObject jsonObject, final RecordComponent[] recordComponents) {
        final Object[] result = new Object[recordComponents.length];
        for (int index = 0; index < recordComponents.length; ++index) {
            final RecordComponent recordComponent = recordComponents[index];
            final String name = recordComponent.getName();
            result[index] = map(recordComponent.getType(), jsonObject.get(name));
        }
        return result;
    }

    private static Object map(final Class<?> type, final JsonValue jsonValue) {
        if (JsonValue.NULL == jsonValue) {
            return null;
        } else {
            return mapNonNull(type, jsonValue);
        }
    }

    private static Object mapNonNull(final Class<?> type, final JsonValue jsonValue) {
        if (type.isRecord()) {
            return mapRecord(type, (JsonObject) jsonValue);
        } else {
            return mapNoRecord(type, jsonValue);
        }
    }

    private static Object mapNoRecord(final Class<?> type, final JsonValue jsonValue) {
        return switch (Mappable.of(type)) {
            case BOOLEAN -> ((JsonBoolean) jsonValue).value();
            case BYTE -> ((JsonNumber) jsonValue).value().byteValueExact();
            case SHORT -> ((JsonNumber) jsonValue).value().shortValueExact();
            case INT -> ((JsonNumber) jsonValue).value().intValueExact();
            case LONG -> ((JsonNumber) jsonValue).value().longValueExact();
            case FLOAT -> ((JsonNumber) jsonValue).value().floatValue();
            case DOUBLE -> ((JsonNumber) jsonValue).value().doubleValue();
            case BIG_INTEGER -> ((JsonNumber) jsonValue).value().toBigIntegerExact();
            case BIG_DECIMAL -> ((JsonNumber) jsonValue).value();
            case STRING -> ((JsonString) jsonValue).value();
            case ENUM -> enumValueOf(type, ((JsonString) jsonValue).value());
            case OTHER -> fail(type);
        };
    }

    private static Object enumValueOf(final Class<?> type, final String value) {
        return Stream.of(type.getEnumConstants())
                     .map(Enum.class::cast)
                     .filter(e -> e.name().equals(value))
                     .findAny()
                     .map(Object.class::cast)
                     .orElseGet(() -> fail(type));
    }

    private static Object fail(final Class<?> type) {
        throw new IllegalStateException("cannot map <%s>".formatted(type.getCanonicalName()));
    }

    private T map() {
        final JsonValue value = JsonParser.parse(source);
        if (value instanceof JsonObject jsonObject) {
            return recordClass.cast(mapRecord(recordClass, jsonObject));
        } else {
            throw new IllegalArgumentException(
                    "expected JsonObject - but was %s".formatted(value.getClass().getSimpleName()));
        }
    }

    private enum Mappable {

        BOOLEAN(boolean.class, Boolean.class),
        BYTE(byte.class, Byte.class),
        SHORT(short.class, Short.class),
        INT(int.class, Integer.class),
        LONG(long.class, Long.class),
        FLOAT(float.class, Float.class),
        DOUBLE(double.class, Double.class),
        BIG_INTEGER(BigInteger.class),
        BIG_DECIMAL(BigDecimal.class),
        STRING(String.class),
        ENUM(),
        OTHER();

        private static final Values<Mappable> VALUES = Values.of(Mappable.class);

        private final Set<Class<?>> classes;

        Mappable(final Class<?>... classes) {
            this.classes = Set.of(classes);
        }

        public static Mappable of(final Class<?> type) {
            return VALUES.findAny(value -> value.classes.contains(type))
                         .orElseGet(() -> ofOther(type));
        }

        private static Mappable ofOther(final Class<?> type) {
            return type.isEnum() ? ENUM : OTHER;
        }
    }
}
