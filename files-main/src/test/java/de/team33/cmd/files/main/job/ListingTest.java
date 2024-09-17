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
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ListingTest {

    private static final Path TEST_PATH = Path.of("target", "testing", ListingTest.class.getSimpleName());

    private final String uuid = UUID.randomUUID().toString();
    private final Path listingPath = TEST_PATH.resolve(uuid).resolve("listing");

    private String expected(final String rsrcName) {
        return String.format("%s%n", TextIO.read(getClass(), rsrcName));
    }

    @Test
    final void run_N() throws IOException, RequestException {
        ZipIO.unzip(ListingTest.class, "Keeping.zip", listingPath);

        final String result = Redirected.outputOf(() -> Listing.job(Output.SYSTEM,
                                                                    Arrays.asList("files", "list", "n",
                                                                                  listingPath.toString(),
                                                                                  "jpg,jpe,jpeg"))
                                                               .run());
        //printf("%s%n", result);

        assertEquals(expected("ListingRunN.txt"), result);
    }

    @Test
    final void run_N_REG() throws IOException, RequestException {
        ZipIO.unzip(ListingTest.class, "Keeping.zip", listingPath);

        final String result = Redirected.outputOf(() -> Listing.job(Output.SYSTEM,
                                                                    Arrays.asList("files", "list", "n",
                                                                                  listingPath.toString(),
                                                                                  ":reg"))
                                                               .run());
        //printf("%s%n", result);

        assertEquals(expected("ListingRunNREG.txt"), result);
    }

    @Test
    final void run_X() throws IOException, RequestException {
        ZipIO.unzip(ListingTest.class, "Keeping.zip", listingPath);

        final String result = Redirected.outputOf(() -> Listing.job(Output.SYSTEM,
                                                                    Arrays.asList("files", "list", "x",
                                                                                  listingPath.toString(),
                                                                                  "jpg,jpe,jpeg"))
                                                               .run());
        //printf("%s%n", result);

        assertEquals(expected("ListingRunX.txt"), result);
    }

    @Test
    final void run_NX() throws IOException, RequestException {
        ZipIO.unzip(ListingTest.class, "Keeping.zip", listingPath);

        final String result = Redirected.outputOf(() -> Listing.job(Output.SYSTEM,
                                                                    Arrays.asList("files", "list", "nx",
                                                                                  listingPath.toString(),
                                                                                  "jpg,jpe,jpeg"))
                                                               .run());
        //printf("%s%n", result);

        assertEquals(expected("ListingRunNX.txt"), result);
    }

    @Test
    final void run_X_REG() throws IOException, RequestException {
        ZipIO.unzip(ListingTest.class, "Keeping.zip", listingPath);

        final String result = Redirected.outputOf(() -> Listing.job(Output.SYSTEM,
                                                                    Arrays.asList("files", "list", "x",
                                                                                  listingPath.toString(),
                                                                                  ":reg"))
                                                               .run());
        //printf("%s%n", result);

        assertEquals(expected("ListingRunXREG.txt"), result);
    }

    @Test
    final void run_NX_REG() throws IOException, RequestException {
        ZipIO.unzip(ListingTest.class, "Keeping.zip", listingPath);

        final String result = Redirected.outputOf(() -> Listing.job(Output.SYSTEM,
                                                                    Arrays.asList("files", "list", "nx",
                                                                                  listingPath.toString(),
                                                                                  ":reg"))
                                                               .run());
        //printf("%s%n", result);

        assertEquals(expected("ListingRunNXREG.txt"), result);
    }
}
