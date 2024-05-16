package de.team33.cmd.template.main.job;

import de.team33.cmd.template.main.common.Context;
import de.team33.patterns.io.deimos.TextIO;

import java.util.List;

public class About implements Runnable {

    private final Context context;
    private final String format;
    private final String cmdLine;

    public About(final Context context, final List<String> args) {
        this.context = context;
        this.format = TextIO.read(About.class, "About.txt");
        this.cmdLine = String.join(" ", args);
    }

    public static Runnable job(final Context context, final List<String> args) {
        return new About(context, args);
    }

    @Override
    public final void run() {
        context.printf(format, cmdLine);
    }
}
