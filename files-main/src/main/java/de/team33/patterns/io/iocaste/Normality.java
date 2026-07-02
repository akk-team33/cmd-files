package de.team33.patterns.io.iocaste;

import java.nio.file.Path;
import java.util.function.Function;

interface Normality extends Function<Path, Path> {

    Normality UNKNOWN = path -> path.toAbsolutePath().normalize();
    Normality DEFINITE = path -> path;
}