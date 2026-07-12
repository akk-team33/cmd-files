package de.team33.patterns.config.alpha;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.file.Path;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class ConfigResourceTest extends Supply {

    private static final Path TEST_PATH =
            Path.of("target", "testing", ConfigResourceTest.class.getSimpleName());

    static Stream<ByCase> byCases() {
        return Stream.generate(() -> UUID.randomUUID().toString())
                     .limit(5)
                     .map("%s.json"::formatted)
                     .map(TEST_PATH::resolve)
                     .map(path -> new ByCase(path, ConfigResource.by(path)));
    }

    public static Stream<RoundTripCase> roundTripCases() {
        return byCases().map(byCase -> new RoundTripCase(byCase.expected));
    }

    @ParameterizedTest
    @MethodSource("byCases")
    final void by(final ByCase given) {
        final ConfigResource result = ConfigResource.by(given.path);
        assertNotNull(result);
        assertSame(given.expected, result);
    }

    @ParameterizedTest
    @MethodSource("roundTripCases")
    final void roundTrip(final RoundTripCase given) {
        final var original = anySampleConfig();
        given.resource.write(original);

        final var result = given.resource.read(SampleConfig.class);
        assertEquals(original, result);
    }

    record ByCase(Path path, ConfigResource expected) {
    }

    record RoundTripCase(ConfigResource resource) {
    }
}