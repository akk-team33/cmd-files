package de.team33.cmd.files.main.job;

import de.team33.cmd.files.main.common.Context;
import de.team33.patterns.io.deimos.TextIO;

import java.util.List;

public class Balancer implements Runnable {

    private final Context context;
    private final String cmdName;
    private final String args;
    private final String format;

    public Balancer(final Context context, final String cmdName, final List<String> args) {
        this.context = context;
        this.cmdName = cmdName;
        this.args = String.join(" ", args);
        this.format = TextIO.read(Balancer.class, "Balancer.txt");
    }

    public static Runnable job(final Context context, final List<String> args) {
        assert 1 < args.size();
        assert Regular.BALANCE.name().equalsIgnoreCase(args.get(1));
        // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
        return job(context, args.get(0), args);
    }

    private static Runnable job(final Context context, final String cmdName, final List<String> args) {
        if (5 > args.size()) {
            return new Balancer(context, cmdName, args);
        } else {
            throw new UnsupportedOperationException("not yet implemented");
        }
    }

    @Override
    public final void run() {
        context.printf(format, args, cmdName);
    }
}
