package com.prithvianilk.csvql.interpreter.ast;

import com.prithvianilk.csvql.interpreter.Token;

import java.util.List;

public record Query(
        List<Token> columnNameTokens,
        Token.Identifier csvFileName) {
}
