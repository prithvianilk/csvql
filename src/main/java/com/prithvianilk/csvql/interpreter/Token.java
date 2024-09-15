package com.prithvianilk.csvql.interpreter;

public sealed interface Token permits Token.AllColumns, Token.And, Token.Comma, Token.Eof, Token.Equals, Token.From, Token.Identifier, Token.Minus, Token.Plus, Token.Select, Token.Where {

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

    record Where() implements Token {
    }

    record Equals() implements Token {
    }

    record And() implements Token {
    }

    record Plus() implements Token {
    }

    record Minus() implements Token {
    }
}

