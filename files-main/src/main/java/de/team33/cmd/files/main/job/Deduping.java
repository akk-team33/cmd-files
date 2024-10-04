package de.team33.cmd.files.main.job;

import de.team33.cmd.files.main.common.Output;
import de.team33.cmd.files.main.common.RequestException;

import java.util.List;

import static de.team33.cmd.files.main.job.Util.cmdLine;
import static de.team33.cmd.files.main.job.Util.cmdName;

class Deduping implements Runnable {

    static final String EXCERPT = "Relocate duplicated files located in a given directory.";

    public static Deduping job(final Output out, final List<String> args) throws RequestException {
        assert 1 < args.size();
        assert Regular.DEDUPE.name().equalsIgnoreCase(args.get(1));
        // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
        throw RequestException.format(Moving.class, "Deduping.txt", cmdLine(args), cmdName(args));
    }

    @Override
    public final void run() {
        throw new UnsupportedOperationException("not yet implemented");
    }
}
