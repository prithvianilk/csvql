package com.prithvianilk.csvql.interpreter.ast;

import com.prithvianilk.csvql.interpreter.Token;

import java.util.List;
import java.util.Optional;

public record Query(
        List<Token> columnNameTokens,
        Token.Identifier csvFileName,
        List<Conditional> conditionals) {

    public List<String> getProjectableColumnNames() {
        return columnNameTokens
                .stream()
                .map(this::getColumnName)
                .flatMap(Optional::stream)
                .toList();
    }

    private Optional<String> getColumnName(Token token) {
        if (token instanceof Token.Identifier identifier) {
            return Optional.of(identifier.value());
        }
        return Optional.empty();
    }
}
