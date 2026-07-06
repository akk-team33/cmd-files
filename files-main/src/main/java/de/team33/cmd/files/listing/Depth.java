package de.team33.cmd.files.listing;

import de.team33.patterns.directories.iocaste.DirectoryLister;
import de.team33.patterns.directories.iocaste.DirectoryStreamer;
import de.team33.patterns.directories.iocaste.FileEntry;
import de.team33.patterns.directories.iocaste.PathOrder;

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

public enum Depth {

    FLAT(entry -> Constants.LISTER.list(entry).stream()),
    DEEP_VISIBLE(Constants.VISIBLE::stream),
    DEEP(Constants.STREAMER::stream);

    private final Function<FileEntry, Stream<FileEntry>> toStream;

    Depth(final Function<FileEntry, Stream<FileEntry>> toStream) {
        this.toStream = toStream;
    }

    public final Stream<FileEntry> stream(final FileEntry entry) {
        return toStream.apply(entry);
    }

    private static class Constants {
        private static final Predicate<FileEntry> INVISIBLE = entry -> entry.name().startsWith(".");
        private static final DirectoryLister LISTER = DirectoryLister.DEFAULT.pathOrder(PathOrder.BY_NAME);
        private static final DirectoryStreamer STREAMER = DirectoryStreamer.basedOn(LISTER).start(1);
        private static final DirectoryStreamer VISIBLE = STREAMER.skip(INVISIBLE);
    }
}
