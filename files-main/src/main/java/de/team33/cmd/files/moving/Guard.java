package de.team33.cmd.files.moving;

import de.team33.patterns.io.alpha.FileEntry;

import java.util.Set;

public class Guard {

    public static final String KEEP = "(keep)";
    public static final String DEDUPED_INDEX = "(deduped).txt";
    public static final String DEDUPED_INDEX_BAK = "(deduped)-00.txt";

    private static final Set<String> PROTECTED = Set.of(KEEP, DEDUPED_INDEX, DEDUPED_INDEX_BAK);

    public static boolean unprotected(final FileEntry entry) {
        return !PROTECTED.contains(entry.name());
    }
}
