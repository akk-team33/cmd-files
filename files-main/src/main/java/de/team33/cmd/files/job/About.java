package de.team33.cmd.files.job;

import de.team33.cmd.files.common.Condition;
import de.team33.patterns.io.deimos.TextIO;

class About {

    static final String EXCERPT = "Get basic info about this application.";

    static Runnable job(final Condition condition) {
        return () -> condition.out()
                              .printLines(TextIO.read(About.class, "About.txt")
                                                .formatted(condition.cmdLine()));
    }
}
