package de.team33.cmd.files.main.job;

import de.team33.cmd.files.main.common.Output;
import de.team33.cmd.files.main.common.RequestException;
import de.team33.patterns.io.deimos.TextIO;
import de.team33.testing.io.hydra.ZipIO;
import de.team33.testing.stdio.ersa.Redirected;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.function.Predicate.not;
import static org.junit.jupiter.api.Assertions.assertEquals;

class FinderTest {

    private static final Path TEST_PATH = Path.of("target", "testing", FinderTest.class.getSimpleName());

    private final String uuid = UUID.randomUUID().toString();
    private final Path finderPath = TEST_PATH.resolve(uuid).resolve("finder");

    private Set<String> expected(final String rsrcName) {
        return TextIO.read(getClass(), rsrcName)
                     .lines()
                     .map(finderPath::resolve)
                     .map(Path::toAbsolutePath)
                     .map(Path::normalize)
                     .map(Path::toString)
                     .collect(Collectors.toSet());
    }

    @Test
    final void run_All() throws IOException, RequestException {
        ZipIO.unzip(FinderTest.class, "Keeping.zip", finderPath);

        final Set<String> result = Redirected.outputOf(() -> Finder.job(Output.SYSTEM,
                                                                        Arrays.asList("files", "find", ".*",
                                                                                      finderPath.toString()))
                                                                   .run())
                                             .lines()
                                             .filter(not(String::isBlank))
                                             .collect(Collectors.toCollection(TreeSet::new));
        // result.forEach(System.out::println);

        assertEquals(expected("Finder-run-All.txt"), result);
    }

    @Test
    final void run_DSC_0001() throws IOException, RequestException {
        ZipIO.unzip(FinderTest.class, "Keeping.zip", finderPath);

        final Set<String> result = Redirected.outputOf(() -> Finder.job(Output.SYSTEM,
                                                                        Arrays.asList("files", "find", ".*_0001.*",
                                                                                      finderPath.toString()))
                                                                   .run())
                                             .lines()
                                             .filter(not(String::isBlank))
                                             .collect(Collectors.toCollection(TreeSet::new));
        // result.forEach(System.out::println);

        assertEquals(expected("Finder-run-DSC_0001.txt"), result);
    }

    @Test
    final void run_TIFF() throws IOException, RequestException {
        ZipIO.unzip(FinderTest.class, "Keeping.zip", finderPath);

        final Set<String> result = Redirected.outputOf(() -> Finder.job(Output.SYSTEM,
                                                                        Arrays.asList("files", "find",
                                                                                      ".*\\.TIFF",
                                                                                      finderPath.toString()))
                                                                   .run())
                                             .lines()
                                             .filter(not(String::isBlank))
                                             .collect(Collectors.toCollection(TreeSet::new));
        // result.forEach(System.out::println);

        assertEquals(expected("Finder-run-TIFF.txt"), result);
    }
}
