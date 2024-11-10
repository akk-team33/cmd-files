package de.team33.tools.io;

import de.team33.patterns.io.phobos.FileEntry;

import java.time.temporal.ChronoUnit;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

public class LazyTiming {

    private static final String PATTERN_CORE = "[0123456789abcdefABCDEF]{1,16}";

    private final Pattern pattern;
    private final int prefixLength;
    private final int postfixLength;

    public LazyTiming(final String prefix, final String postfix) {
        this.pattern = Pattern.compile(Pattern.quote(prefix) + PATTERN_CORE + Pattern.quote(postfix));
        this.prefixLength = prefix.length();
        this.postfixLength = prefix.length();
    }

    public final String valueOf(FileEntry entry) {
        final String fileName = entry.path().getFileName().toString();
        return pattern.matcher(fileName)
                      .results()
                      .findAny()
                      .map(match -> extract(fileName, match))
                      .orElseGet(() -> strict(entry));
    }

    private String extract(final String fileName, final MatchResult result) {
        final int start = result.start() + prefixLength;
        final int end = result.end() - postfixLength;
        return fileName.substring(start, end);
    }

    private String strict(final FileEntry entry) {
        return String.format("%x", entry.lastModified()
                                        .truncatedTo(ChronoUnit.SECONDS)
                                        .toEpochMilli() / 1000);
    }
}
