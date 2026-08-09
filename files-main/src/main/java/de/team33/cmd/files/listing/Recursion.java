package de.team33.cmd.files.listing;

import de.team33.patterns.files.pluto.FileEntry;
import de.team33.patterns.files.styx.Styx;

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

public enum Recursion {

    FLAT(Styx::children),
    VISIBLE(Constants.VISIBLE::descendants),
    DEEP(Styx::descendants);

    private final Function<FileEntry, Stream<FileEntry>> toStream;

    Recursion(final Function<FileEntry, Stream<FileEntry>> toStream) {
        this.toStream = toStream;
    }

    public final Stream<FileEntry> stream(final FileEntry entry) {
        return toStream.apply(entry);
    }

    private static class Constants {
        private static final Predicate<FileEntry> INVISIBLE = entry -> entry.name().startsWith(".");
        private static final Styx.Streamer VISIBLE = Styx.streamer(Styx.Options.DEFAULT.skip(INVISIBLE));
    }
}
