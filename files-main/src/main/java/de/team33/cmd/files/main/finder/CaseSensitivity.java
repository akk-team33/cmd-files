package de.team33.cmd.files.main.finder;

import de.team33.patterns.enums.alpha.Values;

import java.util.function.Function;
import java.util.regex.Pattern;

enum CaseSensitivity {

    CS(Pattern::compile),
    CI(regex -> Pattern.compile(regex, Pattern.CASE_INSENSITIVE));

    private final Function<String, Pattern> toPattern;

    private static final Values<CaseSensitivity> VALUES = Values.of(CaseSensitivity.class);

    CaseSensitivity(final Function<String, Pattern> toPattern) {
        this.toPattern = toPattern;
    }

    public static CaseSensitivity parse(final String name) {
        return name.isEmpty() ? CI : VALUES.findAny(value -> value.name().equalsIgnoreCase(name))
                                           .orElseThrow(() -> newIllegalArgumentException(name));
    }

    private static IllegalArgumentException newIllegalArgumentException(String name) {
        return new IllegalArgumentException("\"" + name + "\" is not a valid case sensitivity!");
    }

    public final Pattern toPattern(final String regex) {
        return toPattern.apply(regex);
    }
}
