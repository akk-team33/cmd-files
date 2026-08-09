package de.team33.cmd.files.job;

import de.team33.patterns.io.thalassa.TextIO;

import static de.team33.cmd.files.job.Util.cmdLine;

class About {

    static final String EXCERPT = "Get basic info about this application.";

    static Runnable job(final Context context) {
        return () -> context.out().printLines(TextIO.read(About.class, "About.txt")
                                                    .formatted(cmdLine(context.args())));
    }
}
