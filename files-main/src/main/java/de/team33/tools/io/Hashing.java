package de.team33.tools.io;

import de.team33.patterns.hashing.pandia.Algorithm;
import de.team33.patterns.hashing.pandia.Hash;

import java.util.Optional;
import java.util.regex.Pattern;

public final class Hashing {

    private static final String OLD_DIGITS = "0123456789abcdef";
    private static final Pattern OLD_PATTERN = Pattern.compile("#[" + OLD_DIGITS + "]{40}",
                                                               Pattern.CASE_INSENSITIVE);

    public static Optional<Hash> oldHashByName(final String name) {
        return OLD_PATTERN.matcher(name)
                          .results()
                          .findAny()
                          .map(match -> name.substring(match.start() + 1, match.end()))
                          .map(hash -> Algorithm.SHA_1.parse(hash, OLD_DIGITS));
    }
}
