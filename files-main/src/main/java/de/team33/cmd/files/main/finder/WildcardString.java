package de.team33.cmd.files.main.finder;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.function.Predicate.*;

class WildcardString {

    private static final Pattern WILDCARD = Pattern.compile("[*?]");

    public static String parse(final String origin) {
        final Matcher matcher = WILDCARD.matcher(origin);
        final List<String> result = new LinkedList<>();
        final Counter counter = new Counter();
        matcher.results().forEach(matchResult -> {
            final int start = matchResult.start();
            result.add(origin.substring(counter.value, start));
            result.add(origin.substring(start, counter.value = matchResult.end()));
        });
        result.add(origin.substring(counter.value));
        return result.stream()
                     .filter(not(String::isEmpty))
                     .map(subs -> switch (subs) {
                         case "*" -> ".*";
                         case "?" -> ".";
                         default -> Pattern.quote(subs);
                     })
                     .collect(Collectors.joining());
    }

    private static class Counter {
        private int value = 0;
    }
}
