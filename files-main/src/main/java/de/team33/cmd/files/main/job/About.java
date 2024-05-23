package de.team33.cmd.files.main.job;

import de.team33.cmd.files.main.common.Context;
import de.team33.patterns.io.deimos.TextIO;

import java.util.List;

public class About extends InfoJob {

    public static final String EXCERPT = "Get basic info about this application.";

    public About(final Context context, final List<String> args) {
        super(context, args, TextIO.read(About.class, "About.txt"));
    }

    public static Runnable job(final Context context, final List<String> args) {
        return new About(context, args);
    }
}
