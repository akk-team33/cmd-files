package de.team33.cmd.files.finder;

import de.team33.patterns.enums.alpha.Values;

import java.util.function.Function;

enum Method {

    RX(Function.identity()),
    WC(WildcardString::parse);

    private static final Values<Method> VALUES = Values.of(Method.class);

    private final Function<String, String> toRegEx;

    Method(Function<String, String> toRegEx) {
        this.toRegEx = toRegEx;
    }

    public static Method parse(final String name) {
        return name.isEmpty() ? WC : VALUES.findAny(value -> value.name().equalsIgnoreCase(name))
                                           .orElseThrow(() -> newIllegalArgumentException(name));
    }

    private static IllegalArgumentException newIllegalArgumentException(String name) {
        return new IllegalArgumentException("\"" + name + "\" is not a valid find method!");
    }

    public final String toRegEx(final String origin) {
        return toRegEx.apply(origin);
    }
}
