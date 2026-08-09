package de.team33.cmd.files.job;

import de.team33.cmd.files.common.Output;
import de.team33.cmd.files.common.RequestException;
import de.team33.patterns.config.alpha.ConfigLevel;
import de.team33.patterns.io.thalassa.RecordIO;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static de.team33.cmd.files.job.Util.cmdLine;
import static de.team33.cmd.files.job.Util.cmdName;

class Configurator {

    static final String EXCERPT = "Handle the configuration of this cli.";

    static Runnable job(final Context context) throws RequestException {
        return job(context.out(), context.config(), context.args());
    }

    private static Runnable job(final Output out,
                                final Config config,
                                final List<String> args) throws RequestException {
        final RuntimeException[] problem = {null};
        try {
            if ((args.size() == 3) && "show".equalsIgnoreCase(args.get(2))) {
                return () -> show(out, config);
            } else if ((args.size() == 4) && "show".equalsIgnoreCase(args.get(2))) {
                final var level = ConfigLevel.valueOf(args.get(3).toUpperCase());
                return () -> show(out, level);
            } else if (args.size() == 5 && "take".equalsIgnoreCase(args.get(2))) {
                final var path = Path.of(args.get(3));
                final var level = ConfigLevel.valueOf(args.get(4).toUpperCase());
                return () -> take(out, path, level);
            }
        } catch (final RuntimeException e) {
            problem[0] = e;
        }
        final RequestException ex = RequestException.format(Configurator.class)
                                                    .apply(cmdLine(args), cmdName(args));
        throw Optional.ofNullable(problem[0])
                      .map(ex::causedBy)
                      .orElse(ex);
    }

    private static void take(final Output out, final Path path, final ConfigLevel level) {
        out.printf("Taking %s at level %s ... ", path, level);
        RecordIO.read(Config.class, path)
                .write(level);
        out.printf("ok%n%n");
    }

    private static void show(final Output out, final ConfigLevel level) {
        final var config = Config.read(level);
        out.printf("%s%n", config);
    }

    private static void show(final Output out, final Config config) {
        out.printf("%s%n", config);
    }
}
