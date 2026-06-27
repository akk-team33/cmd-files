package de.team33.cmd.files.listing;

import de.team33.cmd.files.matching.WildcardString;

import java.util.regex.Pattern;

/**
 * Abstracts an expression for a query relating to (file) names.
 * More precisely: a (file) name may or may not match such an expression.
 * <p>
 * Use {@link #parse(String)} to get an Instance.
 */
public class NameFilter {

    private static final String DOT = ".";
    private static final String COLON = ":";

    private final Pattern pattern;

    private NameFilter(final String regExp) {
        this.pattern = Pattern.compile(regExp, Pattern.CASE_INSENSITIVE);
    }

    /**
     * Parses a string that represents a query relating to (file) names.
     */
    public static NameFilter parse(final String queryString) {
        if (queryString.startsWith(COLON)) {
            return parse("", queryString.substring(1));
        } else if (queryString.startsWith(DOT)) {
            return parse("", queryString);
        } else {
            return parse("(?!\\.)", queryString);
        }
    }

    private static NameFilter parse(final String rxHead, final String queryString) {
        return new NameFilter(rxHead + WildcardString.toRegExp(queryString));
    }

    public final boolean test(final String name) {
        return pattern.matcher(name).matches();
    }
}
