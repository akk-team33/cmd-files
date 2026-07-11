package de.team33.cmd.files;

import de.team33.cmd.files.common.Output;
import de.team33.cmd.files.common.RequestException;
import de.team33.cmd.files.job.Command;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class Main {

    public static void main(final String... args) {
        job(Arrays.asList(args)).run();
    }

    private static Runnable job(final List<String> args) {
        final Output out = Output.SYSTEM;
        try {
            return Command.job(out, args);
        } catch (final RequestException e) {
            return () -> {
                Optional.ofNullable(e.getCause())
                        .ifPresent(Throwable::printStackTrace);
                out.printHelp(e.getMessage());
            };
        }
    }
}
