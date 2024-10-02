package de.team33.cmd.files.main.job;

import de.team33.cmd.files.main.common.Output;
import de.team33.cmd.files.main.common.RequestException;
import de.team33.cmd.files.main.moving.Resolver;

import java.nio.file.Path;
import java.util.List;

import static de.team33.cmd.files.main.job.Util.cmdLine;
import static de.team33.cmd.files.main.job.Util.cmdName;

class Moving implements Runnable {

    static final String EXCERPT = "Relocate regular files located in a given directory.";

    private Moving(final Output out, final Mode mode, final Path mainPath, final Resolver resolver) {
        throw new UnsupportedOperationException("not yet implemented");
    }

    static Moving job(final Output out, final List<String> args) throws RequestException {
        assert 1 < args.size();
        assert Regular.MOVE.name().equalsIgnoreCase(args.get(1));
        // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
        if (args.stream().skip(2).findFirst().map("-r"::equalsIgnoreCase).orElse(false)) {
            return job(out, Mode.DEEP, args, 3);
        } else {
            return job(out, Mode.FLAT, args, 2);
        }
    }

    private static Moving job(final Output out, final Mode mode,
                              final List<String> args, final int nextArg) throws RequestException {
        if ((nextArg + 2) == args.size()) {
            final Path mainPath = Path.of(args.get(nextArg));
            final Resolver resolver = Resolver.parse(args.get(nextArg + 1));
            return new Moving(out, mode, mainPath, resolver);
        }
        throw RequestException.format(Moving.class, "Moving.txt", cmdLine(args), cmdName(args));
    }

    @Override
    public void run() {
        throw new UnsupportedOperationException("not yet implemented");
    }

    enum Mode {
        FLAT,
        DEEP;
    }
}
