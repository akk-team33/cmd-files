package de.team33.cmd.files.main.common;

import de.team33.tools.io.FileHashing;
import de.team33.tools.io.LazyHashing;
import de.team33.tools.io.StrictHashing;

import java.nio.file.Path;

public class HashId {

    private static final String PREFIX = "#";
    private static final FileHashing CORE = new LazyHashing(PREFIX, StrictHashing.SHA_1);

    public static String valueOf(final Path path) {
        return PREFIX + CORE.hash(path);
    }

    public static String coreValueOf(final Path path) {
        return PREFIX + CORE.hash(path);
    }
}
