package de.team33.patterns.io.gamma;

import de.team33.patterns.exceptional.dione.XBiConsumer;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

@FunctionalInterface
public interface Writing<T> {

    OutputStream newOutputStream() throws IOException;

    default Output<T> output(final XBiConsumer<? super OutputStream, ? super T, ? extends IOException> method) {
        return origin -> {
            try (final OutputStream out = newOutputStream()) {
                method.accept(out, origin);
            }
        };
    }

    default Output<T> output(final XBiConsumer<? super BufferedWriter, ? super T, ? extends IOException> method,
                             final Charset charset) {
        return output(Util.outputMethod(method, charset));
    }
}
