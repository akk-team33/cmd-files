package de.team33.patterns.io.gamma.json;

import java.util.Set;

class Scanner {

    private final Set<Token> tokens;
    private final Source source;

    Scanner() {
        this.tokens = Set.of();
        this.source = new Source("");
    }

    final boolean hasNext() {

    }

    interface Token {

        TokenType type();
    }

    record Value(Token token, String text, int position, int length) {
    }
}
