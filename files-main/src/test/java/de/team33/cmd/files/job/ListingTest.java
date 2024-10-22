package de.team33.cmd.files.job;

import de.team33.cmd.files.common.Output;
import de.team33.cmd.files.common.RequestException;
import de.team33.cmd.files.testing.ModifyingTestBase;
import de.team33.patterns.io.deimos.TextIO;
import de.team33.testing.stdio.ersa.Redirected;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static de.team33.cmd.files.job.Util.condition;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ListingTest extends ModifyingTestBase {

    ListingTest() {
        super(ABSOLUTE, InitMode.FILL_BOTH);
    }

    @Test
    final void list_simple() throws IOException, RequestException {
        final Runnable job = Listing.job(condition(Output.SYSTEM,
                                                   "files", "list", leftPath().toString(), rightPath().toString()));
        final String result = Redirected.outputOf(job::run)
                                        .replace(testPath().toString(), "[PATH]");
        assertEquals(TextIO.read(ListingTest.class, "ListingTest-list_simple.txt"), result);
    }
}
