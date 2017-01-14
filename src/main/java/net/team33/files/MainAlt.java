package net.team33.files;

public final class MainAlt {

    public static void main(final String[] args) {
        final Command cmd = Command.from(args);
        cmd.job(args).run();
    }
}
