package de.team33.cmd.files.stats.publics;

import de.team33.cmd.files.stats.Aggregat;
import de.team33.cmd.files.stats.Aspect;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AggregatTest {

    private static final Aspect<UUID> ASPECT = new Aspect<>() {

        private int value;

        @Override
        public void reset() {
            value = 0;
        }

        @Override
        public void increment(UUID item) {
            value += 1;
        }

        @Override
        public Stream<String> lines() {
            return Stream.of("%,10d".formatted(value));
        }
    };

    @Test
    final void lines() {
        final Aggregat<UUID> aggregat = Aggregat.head("name", ASPECT);
        Stream.generate(UUID::randomUUID)
              .limit(278)
              .forEach(uuid -> aggregat.increment("name", uuid));

        final List<String> result = aggregat.lines().toList();

        assertEquals(List.of("       278"), result);
    }

    @Test
    final void reset() {
        final Aggregat<UUID> aggregat = Aggregat.head("name", ASPECT);
        Stream.generate(UUID::randomUUID)
              .limit(278)
              .forEach(uuid -> aggregat.increment("name", uuid));

        aggregat.reset();
        final List<String> result = aggregat.lines().toList();

        assertEquals(List.of("         0"), result);
    }
}
