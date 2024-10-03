package de.team33.tools.io;

import de.team33.patterns.io.alpha.FileEntry;

import java.util.regex.MatchResult;
import java.util.regex.Pattern;

import static java.lang.String.format;

public class LazyDating implements FileDating {

    private static final String PATTERN_FORMAT = "%s[0123456789abcdefABCDEF]{1,16}";

    private final FileDating strict;
    private final Pattern pattern;
    private final int prefixLength;

    private LazyDating(final String prefix, final FileDating strict) {
        this.strict = strict;
        this.pattern = Pattern.compile(format(PATTERN_FORMAT, Pattern.quote(prefix)));
        this.prefixLength = prefix.length();
    }

    public static LazyDating of(final String prefix) {
        return new LazyDating(prefix, new StrictDating());
    }

    @Override
    public String timestamp(final FileEntry fileEntry) {
        final String fileName = fileEntry.path().getFileName().toString();
        return pattern.matcher(fileName)
                      .results()
                      .findAny()
                      .map(match -> extract(fileName, match))
                      .orElseGet(() -> strict.timestamp(fileEntry));
    }

    private String extract(final String fileName, final MatchResult match) {
        final int start = match.start() + prefixLength;
        final int end = match.end();
        return fileName.substring(start, end);
    }
}
