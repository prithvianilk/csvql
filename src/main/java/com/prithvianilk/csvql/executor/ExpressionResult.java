package com.prithvianilk.csvql.executor;

public sealed interface ExpressionResult permits ExpressionResult.Int, ExpressionResult.Str {
    record Int(int value) implements ExpressionResult {
    }

    record Str(String value) implements ExpressionResult {
    }
}
