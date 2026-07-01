package de.team33.cmd.files.listing;

import de.team33.patterns.files.iocaste.FileEntry;
import de.team33.patterns.files.iocaste.LinkHandling;

import java.util.function.Function;
import java.util.stream.Stream;

public enum Depth {

    FLAT(entry -> Constants.LISTER.list(entry).stream()),
    DEEP(entry -> Constants.STREAMER.stream(entry).skip(1));

    private final Function<FileEntry, Stream<FileEntry>> toStream;

    Depth(final Function<FileEntry, Stream<FileEntry>> toStream) {
        this.toStream = toStream;
    }

    public final Stream<FileEntry> stream(final FileEntry entry) {
        return toStream.apply(entry);
    }

    private static class Constants {
        private static final FileEntry.Lister LISTER = FileEntry.lister(LinkHandling.ORIGINAL);
        private static final FileEntry.Streamer STREAMER = FileEntry.streamer(LISTER);
    }
}
