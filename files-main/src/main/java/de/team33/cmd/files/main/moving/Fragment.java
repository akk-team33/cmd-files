package de.team33.cmd.files.main.moving;

import java.util.function.Function;

class Fragment {

    private final Function<FileInfo, String> method;

    private Fragment(final Function<FileInfo, String> method) {
        this.method = method;
    }

    static Fragment parse(final String token) {
        return new Fragment(Rule.map(token));
    }

    final String map(final FileInfo fileInfo) {
        return method.apply(fileInfo);
    }
}