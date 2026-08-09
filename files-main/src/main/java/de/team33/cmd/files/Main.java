package de.team33.cmd.files;

import de.team33.cmd.files.common.Output;
import de.team33.cmd.files.common.RequestException;
import de.team33.cmd.files.job.Command;
import de.team33.cmd.files.job.Context;

import java.util.Optional;

public class Main {

    public static void main(final String... args) {
        job(Context.of(Output.SYSTEM, args)).run();
    }

    private static Runnable job(final Context context) {
        final Output out = Output.SYSTEM;
        try {
            return Command.job(context);
        } catch (final RequestException e) {
            return () -> {
                Optional.ofNullable(e.getCause())
                        .ifPresent(Throwable::printStackTrace);
                out.printHelp(e.getMessage());
            };
        }
    }
}
