package de.team33.cmd.files.common;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public class CoreCondition {

    private final Output out;
    private final List<String> args;
    private final String cmdLine;
    private final String cmdName;

    private CoreCondition(final Output out, final List<String> args, final String cmdLine, final String cmdName) {
        this.out = out;
        this.args = args;
        this.cmdLine = cmdLine;
        this.cmdName = cmdName;
    }

    private CoreCondition(final Output out, final List<String> args) {
        this(out, args, cmdLine(args), cmdName(args));
    }

    protected CoreCondition(final CoreCondition origin) {
        this(origin.out, origin.args, origin.cmdLine, origin.cmdName);
    }

    public static CoreCondition of(final Output out, final String[] args) {
        return new CoreCondition(out, List.of(args));
    }

    private static String cmdLine(final List<String> args) {
        return String.join(" ", args);
    }

    private static String cmdName(final List<String> args) {
        final Optional<Path> cmdPath = args.stream().findFirst().map(Path::of);
        return cmdPath.filter(Path::isAbsolute)
                      .map(Path::getFileName)
                      .map(Path::toString)
                      .orElseGet(() -> cmdPath.map(Path::toString)
                                              .orElse("[n/a]"));
    }

    public final Output out() {
        return out;
    }

    public final List<String> args() {
        return args;
    }

    public final String cmdLine() {
        return cmdLine;
    }

    public final String cmdName() {
        return cmdName;
    }
}
