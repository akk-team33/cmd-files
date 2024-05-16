package de.team33.cmd.files.main.job;

import de.team33.cmd.files.main.common.Context;
import de.team33.patterns.io.deimos.TextIO;

import java.util.List;

public class NoArgs implements Runnable {

    private final Context context;
    private final String text;

    public NoArgs(final Context context) {
        this.context = context;
        this.text = TextIO.read(NoArgs.class, "NoArgs.txt");
    }

    public static boolean test(final List<String> args) {
        return args.isEmpty();
    }

    public static Runnable job(final Context context, final List<String> args) {
        assert test(args);
        // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
        return new NoArgs(context);
    }

    @Override
    public final void run() {
        context.printf("%s%n%n", text);
    }
}
