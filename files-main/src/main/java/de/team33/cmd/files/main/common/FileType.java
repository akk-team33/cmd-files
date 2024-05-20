package de.team33.cmd.files.main.common;

import java.nio.file.Path;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collector;
import java.util.stream.Stream;

import static java.util.Collections.unmodifiableSet;
import static java.util.stream.Collectors.toCollection;

public class FileType {

    private static final Collector<String, ?, Set<String>> COLLECTOR = toCollection(TreeSet::new);

    private final Set<String> extensions;

    private FileType(final String[] extensions) {
        this.extensions = unmodifiableSet(Stream.of(extensions)
                                                .map(ext -> "." + ext)
                                                .collect(COLLECTOR));
    }

    public static FileType parse(final String csvEtensions) {
        return new FileType(csvEtensions.toLowerCase().split(","));
    }

    public final boolean isTypeOf(final Path path) {
        final String normalName = path.getFileName().toString().toLowerCase();
        return extensions.stream()
                         .anyMatch(normalName::endsWith);
    }

    public final String toPureName(final Path path) {
        final String fullName = path.getFileName().toString();
        return extensions.stream()
                         .filter(ext -> fullName.toLowerCase().endsWith(ext))
                         .findAny()
                         .map(ext -> fullName.substring(0, fullName.length() - ext.length()))
                         .orElse(null);
    }
}
