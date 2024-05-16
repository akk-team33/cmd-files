package de.team33.cmd.files.main;

import de.team33.cmd.files.main.job.BadArgs;
import de.team33.cmd.files.main.job.NoArgs;
import de.team33.cmd.files.main.job.Regular;
import de.team33.cmd.files.main.common.Context;
import de.team33.patterns.enums.alpha.EnumTool;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

public enum Main implements Context {

    NO_ARGS(NoArgs::test, NoArgs::job),
    REGULAR(Regular::test, Regular::job),
    BAD_ARGS(BadArgs::test, BadArgs::job);

    private static final EnumTool<Main> TOOL = EnumTool.of(Main.class)
                                                       .fallback(BAD_ARGS);

    private final Predicate<List<String>> filter;
    private final BiFunction<Context, List<String>, Runnable> toJob;

    Main(final Predicate<List<String>> filter, final BiFunction<Context, List<String>, Runnable> toJob) {
        this.filter = filter;
        this.toJob = toJob;
    }

    public static void main(final String... args) {
        job(Arrays.asList(args)).run();
    }

    private static Runnable job(final List<String> args) {
        return TOOL.mapFirst(toFilter(args), toJob(args));
    }

    private static Predicate<Main> toFilter(final List<String> args) {
        return item -> item.filter.test(args);
    }

    private static Function<Main, Runnable> toJob(final List<String> args) {
        return item -> item.toJob.apply(item, args);
    }
}
