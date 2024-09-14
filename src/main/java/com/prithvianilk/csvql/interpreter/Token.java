package com.prithvianilk.csvql.interpreter;

public sealed interface Token permits
        Token.AllColumns,
        Token.Comma,
        Token.From,
        Token.Identifier,
        Token.Select,
        Token.Eof {

    record Select() implements Token {
    }

    record AllColumns() implements Token {
    }

    record Identifier(String value) implements Token {
    }

    record From() implements Token {
    }

    record Eof() implements Token {
    }

    record Comma() implements Token {
    }
}

