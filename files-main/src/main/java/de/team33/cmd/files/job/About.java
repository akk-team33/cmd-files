package de.team33.cmd.files.job;

import de.team33.cmd.files.common.Output;
import de.team33.patterns.io.deimos.TextIO;

import java.util.List;

import static de.team33.cmd.files.job.Util.cmdLine;

class About {

    static final String EXCERPT = "Get basic info about this application.";

    static Runnable job(final Output out, final List<String> args) {
        return () -> out.printLines(String.format(TextIO.read(About.class, "About.txt"), cmdLine(args)));
    }
}
