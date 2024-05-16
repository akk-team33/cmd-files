package de.team33.cmd.files.main.job;

import de.team33.cmd.files.main.common.Context;
import de.team33.patterns.io.deimos.TextIO;

import java.util.List;

public class BadArgs implements Runnable {

    private final Context context;
    private final List<String> args;
    private final String format;

    public BadArgs(final Context context, final List<String> args) {
        this.context = context;
        this.args = args;
        this.format = TextIO.read(BadArgs.class, "BadArgs.txt");
    }

    public static boolean test(final List<String> args) {
        return !args.isEmpty();
    }

    public static Runnable job(final Context context, final List<String> args) {
        assert test(args);
        // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
        return new BadArgs(context, args);
    }

    @Override
    public final void run() {
        final String cmdLine = String.join(" ", args);
        final String shellCmdName = args.get(0);
        context.printf(format, cmdLine, shellCmdName, Regular.excerpt());
    }
}
