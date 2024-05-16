package de.team33.patterns.enums.alpha.publics;

import de.team33.cmd.template.main.Main;
import de.team33.patterns.enums.alpha.EnumTool;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class EnumToolTest {

    @Test
    final void stream() {
        final List<Main> expected = Arrays.asList(Main.values());
        final List<Main> result = EnumTool.of(Main.class)
                                          .stream()
                                          .collect(Collectors.toList());
        assertEquals(expected, result);
    }

    @Test
    void findAll() {
        final Set<Main> expected = EnumSet.of(Main.BAD_ARGS, Main.NO_ARGS);
        final Set<Main> result = EnumTool.of(Main.class)
                                         .findAll(expected::contains)
                                         .collect(Collectors.toSet());
        assertEquals(expected, result);
    }

    @ParameterizedTest
    @EnumSource
    void findAny(final Main expected) {
        final Main result = EnumTool.of(Main.class)
                                    .findAny(expected::equals);
        assertEquals(expected, result);
    }

    @ParameterizedTest
    @EnumSource
    void findFirst(final Main expected) {
        final Main result = EnumTool.of(Main.class)
                                    .fallback(() -> null)
                                    .findFirst(expected::equals);
        assertEquals(expected, result);
    }

    @ParameterizedTest
    @EnumSource
    void mapAny(final Main expected) {
        final String result = EnumTool.of(Main.class)
                                      .fallback((Main)null)
                                      .mapAny(expected::equals, Main::name);
        assertEquals(expected.name(), result);
    }

    @ParameterizedTest
    @EnumSource
    void mapFirst(final Main expected) {
        final String result = EnumTool.of(Main.class)
                                      .failing(IllegalArgumentException::new)
                                      .mapFirst(expected::equals, Main::name);
        assertEquals(expected.name(), result);
    }
}
