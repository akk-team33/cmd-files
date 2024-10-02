package de.team33.cmd.files.main.moving;

import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;

public class Resolver {

    private static final Pattern PATTERN = Pattern.compile("[\\\\/]");

    private final List<Segment> segments;

    private Resolver(final List<Segment> segments) {
        this.segments = segments;
    }

    public static Resolver parse(final String rule) {
        return new Resolver(PATTERN.splitAsStream(rule)
                                   .map(Segment::parse)
                                   .toList());
    }

    public Path resolve(final Path cwd, final Path path) {
        final FileInfo fileInfo = new FileInfo(cwd, path);
        return segments.stream()
                       .map(segment -> segment.map(fileInfo))
                       .map(Path::of)
                       .reduce(Path::resolve)
                       .orElseThrow(() -> new ResolverException("Resolver: no segments specified."));
    }
}