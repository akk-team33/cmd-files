package de.team33.patterns.io.gamma.json;

enum JsonToken implements Scanner.Token {

    NULL(TokenType.LITERAL),
    BOOLEAN(TokenType.LITERAL),
    NUMBER(TokenType.LITERAL),
    STRING(TokenType.LITERAL),
    LEFT_BRACE(TokenType.STRUCTURAL),
    RIGHT_BRACE(TokenType.STRUCTURAL),
    LEFT_BRACKET(TokenType.STRUCTURAL),
    RIGHT_BRACKET(TokenType.STRUCTURAL),
    COLON(TokenType.STRUCTURAL),
    COMMA(TokenType.STRUCTURAL),
    WHITESPACE(TokenType.IGNORABLE);

    private final TokenType type;

    JsonToken(final TokenType type) {
        this.type = type;
    }

    @Override
    public final TokenType type() {
        return type;
    }
}
