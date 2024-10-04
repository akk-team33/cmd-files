package de.team33.tools.io;

import java.nio.file.Path;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

import static java.lang.String.format;

public class LazyHashing implements FileHashing {

    private static final String PATTERN_FORMAT = "%s[0123456789abcdefABCDEF]{%d}";

    private final FileHashing backing;
    private final Pattern pattern;

    public LazyHashing(final String prefix, final FileHashing backing) {
        this.backing = backing;
        this.pattern = Pattern.compile(format(PATTERN_FORMAT, prefix, backing.resultLength()));
    }

    @Override
    public final String hash(final Path filePath) {
        final String fileName = filePath.getFileName().toString();
        return pattern.matcher(fileName)
                      .results()
                      .findAny()
                      .map(match -> extract(fileName, match))
                      .orElseGet(() -> backing.hash(filePath));
    }

    private String extract(final String fileName, final MatchResult match) {
        final int start = match.start() + 1;
        final int end = start + backing.resultLength();
        return fileName.substring(start, end);
    }

    @Override
    public final String algorithm() {
        return backing.algorithm();
    }

    @Override
    public final int resultLength() {
        return backing.resultLength();
    }
}
