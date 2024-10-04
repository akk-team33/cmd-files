package de.team33.cmd.files.main.moving;

import de.team33.tools.io.FileHashing;
import de.team33.tools.io.LazyHashing;
import de.team33.tools.io.StrictHashing;

import java.nio.file.Path;

class Hash {

    private static final String PREFIX = "#";
    private static final FileHashing CORE = new LazyHashing(PREFIX, StrictHashing.SHA_1);

    static String valueOf(final Path path) {
        return PREFIX + CORE.hash(path);
    }
}
