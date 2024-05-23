package de.team33.cmd.files.main.job;

import de.team33.cmd.files.main.common.Context;

import java.util.List;
import java.util.stream.Stream;

class InfoJob implements Runnable {

    private final Context context;
    private final String cmdLine;
    private final String cmdName;
    private final String format;
    private final Object[] moreArgs;

    InfoJob(final Context context, final List<String> args, final String format, final Object ... moreArgs) {
        this.context = context;
        this.cmdLine = String.join(" ", args);
        this.cmdName = args.get(0);
        this.format = format;
        this.moreArgs = moreArgs;
    }

    @Override
    public final void run() {
        context.printf(format, Stream.concat(Stream.of(cmdLine, cmdName), Stream.of(moreArgs)).toArray());
    }
}
