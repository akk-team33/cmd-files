package de.team33.cmd.files.job;

import de.team33.cmd.files.matching.NameMatcher;
import de.team33.patterns.files.pluto.FileEntry;
import de.team33.patterns.functions.alpha.Predicates;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

interface FilteringNames {

    String[] namePatterns();

    String[] excludePatterns();

    default Predicate<FileEntry> nameInclusion() {
        return Optional.ofNullable(namePatterns()).stream()
                       .flatMap(Stream::of)
                       .map(NameMatcher::parse)
                       .map(NameMatcher::toFileEntryFilter)
                       .reduce(Predicates.accept(), Predicate::and);
    }

    default Predicate<FileEntry> nameExclusion() {
        return Optional.ofNullable(excludePatterns()).stream()
                       .flatMap(Stream::of)
                       .map(NameMatcher::parse)
                       .map(NameMatcher::toFileEntryFilter)
                       .reduce(Predicates.reject(), Predicate::or)
                       .negate();
    }
}
