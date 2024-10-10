package de.team33.cmd.files.matching;

import de.team33.patterns.io.alpha.FileEntry;
import de.team33.patterns.io.alpha.FileType;
import de.team33.patterns.io.deimos.TextIO;

import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
            final String message = TextIO.read(NameMatcher.class, "TypeMatcher.txt")
                                         .formatted(pattern, e.getMessage());
            throw new IllegalArgumentException(message, e);
        }
    }

    private static TypeMatcher parseEx(final String pattern) throws InternalException {
        final String[] parts = pattern.split(":", -1);
        if (1 == parts.length) {
            return parseEx(parts[0], "");
        } else if (2 == parts.length) {
            return parseEx(parts[0], parts[1]);
        } else {
            throw new InternalException("TODO ... must not contain ':' (a colon)!");
        }
    }

    private static TypeMatcher parseEx(final String extensions, final String fileTypes) throws InternalException {
        final Set<String> exts = Stream.of(extensions.split(","))
                                       .map(String::toLowerCase)
                                       .collect(Collectors.toSet());
        final Set<FileType> types = EnumSet.allOf(FileType.class); // TODO
        return new TypeMatcher(exts, types);
    }

    public final boolean matches(final FileEntry entry) {
        return extensions.contains(extensionOf(entry.path().getFileName().toString()));
    }

    private String extensionOf(final String fileName) {
        final int index = fileName.lastIndexOf('.');
        return (0 > index) ? null : fileName.substring(index + 1).toLowerCase();
    }
}
