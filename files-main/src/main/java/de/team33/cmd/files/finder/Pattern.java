package de.team33.cmd.files.finder;

import de.team33.patterns.io.alpha.FileEntry;

import java.util.function.Predicate;

public class Pattern {

    private final java.util.regex.Pattern rxPattern;

    private Pattern(final Method method, final CaseSensitivity sensitivity, final String value) {
        this.rxPattern = sensitivity.toPattern(method.toRegEx(value));
    }

    public static Pattern parse(final String pattern) {
        final String[] parts = pattern.split(":");
        if (1 == parts.length) {
            return parse("", parts[0]);
        } else if (2 == parts.length) {
            return parse(parts[0], parts[1]);
        } else {
            throw new IllegalArgumentException("\"" + pattern + "\" is not a valid find pattern!");
        }
    }

    private static Pattern parse(final String head, String tail) {
        final String[] parts = head.split("/");
        if (1 == parts.length) {
            return parse(parts[0], "", tail);
        } else if (2 == parts.length) {
            return parse(parts[0], parts[1], tail);
        } else {
            throw new IllegalArgumentException("\"" + head + "\" is not a valid find method!");
        }
    }

    private static Pattern parse(final String method, final String sensitivity, final String value) {
        return new Pattern(Method.parse(method), CaseSensitivity.parse(sensitivity), value);
    }

    public final Predicate<FileEntry> matcher() {
        return entry -> rxPattern.matcher(entry.name()).matches();
    }
}
