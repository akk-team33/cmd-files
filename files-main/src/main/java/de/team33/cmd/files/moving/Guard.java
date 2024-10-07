package de.team33.cmd.files.moving;

import de.team33.patterns.io.alpha.FileEntry;

import java.util.Set;

public class Guard {

    public static final String KEEP = "(keep)";
    public static final String DEDUPED_PREV = "(deduped-prev).txt";
    public static final String DEDUPED_NEXT = "(deduped-next).txt";

    private static final Set<String> PROTECTED = Set.of(KEEP, DEDUPED_PREV, DEDUPED_NEXT);

    public static boolean unprotected(final FileEntry entry) {
        return !PROTECTED.contains(entry.name());
    }
}
