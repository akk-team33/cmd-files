package de.team33.cmd.files.matching;

import de.team33.patterns.enums.alpha.Values;
import de.team33.patterns.io.alpha.FileEntry;
import de.team33.patterns.io.alpha.FileType;
import de.team33.patterns.io.deimos.TextIO;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.function.Predicate.*;

public class TypeMatcher {

    private final Set<String> extensions;
    private final Set<FileType> fileTypes;

    private TypeMatcher(final Set<String> extensions, final Set<FileType> fileTypes) {
        this.extensions = extensions;
        this.fileTypes = fileTypes;
    }

    /**
     * @throws IllegalArgumentException if <em>pattern</em> is invalid.
     */
    public static TypeMatcher parse(final String pattern) {
        try {
            return parseEx(pattern);
        } catch (final InternalException e) {
            final String message = TextIO.read(TypeMatcher.class, "TypeMatcher.txt")
                                         .formatted(pattern, e.getMessage());
            throw new IllegalArgumentException(message, e);
        }
    }

    private static TypeMatcher parseEx(final String pattern) throws InternalException {
        final String[] parts = pattern.split(":", 2);
        if (1 == parts.length) {
            return parseEx(parts[0], "");
        } else {
            return parseEx(parts[0], parts[1]);
        }
    }

    private static final Pattern EXT_PATTERN = Pattern.compile(Pattern.quote(","));

    private static TypeMatcher parseEx(final String fileTypes, final String extensions) throws InternalException {
        final Set<FileType> types = TypeToken.parse(fileTypes);
        final Set<String> extensionSet = EXT_PATTERN.splitAsStream(extensions)
                                                    .filter(not(String::isBlank))
                                                    .map(String::toLowerCase)
                                                    .collect(Collectors.toSet());
        return new TypeMatcher(extensionSet, types);
    }

    private enum TypeToken {
        A(FileType.values()),
        D(FileType.DIRECTORY),
        F(FileType.REGULAR),
        L(FileType.SYMBOLIC),
        S(FileType.SPECIAL);

        private static final Values<TypeToken> VALUES = Values.of(TypeToken.class);

        private final Set<FileType> types;

        TypeToken(final FileType ... type) {
            types = EnumSet.copyOf(List.of(type));
        }

        static Set<FileType> parse(final String tokens) throws InternalException {
            if (tokens.isEmpty()) {
                throw new InternalException("No file type(s) specified");
            }
            final Set<FileType> result = EnumSet.noneOf(FileType.class);
            for (int index = 0; index < tokens.length(); ++index) {
                final String single = tokens.substring(index, index + 1);
                final TypeToken token = VALUES.findAny(value -> value.name().equalsIgnoreCase(single))
                                              .orElseThrow(() -> newException(single));
                result.addAll(token.types);
            }
            return result;
        }

        private static InternalException newException(final String token) {
            return new InternalException("invalid file type token: '%s'".formatted(token));
        }
    }

    private static Set<FileType> parseTypes(final String fileTypes) {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public final boolean matches(final FileEntry entry) {
        return extensions.contains(extensionOf(entry.path().getFileName().toString()));
    }

    private String extensionOf(final String fileName) {
        final int index = fileName.lastIndexOf('.');
        return (0 > index) ? null : fileName.substring(index + 1).toLowerCase();
    }
}
