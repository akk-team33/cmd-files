package de.team33.cmd.files.job;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

final class Util {

    private Util() {
    }

    static String cmdLine(final List<String> args) {
        return String.join(" ", args);
    }

    static String cmdName(final List<String> args) {
        final Optional<Path> cmdPath = args.stream().findFirst().map(Path::of);
        return cmdPath.filter(Path::isAbsolute)
                      .map(Path::getFileName)
                      .map(Path::toString)
                      .orElseGet(() -> cmdPath.map(Path::toString)
                                              .orElse("[n/a]"));
    }
}
