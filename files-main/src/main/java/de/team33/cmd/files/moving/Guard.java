package de.team33.cmd.files.moving;

import de.team33.patterns.io.phobos.FileEntry;

import java.util.Set;

public class Guard {

    public static final String KEEP = "(keep)";
    public static final String DEDUPED_INDEX = "(deduped).txt";
    public static final String DEDUPE_PATH_ID = "(dedupe-id).txt";
    public static final String DEDUPE_NEXT = "(dedupe-next).txt";

    private static final Set<String> PROTECTED = Set.of(KEEP, DEDUPED_INDEX, DEDUPE_PATH_ID, DEDUPE_NEXT);

    public static boolean unprotected(final FileEntry entry) {
        return !PROTECTED.contains(entry.name());
    }
}
