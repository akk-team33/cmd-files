package de.team33.cmd.files.main.job;

import de.team33.cmd.files.main.common.Output;
import de.team33.cmd.files.main.common.RequestException;

import java.util.List;

import static de.team33.cmd.files.main.job.Util.cmdLine;
import static de.team33.cmd.files.main.job.Util.cmdName;

class Moving implements Runnable {

    static final String EXCERPT = "Relocate regular files located in a given directory.";

    static Moving job(final Output out, final List<String> args) throws RequestException {
        assert 1 < args.size();
        assert Regular.MOVE.name().equalsIgnoreCase(args.get(1));
        // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
        // TODO: normal creation
        throw RequestException.format(Moving.class, "Moving.txt", cmdLine(args), cmdName(args));
    }

    @Override
    public void run() {
        throw new UnsupportedOperationException("not yet implemented");
    }
}
