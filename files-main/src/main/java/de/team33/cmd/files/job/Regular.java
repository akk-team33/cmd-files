package de.team33.cmd.files.job;

import de.team33.cmd.files.common.Condition;
import de.team33.cmd.files.common.CoreCondition;
import de.team33.cmd.files.common.Output;
import de.team33.cmd.files.common.RequestException;
import de.team33.patterns.enums.alpha.Values;
import de.team33.patterns.exceptional.dione.XBiFunction;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

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

    private static Supplier<RequestException> newBadArgsException(final CoreCondition condition) {
        return () -> RequestException.format(Regular.class, "BadArgs.txt",
                                             condition.cmdLine(), condition.cmdName(), excerpts());
    }

    private static Runnable ofCharged(final CoreCondition preCondition) throws RequestException {
        final Condition condition = Condition.of(preCondition)
                                             .orElseThrow(newBadArgsException(preCondition));
        return findAny(condition).runnable(condition);
    }

    private static Regular findAny(final Condition condition) throws RequestException {
        return VALUES.findAny(value -> value.name().equalsIgnoreCase(condition.subCmdName()))
                     .orElseThrow(newBadArgsException(condition));
    }

    public static Runnable job(final CoreCondition condition) throws RequestException {
        if (condition.args().isEmpty()) {
            throw RequestException.read(Regular.class, "NoArgs.txt");
        } else {
            return ofCharged(condition);
        }
    }

    private Runnable runnable(final Condition condition) throws RequestException {
        return toJob.apply(condition.out(), condition.args());
    }
}
