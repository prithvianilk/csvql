package com.prithvianilk.csvql.interpreter;

public sealed interface Token permits Token.AllColumns, Token.And, Token.Comma, Token.Count, Token.Eof, Token.Equals, Token.From, Token.Identifier, Token.LeftBracket, Token.Minus, Token.Plus, Token.RightBracket, Token.Select, Token.Sum, Token.Where {

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

    record Count() implements Token {
    }

    record Sum() implements Token {
    }

    record LeftBracket() implements Token {
    }

    record RightBracket() implements Token {
    }
}

