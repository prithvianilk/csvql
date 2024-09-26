package com.prithvianilk.csvql.interpreter.ast;

public sealed interface Expression permits Expression.Simple, Expression.Composite {
    record Simple(Value value) implements Expression {
    }

    record Composite(
            Value value,
            Operation operation,
            Expression nextExpression) implements Expression {
    }

    enum Operation {
        PLUS, MINUS, MULTIPLY, DIVIDE
    }

    sealed interface Value permits Value.Int, Value.Str, Value.ColumnName {
        record Int(int value) implements Value {
        }

        record Str(String value) implements Value {
        }

        record ColumnName(String value) implements Value {
        }
    }
}
