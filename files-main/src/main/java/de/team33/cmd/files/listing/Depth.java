package de.team33.cmd.files.listing;

import de.team33.patterns.io.adrastea.Directory;
import de.team33.patterns.io.adrastea.FileEntry;
import de.team33.patterns.io.adrastea.LinkHandling;

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
        private static final Directory.Lister LISTER = Directory.lister(LinkHandling.ORIGINAL);
        private static final Directory.Streamer STREAMER = Directory.streamer(LISTER);
    }
}
