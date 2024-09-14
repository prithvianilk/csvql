package org.example.interpreter.ast;

import org.example.interpreter.Token;

import java.util.List;

public record Query(
        List<Token> columnNameTokens,
        Token.Identifier csvFileName) {
}
