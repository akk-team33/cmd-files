package de.team33.cmd.files.finding;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.function.Predicate.not;

class WildcardString {

    private static final Pattern WILDCARD = Pattern.compile("[*?]");

    static String parse(final String origin) {
        final Matcher matcher = WILDCARD.matcher(origin);
        final List<String> result = new LinkedList<>();
        final Index index = new Index();
        matcher.results().forEach(matchResult -> {
            final int start = matchResult.start();
            result.add(origin.substring(index.value, start));
            result.add(origin.substring(start, index.value = matchResult.end()));
        });
        result.add(origin.substring(index.value));
        return result.stream()
                     .filter(not(String::isEmpty))
                     .map(subs -> switch (subs) {
                         case "*" -> ".*";
                         case "?" -> ".";
                         default -> Pattern.quote(subs);
                     })
                     .collect(Collectors.joining());
    }

    private static class Index {
        private int value = 0;
    }
}
