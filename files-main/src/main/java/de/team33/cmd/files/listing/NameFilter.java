package de.team33.cmd.files.listing;

import de.team33.cmd.files.matching.WildcardString;
import de.team33.patterns.io.adrastea.FileEntry;

import java.nio.file.Path;
import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * Abstracts an expression for a query relating to (file) names.
 * More precisely: a (file) name may or may not match such an expression.
 * <p>
 * Use {@link #parse(String)} to get an Instance.
 */
public class NameFilter {

    private static final String PERIOD = ".";
    private static final String COLON = ":";

    private final Pattern pattern;

    private NameFilter(final String regExp, final Mode mode) {
        this.pattern = mode.compiler.apply(regExp);
    }

    /**
     * Parses a string that represents a <em>pattern</em> for filenames.
     * The string can contain the usual wildcards ('?', '*'), with their corresponding standard meanings.
     * <p>
     * Normally, the resulting NameFilter will reject filenames that begin with a period ('.')
     * (the Unix standard for 'hidden' files).
     * However, if the <em>pattern</em> begins with a colon (':'), the rest of the <em>pattern</em> is applied
     * equally to any filename.
     * <p>
     * If the <em>pattern</em> begins with a period, it makes sense not to reject filenames that begin with a period.
     * Naturally, all filenames that do NOT begin with a period will then be rejected.
     */
    public static NameFilter parse(final String pattern) {
        if (pattern.startsWith(COLON)) {
            return parse("", pattern.substring(1));
        } else if (pattern.startsWith(PERIOD)) {
            return parse("", pattern);
        } else {
            return parse("(?!\\.)", pattern);
        }
    }

    private static NameFilter parse(final String rxHead, final String wildcardString) {
        return new NameFilter(rxHead + WildcardString.toRegExp(wildcardString), Mode.IGNORE_CASE);
    }

    public final boolean test(final String name) {
        return pattern.matcher(name).matches();
    }

    public final boolean test(final Path path) {
        return test(FileEntry.original(path));
    }

    public final boolean test(final FileEntry entry) {
        return test(entry.name());
    }

    @Override
    public final boolean equals(final Object obj) {
        return (this == obj) || ((obj instanceof NameFilter other) && toString().equals(other.toString()));
    }

    @Override
    public final int hashCode() {
        return toString().hashCode();
    }

    @Override
    public final String toString() {
        return pattern.toString();
    }

    public enum Mode {

        IGNORE_CASE(regExp -> Pattern.compile(regExp, Pattern.CASE_INSENSITIVE)),
        RESPECT_CASE(Pattern::compile);

        private final Function<String, Pattern> compiler;

        Mode(final Function<String, Pattern> compiler) {
            this.compiler = compiler;
        }
    }
}
