package de.team33.cmd.files.job;

import de.team33.cmd.files.common.RequestException;
import de.team33.cmd.files.testing.Buffer;
import de.team33.patterns.records.triton.Triton;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConfiguratorTest {

    @Test
    void job_show() throws RequestException {
        final Config expected = Config.read();
        final Buffer buffer = new Buffer();

        Configurator.job(Context.of(buffer, "files", "config", "show"))
                    .run();

        final String json = buffer.toString();
        final Config result = Triton.toRecord(Config.class, json);
        assertEquals(expected, result);
    }
}