package de.team33.patterns.io.gamma;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Path;

public class TextResource {

    private final Resource.Input<String> input;
    private final Resource.Output<String> output;

    private TextResource(final Resource resource) {
        this.input = resource.reading(this::reading);
        this.output = resource.writing(this::writing);
    }

    public static TextResource by(final Resource resource) {
        return new TextResource(resource);
    }

    public static TextResource by(final Path path) {
        return new TextResource(Resource.by(path));
    }

    public static TextResource by(final Class<?> refClass, final String name) {
        return new TextResource(Resource.by(refClass, name));
    }

    private void writing(final BufferedWriter out, final String text) throws IOException {
        out.write(text);
    }

    private String reading(final BufferedReader in) throws IOException {
        try (final StringWriter out = new StringWriter()) {
            in.transferTo(out);
            return out.toString();
        }
    }

    public final String read() throws IOException {
        return input.read();
    }

    public final void write(final String text) throws IOException {
        output.write(text);
    }
}
