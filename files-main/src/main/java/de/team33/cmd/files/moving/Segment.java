package de.team33.cmd.files.moving;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

class Segment {

    private static final Pattern TOKEN_PATTERN = Pattern.compile("@.");
    private static final Pattern MULTI_DOT_PATTERN = Pattern.compile("\\.{2,}");
    private static final Pattern END_DOT_PATTERN = Pattern.compile("\\.$");

    private final List<Fragment> fragments;

    private Segment(final List<Fragment> fragments) {
        this.fragments = fragments;
    }

    static Segment parse(final String rule) {
        final List<Fragment> fragments = split(rule).stream()
                                                    .map(Fragment::parse)
                                                    .toList();
        return new Segment(fragments);
    }

    private static List<String> split(final String rule) {
        final Matcher matcher = TOKEN_PATTERN.matcher(rule);
        final List<String> result = new LinkedList<>();
        final Iterator<MatchResult> matches = matcher.results().toList().iterator();
        int start = 0;
        while (matches.hasNext()) {
            final MatchResult head = matches.next();
            result.add(rule.substring(start, head.start()));
            result.add(rule.substring(head.start(), head.end()));
            start = head.end();
        }
        result.add(rule.substring(start));
        return List.copyOf(result);
    }

    public final String map(final FileInfo fileInfo) {
        return normal(fragments.stream()
                               .map(fragment -> fragment.map(fileInfo))
                               .collect(Collectors.joining()));
    }

    private String normal(final String collected) {
        if (Arrays.asList(".", "..").contains(collected))
            return collected;
        else
            return END_DOT_PATTERN.matcher(MULTI_DOT_PATTERN.matcher(collected)
                                                            .replaceAll("."))
                                  .replaceAll("");
    }
}