package de.team33.cmd.files.matching;

import de.team33.patterns.enums.pan.Values;
import de.team33.patterns.files.iocaste.FileEntry;
import de.team33.patterns.io.deimos.TextIO;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.function.Predicate.not;

public class TypeMatcher {

    private final Set<String> extensions;
    private final Set<FileEntry.Type> fileTypes;

    private TypeMatcher(final Set<String> extensions, final Set<FileEntry.Type> fileTypes) {
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
        final Set<FileEntry.Type> types = TypeToken.parse(fileTypes);
        final Set<String> extensionSet = EXT_PATTERN.splitAsStream(extensions)
                                                    .filter(not(String::isBlank))
                                                    .map(String::toLowerCase)
                                                    .collect(Collectors.toSet());
        return new TypeMatcher(extensionSet, types);
    }

    private static Set<FileEntry.Type> parseTypes(final String fileTypes) {
        throw new UnsupportedOperationException("not yet implemented");
    }

    private enum TypeToken {
        A(FileEntry.Type.values()),
        D(FileEntry.Type.DIRECTORY),
        F(FileEntry.Type.REGULAR_FILE),
        L(FileEntry.Type.SYMBOLIC_LINK),
        S(FileEntry.Type.SPECIAL_FILE);

        private static final Values<TypeToken> VALUES = Values.of(TypeToken.class);

        private final Set<FileEntry.Type> types;

        TypeToken(final FileEntry.Type... type) {
            types = EnumSet.copyOf(List.of(type));
        }

        static Set<FileEntry.Type> parse(final String tokens) throws InternalException {
            if (tokens.isEmpty()) {
                throw new InternalException("No file type(s) specified");
            }
            final Set<FileEntry.Type> result = EnumSet.noneOf(FileEntry.Type.class);
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

    public final boolean matches(final FileEntry entry) {
        return extensions.contains(extensionOf(entry.path().getFileName().toString()));
    }

    private String extensionOf(final String fileName) {
        final int index = fileName.lastIndexOf('.');
        return (0 > index) ? null : fileName.substring(index + 1).toLowerCase();
    }
}
