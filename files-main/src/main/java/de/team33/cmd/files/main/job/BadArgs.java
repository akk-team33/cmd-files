package de.team33.cmd.files.main.job;

import de.team33.cmd.files.main.common.Context;
import de.team33.patterns.io.deimos.TextIO;

import java.util.List;

public class BadArgs extends InfoJob {

    public BadArgs(final Context context, final List<String> args) {
        super(context, args, TextIO.read(BadArgs.class, "BadArgs.txt"), Regular.excerpt());
    }

    public static boolean test(final List<String> args) {
        return !args.isEmpty();
    }

    public static Runnable job(final Context context, final List<String> args) {
        assert test(args);
        // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
        return new BadArgs(context, args);
    }
}
