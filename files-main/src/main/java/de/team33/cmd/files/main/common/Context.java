package de.team33.cmd.files.main.common;

public interface Context {

    default void printf(final String format, final Object... args) {
        System.out.printf(format, args);
    }
}
