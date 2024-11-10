package de.team33.cmd.files.job;

import de.team33.cmd.files.common.Output;
import de.team33.cmd.files.common.RequestException;
import de.team33.patterns.enums.pan.Values;
import de.team33.patterns.exceptional.dione.XBiFunction;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static de.team33.cmd.files.job.Util.cmdLine;
import static de.team33.cmd.files.job.Util.cmdName;

public enum Regular {

    ABOUT(About::job, About.EXCERPT),
    CLEAN(Cleaning::job, Cleaning.EXCERPT),
    CMP(Comparing::job, Comparing.EXCERPT),
    COPY(Copying::job, Copying.EXCERPT),
    DCOPY(DirCopying::job, DirCopying.EXCERPT),
    DEDUPE(Deduping::job, Deduping.EXCERPT),
    DELETE(Deletion::job, Deletion.EXCERPT),
    FIND(Finder::job, Finder.EXCERPT),
    FINDIR(DirFinder::job, DirFinder.EXCERPT),
    LIST(Listing::job, Listing.EXCERPT),
    MOVE(Moving::job, Moving.EXCERPT),
    KEEP(Keeping::job, Keeping.EXCERPT);

    private static final Values<Regular> VALUES = Values.of(Regular.class);

    private final XBiFunction<Output, List<String>, Runnable, RequestException> toJob;
    private final String excerpt;

    Regular(final XBiFunction<Output, List<String>, Runnable, RequestException> toJob, final String excerpt) {
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
        return RequestException.format(Regular.class, "BadArgs.txt",
                                       cmdLine(args), cmdName(args), excerpts());
    }

    private static Optional<Regular> ofAmbiguous(final List<String> args) {
        if (1 < args.size()) {
            return VALUES.findAny(regular -> regular.name().equalsIgnoreCase(args.get(1)));
        } else {
            return Optional.empty();
        }
    }

    private static Runnable ofCharged(final Output out, final List<String> args) throws RequestException {
        return ofAmbiguous(args).orElseThrow(() -> newBadArgsException(args))
                                .runnable(out, args);
    }

    public static Runnable job(final Output out, final List<String> args) throws RequestException {
        if (args.isEmpty()) {
            throw RequestException.read(Regular.class, "NoArgs.txt");
        } else {
            return ofCharged(out, args);
        }
    }

    private Runnable runnable(final Output out, final List<String> args) throws RequestException {
        return toJob.apply(out, args);
    }
}
