package de.team33.cmd.files.moving;

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static java.util.function.Predicate.not;

enum Rule {

    FILE_YEAR("@Y", FileInfo::getLastModifiedYear),
    FILE_MONTH("@M", FileInfo::getLastModifiedMonth),
    FILE_DAY("@D", FileInfo::getLastModifiedDay),
    FILE_HOUR("@h", FileInfo::getLastModifiedHour),
    FILE_MINUTE("@m", FileInfo::getLastModifiedMinute),
    FILE_SECOND("@s", FileInfo::getLastModifiedSecond),
    FILE_NAME("@N", FileInfo::getFileName),
    FULL_NAME("@F", FileInfo::getFullName),
    EXTENSION("@X", FileInfo::getExtensionLC),
    PROCESSING_DIR("@P", FileInfo::getProcessingDir),
    PARENT_DIR("@p", FileInfo::getParentDir),
    REL_PATH("@R", FileInfo::getRelativePath),
    HASH("@#", FileInfo::getHash),
    TIMESTAMP("@!", FileInfo::getTimeId),
    AT("@@", fileInfo -> "@"),
    PLAIN(not(fragment -> fragment.startsWith("@")), null);

    private final Predicate<String> filter;
    private final Function<FileInfo, String> mapping;

    Rule(final String token, final Function<FileInfo, String> mapping) {
        this(fragment -> fragment.startsWith(token), mapping);
    }

    Rule(final Predicate<String> filter, final Function<FileInfo, String> mapping) {
        this.filter = filter;
        this.mapping = mapping;
    }

    static Function<FileInfo, String> map(final String token) {
        return Stream.of(values())
                     .filter(rule -> rule.filter.test(token))
                     .map(rule -> rule.mapping(token))
                     .findAny()
                     .orElseThrow(() -> new ResolverException(String.format(
                             "unknown token: '%s'", token)));
    }

    private Function<FileInfo, String> mapping(final String token) {
        if (null == mapping) {
            return fileInfo -> token;
        } else {
            final int[] indices = indices(token);
            return fileInfo -> substring(indices, mapping.apply(fileInfo));
        }
    }

    private String substring(final int[] indices, final String primary) {
        final int maxIndex = primary.length();
        final int beginIndex = Integer.min(indices[0], maxIndex);
        final int endIndex = Integer.min(indices[1], maxIndex);
        return primary.substring(beginIndex, endIndex);
    }

    private int[] indices(final String token) {
        final int start = token.indexOf('(');
        if (0 > start) {
            return new int[]{0, Integer.MAX_VALUE};
        }
        final int limit = token.indexOf(')');
        if (0 > limit) {
            throw new IllegalArgumentException("illegal token: '%s'".formatted(token));
        }
        final String[] stage = token.substring(start + 1, limit)
                                    .concat(",")
                                    .split(Pattern.quote(","), -1);
        final int[] result = new int[2];
        result[0] = stage[0].isBlank() ? 0 : Integer.parseInt(stage[0]);
        result[1] = stage[1].isBlank() ? Integer.MAX_VALUE : Integer.parseInt(stage[1]);
        return result;
    }
}
