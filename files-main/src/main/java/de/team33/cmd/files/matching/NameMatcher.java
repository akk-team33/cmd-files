package de.team33.cmd.files.matching;

import de.team33.patterns.files.iocaste.FileEntry;
import de.team33.patterns.io.deimos.TextIO;

import java.nio.file.Path;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class NameMatcher {

    private final Pattern pattern;

    private NameMatcher(final Method method, final CaseSensitivity sensitivity, final String value) {
        this.pattern = sensitivity.toPattern(method.toRegEx(value));
    }

    /**
     * @throws IllegalArgumentException if <em>pattern</em> is invalid.
     */
    public static NameMatcher parse(final String pattern) throws IllegalArgumentException {
        try {
            return parseEx(pattern);
        } catch (final InternalException e) {
            final String message = TextIO.read(NameMatcher.class, "NameMatcher.txt")
                                         .formatted(pattern, e.getMessage());
            throw new IllegalArgumentException(message, e);
        }
    }

    private static NameMatcher parseEx(final String pattern) throws InternalException {
        final String[] parts = pattern.split(":", -1);
        return switch (parts.length) {
            case 1 -> parseEx("", "", parts[0]);
            case 2 -> parseEx(parts[0], "", parts[1]);
            case 3 -> parseEx(parts[0], parts[1], parts[2]);
            default -> throw new InternalException("The VALUE must not contain ':' (a colon)!");
        };
    }

    private static NameMatcher parseEx(final String method,
                                       final String option,
                                       final String value) throws InternalException {
        return new NameMatcher(Method.parse(method), CaseSensitivity.parse(option), value);
    }

    public final boolean matches(final String name) {
        return pattern.matcher(name).matches();
    }

    public final boolean matches(final Path path) {
        return matches(path.getFileName().toString());
    }

    public final boolean matches(final FileEntry entry) {
        return matches(entry.name());
    }

    public final Predicate<String> toNameFilter() {
        return this::matches;
    }

    public final Predicate<Path> toPathFilter() {
        return this::matches;
    }

    public final Predicate<FileEntry> toFileEntryFilter() {
        return this::matches;
    }
}
