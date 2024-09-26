package de.team33.cmd.files.main.balancing;

import de.team33.patterns.io.alpha.FileEntry;
import de.team33.patterns.io.alpha.FilePolicy;

import java.nio.file.Path;

public class Relative {

    private final String path;
    private final FileEntry source;
    private final FileEntry target;
    private final State state;

    Relative(final String path, final Path srcRoot, final Path tgtRoot, final FilePolicy policy) {
        this.path = path;
        this.source = FileEntry.of(srcRoot.resolve(path), policy);
        this.target = FileEntry.of(tgtRoot.resolve(path), policy);
        this.state = State.of(source, target);
    }

    public final String path() {
        return path;
    }

    public final FileEntry source() {
        return source;
    }

    public final FileEntry target() {
        return target;
    }

    public final State state() {
        return state;
    }
}
