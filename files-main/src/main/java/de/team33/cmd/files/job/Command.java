package de.team33.cmd.files.job;

import de.team33.cmd.files.common.RequestException;
import de.team33.patterns.enums.pan.Values;
import de.team33.patterns.exceptional.dione.XFunction;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static de.team33.cmd.files.job.Util.cmdLine;
import static de.team33.cmd.files.job.Util.cmdName;

public enum Command {

    ABOUT(About::job, About.EXCERPT),
    CONFIG(Configurator::job, Configurator.EXCERPT),
    CLEAN(Cleaning::job, Cleaning.EXCERPT),
    CMP(Comparing::job, Comparing.EXCERPT),
    COPY(Copying::job, Copying.EXCERPT),
    DCOPY(DirCopying::job, DirCopying.EXCERPT),
    DELETE(Deletion::job, Deletion.EXCERPT),
    LIST(Lister::job, Lister.EXCERPT),
    LSD(DirLister::job, DirLister.EXCERPT),
    MOVE(Moving::job, Moving.EXCERPT),
    REGISTER(Registrar::job, Registrar.EXCERPT);

    private static final Values<Command> VALUES = Values.of(Command.class);

    private final XFunction<Context, Runnable, RequestException> toJob;
    private final String excerpt;

    Command(final XFunction<Context, Runnable, RequestException> toJob, final String excerpt) {
        this.toJob = toJob;
        this.excerpt = excerpt;
    }

    public static String excerpts() {
        final int maxLength = VALUES.mapAll(value -> value.name().length())
                                    .reduce(0, Math::max);
        final String format = String.format("    %%-%ds : %%s%%n", maxLength);
        return VALUES.mapAll(regular -> String.format(format, regular.name(), regular.excerpt))
                     .collect(Collectors.joining())
                     .trim();
    }

    private static RequestException newBadArgsException(final List<String> args) {
        return RequestException.format(Command.class, "BadArgs.txt")
                               .apply(cmdLine(args), cmdName(args), excerpts());
    }

    private static Optional<Command> ofAmbiguous(final List<String> args) {
        if (1 < args.size()) {
            return VALUES.findAny(regular -> regular.name().equalsIgnoreCase(args.get(1)));
        } else {
            return Optional.empty();
        }
    }

    private static Runnable ofCharged(final Context context) throws RequestException {
        return ofAmbiguous(context.args()).orElseThrow(() -> newBadArgsException(context.args()))
                                          .runnable(context);
    }

    public static Runnable job(final Context context) throws RequestException {
        if (context.args().isEmpty()) {
            throw RequestException.read(Command.class, "NoArgs.txt");
        } else {
            return ofCharged(context);
        }
    }

    private Runnable runnable(final Context context) throws RequestException {
        return toJob.apply(context);
    }
}
