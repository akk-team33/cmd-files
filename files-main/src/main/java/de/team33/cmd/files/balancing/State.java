package de.team33.cmd.files.balancing;

import de.team33.patterns.io.phobos.FileEntry;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

public enum State {

    INCOMPATIBLE,
    TARGET_IS_MISSING,
    SOURCE_IS_MISSING,
    TARGET_IS_MORE_RECENT,
    SOURCE_IS_MORE_RECENT,
    AMBIGUOUS,
    BALANCED;

    public static State of(final FileEntry source, final FileEntry target) {
        if (source.isRegularFile()) {
            if (target.isRegularFile()) {
                return ofRegular(source, target);
            } else if (target.exists()) {
                return INCOMPATIBLE;
            } else {
                return TARGET_IS_MISSING;
            }
        } else if (source.exists()) {
            return INCOMPATIBLE;
        } else {
            if (target.isRegularFile()) {
                return SOURCE_IS_MISSING;
            } else if (target.exists()) {
                return INCOMPATIBLE;
            } else {
                return BALANCED;
            }
        }
    }

    private static State ofRegular(final FileEntry source, final FileEntry target) {
        final Instant sourceTime = source.lastModified();
        final Instant targetTime = target.lastModified();
        if (sourceTime.minus(1, ChronoUnit.SECONDS).compareTo(targetTime) > 0) {
            return SOURCE_IS_MORE_RECENT;
        } else if (targetTime.minus(1, ChronoUnit.SECONDS).compareTo(sourceTime) > 0) {
            return TARGET_IS_MORE_RECENT;
        } else if (source.size() != target.size()) {
            return AMBIGUOUS;
        } else {
            return BALANCED;
        }
    }
}
