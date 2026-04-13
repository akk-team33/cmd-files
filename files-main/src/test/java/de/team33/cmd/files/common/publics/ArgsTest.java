package de.team33.cmd.files.common.publics;

import de.team33.cmd.files.common.Args;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

class ArgsTest {

    private static final EnumSet<Key> KEYS = EnumSet.allOf(Key.class);
    private static final Function<List<String>, Args> ARGS = Args.stage(3, KEYS);

    @Test
    final void get_max() {
        final Args args = ARGS.apply(List.of("a", "b", "c", "x:1", "y:2", "z:3"));
        assertEquals("a", args.get(0));
        assertEquals("b", args.get(1));
        assertEquals("c", args.get(2));
        assertEquals("x:1", args.get(3));
        assertEquals("y:2", args.get(4));
        assertEquals("z:3", args.get(5));
        assertEquals("1", args.get(Key.X).orElse(null));
        assertEquals("2", args.get(Key.Y).orElse(null));
        assertEquals("3", args.get(Key.Z).orElse(null));
        assertEquals("[[a, b, c, x:1, y:2, z:3], {X=1, Y=2, Z=3}]", args.toString());
    }

    @Test
    final void get_min() {
        final Args args = ARGS.apply(List.of("a", "b", "c"));
        assertEquals("a", args.get(0));
        assertEquals("b", args.get(1));
        assertEquals("c", args.get(2));
        assertEquals("[[a, b, c], {}]", args.toString());
    }

    @Test
    final void get_more() {
        final Args args = ARGS.apply(List.of("a", "b", "c", "z:1", "x:2"));
        assertEquals("a", args.get(0));
        assertEquals("b", args.get(1));
        assertEquals("c", args.get(2));
        assertEquals("z:1", args.get(3));
        assertEquals("x:2", args.get(4));
        assertEquals("1", args.get(Key.Z).orElse(null));
        assertEquals("2", args.get(Key.X).orElse(null));
        assertEquals("[[a, b, c, z:1, x:2], {X=2, Z=1}]", args.toString());
    }

    @Test
    final void get_fail_too_few_args() {
        try {
            final Args args = ARGS.apply(List.of("a", "b"));
            fail(() -> "expected to fail - but was %s".formatted(args));
        } catch (final IllegalArgumentException e) {
            // as expected
            // e.printStackTrace();
            assertEquals("Expected 3 arguments - but was [a, b]", e.getMessage());
        }
    }

    @Test
    final void get_fail_key_value() {
        try {
            final Args args = ARGS.apply(List.of("a", "b", "c", "x:1", "d"));
            fail(() -> "expected to fail - but was %s".formatted(args));
        } catch (final IllegalArgumentException e) {
            // as expected
            // e.printStackTrace();
            assertEquals("Expected format KEY:VALUE - but arg was 'd'", e.getMessage());
        }
    }

    @Test
    final void get_fail_unknown_key() {
        try {
            final Args args = ARGS.apply(List.of("a", "b", "c", "x:1", "f:d"));
            fail(() -> "expected to fail - but was %s".formatted(args));
        } catch (final IllegalArgumentException e) {
            // as expected
            // e.printStackTrace();
            assertEquals("Expected one of [X, Y, Z] - but key was 'f'", e.getMessage());
        }
    }

    @Test
    final void get_fail_duplicate_key() {
        try {
            final Args args = ARGS.apply(List.of("a", "b", "c", "z:1", "x:d", "x:e"));
            fail(() -> "expected to fail - but was %s".formatted(args));
        } catch (final IllegalArgumentException e) {
            // as expected
            // e.printStackTrace();
            assertEquals("Expected unique VALUE - but was first: 'd', second: 'e'", e.getMessage());
        }
    }

    enum Key implements Args.Key {
        X,
        Y,
        Z
    }
}