package de.team33.cmd.files.main.common;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Stream;

import static java.util.Collections.unmodifiableSet;
import static java.util.stream.Collectors.toCollection;

public abstract class FileType {

    private static final Collector<String, ?, Set<String>> COLLECTOR = toCollection(TreeSet::new);

    public static FileType parse(final String csvExtensions) {
        final String csvLower = csvExtensions.toLowerCase();
        return switch (csvLower) {
            case ":dir" -> ByFile.DIR;
            case ":reg" -> ByFile.REG;
            case ":sym" -> ByFile.SYM;
            case ":all" -> ByFile.ALL;
            default -> new ByExtensions(csvLower.split(","));
        };
    }

    public abstract boolean isTypeOf(Path path);

    public abstract String toPureName(Path path);

    public abstract String toExtension(Path path);

    private static class ByFile extends FileType {

        private static final ByFile DIR = new ByFile(Files::isDirectory);
        private static final ByFile REG = new ByFile(Files::isRegularFile);
        private static final ByFile SYM = new ByFile(Files::isSymbolicLink);
        private static final ByFile ALL = new ByFile(path -> true);

        private final Predicate<Path> testing;

        private ByFile(final Predicate<Path> testing) {
            this.testing = testing;
        }

        @Override
        public boolean isTypeOf(final Path path) {
            return testing.test(path);
        }

        @Override
        public final String toPureName(final Path path) {
            final String name = path.getFileName().toString();
            final int dotIndex = name.indexOf('.');
            return (0 > dotIndex) ? name : name.substring(0, dotIndex);
        }

        @Override
        public final String toExtension(final Path path) {
            final String name = path.getFileName().toString();
            final int dotIndex = name.indexOf('.');
            return (0 > dotIndex) ? null : name.substring(dotIndex).toLowerCase();
        }
    }

    private static class ByExtensions extends FileType {

        private final Set<String> extensions;

        private ByExtensions(final String[] extensions) {
            this.extensions = unmodifiableSet(Stream.of(extensions)
                                                    .map(ext -> "." + ext)
                                                    .collect(COLLECTOR));
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

        @Override
        public String toExtension(Path path) {
            final String fullName = path.getFileName().toString();
            return extensions.stream()
                             .filter(ext -> fullName.toLowerCase().endsWith(ext))
                             .findAny()
                             .orElse(null);
        }
    }
}
