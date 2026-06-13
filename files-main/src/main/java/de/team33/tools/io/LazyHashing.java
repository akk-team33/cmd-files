package de.team33.tools.io;

import java.nio.file.Path;
import java.util.regex.MatchResult;

public class LazyHashing implements FileHashing {

    private final FileHashing backing;

    public LazyHashing(final FileHashing backing) {
        this.backing = backing;
    }

    @Override
    public final String hash(final Path filePath) {
        return Hashing.oldHashStringByName(filePath.getFileName().toString())
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
