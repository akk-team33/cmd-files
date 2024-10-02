package de.team33.tools.io;

import java.nio.file.Path;

public interface FileHashing {

    String hash(Path filePath);

    String algorithm();

    int resultLength();
}