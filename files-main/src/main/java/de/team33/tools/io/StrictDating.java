package de.team33.tools.io;

import de.team33.patterns.io.alpha.FileEntry;

import java.time.temporal.ChronoUnit;

public class StrictDating implements FileDating {

    @Override
    public final String timestamp(final FileEntry fileEntry) {
        return String.format("%x", fileEntry.lastModified()
                                            .truncatedTo(ChronoUnit.SECONDS)
                                            .toEpochMilli() / 1000);
    }
}
