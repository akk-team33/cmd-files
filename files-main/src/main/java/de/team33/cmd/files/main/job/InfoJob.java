package de.team33.cmd.files.main.job;

import de.team33.cmd.files.main.common.Context;

import java.util.List;
import java.util.stream.Stream;

class InfoJob {

    private final Context context;
    private final String cmdLine;
    private final String cmdName;

    InfoJob(final Context context, final List<String> args) {
        this.context = context;
        this.cmdLine = String.join(" ", args);
        this.cmdName = args.get(0);
    }

    public final Runnable printf(final String format, final Object ... moreArgs) {
        final Object[] args = Stream.concat(Stream.of(cmdLine, cmdName), Stream.of(moreArgs)).toArray();
        return () -> context.printf(format, args);
    }
}
