package com.prithvianilk.csvql.interpreter.ast;

import com.prithvianilk.csvql.interpreter.Token;

import java.util.List;
import java.util.Optional;

public record Query(
        List<ColumnProjection> columnProjections,
        Token.Identifier csvFileName,
        List<Conditional> conditionals) {

    public List<String> getProjectableColumnNames() {
        return columnProjections
                .stream()
                .map(this::getColumnName)
                .flatMap(Optional::stream)
                .toList();
    }

    private Optional<String> getColumnName(ColumnProjection projection) {
        if (projection instanceof ColumnProjection.Column(Token.Identifier(String value))) {
            return Optional.of(value);
        }
        return Optional.empty();
    }
}
