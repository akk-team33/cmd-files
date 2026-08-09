package de.team33.cmd.files.job;

import de.team33.cmd.files.matching.TypeFilter;
import de.team33.patterns.files.pluto.FileEntry;
import de.team33.patterns.functions.alpha.Predicates;

import java.util.Optional;
import java.util.function.Predicate;

interface FilteringTypes {

    String types();

    default Predicate<FileEntry> typeInclusion() {
        return Optional.ofNullable(types())
                       .map(TypeFilter::parse)
                       .orElseGet(Predicates::accept);
    }
}
