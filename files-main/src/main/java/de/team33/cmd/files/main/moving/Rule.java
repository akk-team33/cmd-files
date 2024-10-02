package de.team33.cmd.files.main.moving;

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.function.Predicate.not;

enum Rule {

    FILE_YEAR("@Y"::equals, rule -> FileInfo::getLastModifiedYear),
    FILE_MONTH("@M"::equals, rule -> FileInfo::getLastModifiedMonth),
    FILE_DAY("@D"::equals, rule -> FileInfo::getLastModifiedDay),
    FILE_NAME("@N"::equals, rule -> FileInfo::getFileName),
    FULL_NAME("@F"::equals, rule -> FileInfo::getFullName),
    EXTENSION("@X"::equals, rule -> FileInfo::getExtensionLC),
    REL_PATH("@P"::equals, rule -> FileInfo::getRelativePath),
    HASH("@#"::equals, rule -> FileInfo::getHash),
    AT("@@"::equals, rule -> fileInfo -> "@"),
    PLAIN(not(rule -> rule.startsWith("@")), rule -> fileInfo -> rule);

    private final Predicate<String> filter;
    private final Function<String, Function<FileInfo, String>> mapping;

    Rule(final Predicate<String> filter, final Function<String, Function<FileInfo, String>> mapping) {
        this.filter = filter;
        this.mapping = mapping;
    }

    static Function<FileInfo, String> map(final String token) {
        return Stream.of(values())
                     .filter(value -> value.filter.test(token))
                     .map(t -> t.mapping.apply(token))
                     .findAny()
                     .orElseThrow(() -> new ResolverException(String.format(
                             "unknown token: '%s'", token)));
    }
}