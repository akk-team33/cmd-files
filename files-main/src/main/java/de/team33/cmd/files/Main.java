package de.team33.cmd.files;

import de.team33.cmd.files.common.CoreCondition;
import de.team33.cmd.files.job.Regular;
import de.team33.cmd.files.common.Output;
import de.team33.cmd.files.common.RequestException;

public class Main {

    public static void main(final String... args) {
        job(CoreCondition.of(Output.SYSTEM, args)).run();
    }

    private static Runnable job(final CoreCondition condition) {
        try {
            return Regular.job(condition);
        } catch (final RequestException e) {
            return () -> condition.out().printHelp(e.getMessage());
        }
    }
}
