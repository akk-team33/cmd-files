package de.team33.cmd.files.common;

public class Counter {

    private long value = 0;

    public final long value() {
        return value;
    }

    public final void increment() {
        value += 1;
    }

    public final void reset() {
        value = 0;
    }
}
