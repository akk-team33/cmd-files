package de.team33.cmd.files;

import de.team33.cmd.files.job.Regular;
import de.team33.cmd.files.common.Output;
import de.team33.cmd.files.common.RequestException;

import java.util.Arrays;
import java.util.List;

public class Main {

    public static void main(final String... args) {
        job(Arrays.asList(args)).run();
    }

    private static Runnable job(final List<String> args) {
        final Output out = Output.SYSTEM;
        try {
            return Regular.job(out, args);
        } catch (final RequestException e) {
            return () -> out.printHelp(e.getMessage());
        }
    }
}
