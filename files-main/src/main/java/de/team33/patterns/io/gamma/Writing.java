package de.team33.patterns.io.gamma;

import de.team33.patterns.exceptional.dione.XBiConsumer;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;

@FunctionalInterface
public interface Writing {

    static Writing by(final Writing writing) {
        return writing;
    }

    static Writing by(final Path path, final OpenOption... options) {
        return by(() -> Files.newOutputStream(path, options));
    }

    OutputStream newOutputStream() throws IOException;

    default <T> Output<T> output(final XBiConsumer<? super OutputStream, ? super T, ? extends IOException> method) {
        return origin -> {
            try (final OutputStream out = newOutputStream()) {
                method.accept(out, origin);
            }
        };
    }

    default <T> Output<T> output(final Charset charset,
                                 final XBiConsumer<? super BufferedWriter, ? super T, ? extends IOException> method) {
        return output(Util.outputMethod(method, charset));
    }
}
