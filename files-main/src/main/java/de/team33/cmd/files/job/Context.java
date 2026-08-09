package de.team33.cmd.files.job;

import de.team33.cmd.files.common.Output;

import java.util.List;

public final class Context {

    private final Output output;
    private final List<String> args;
    private final Config config;

    private Context(Output output, List<String> args) {
        this.output = output;
        this.args = args;
        this.config = Config.read();
    }

    public static Context of(final Output output, final String... args) {
        return new Context(output, List.of(args));
    }

    public final Output out() {
        return output;
    }

    public final List<String> args() {
        return args;
    }

    final Config config() {
        return config;
    }
}
