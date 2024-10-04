package de.team33.cmd.files.main.common;

import de.team33.patterns.io.alpha.FileEntry;
import de.team33.tools.io.LazyTiming;

public class TimeId {

    private static final String PREFIX = "[";
    private static final String POSTFIX = "]";
    private static final LazyTiming CORE = new LazyTiming(PREFIX, POSTFIX);

    public static String valueOf(final FileEntry entry) {
        return PREFIX + CORE.valueOf(entry) + POSTFIX;
    }

    public static String coreValueOf(final FileEntry entry) {
        return CORE.valueOf(entry);
    }
}
