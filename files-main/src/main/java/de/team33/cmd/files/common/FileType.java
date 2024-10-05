package de.team33.cmd.files.common;

import de.team33.patterns.io.alpha.FileEntry;

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
            case ":spc" -> ByFile.SPC;
            case ":all" -> ByFile.ALL;
            default -> new ByExtensions(csvLower.split(","));
        };
    }

    public abstract boolean isTypeOf(FileEntry entry);

    public abstract String toPureName(FileEntry entry);

    public abstract String toExtension(FileEntry entry);

    private static class ByFile extends FileType {

        private static final ByFile DIR = new ByFile(FileEntry::isDirectory);
        private static final ByFile REG = new ByFile(FileEntry::isRegularFile);
        private static final ByFile SYM = new ByFile(FileEntry::isSymbolicLink);
        private static final ByFile SPC = new ByFile(FileEntry::isSpecial);
        private static final ByFile ALL = new ByFile(path -> true);

        private final Predicate<FileEntry> testing;

        private ByFile(final Predicate<FileEntry> testing) {
            this.testing = testing;
        }

        @Override
        public boolean isTypeOf(final FileEntry entry) {
            return testing.test(entry);
        }

        @Override
        public final String toPureName(final FileEntry entry) {
            final String name = entry.name();
            final int dotIndex = name.indexOf('.');
            return (0 > dotIndex) ? name : name.substring(0, dotIndex);
        }

        @Override
        public final String toExtension(final FileEntry entry) {
            final String name = entry.name();
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

        public final boolean isTypeOf(final FileEntry entry) {
            final String normalName = entry.name().toLowerCase();
            return extensions.stream()
                             .anyMatch(normalName::endsWith);
        }

        public final String toPureName(final FileEntry entry) {
            final String fullName = entry.name();
            return extensions.stream()
                             .filter(ext -> fullName.toLowerCase().endsWith(ext))
                             .findAny()
                             .map(ext -> fullName.substring(0, fullName.length() - ext.length()))
                             .orElse(null);
        }

        @Override
        public String toExtension(final FileEntry entry) {
            final String fullName = entry.name();
            return extensions.stream()
                             .filter(ext -> fullName.toLowerCase().endsWith(ext))
                             .findAny()
                             .orElse(null);
        }
    }
}
