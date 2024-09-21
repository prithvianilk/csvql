package com.prithvianilk.csvql.interpreter.ast;

public sealed interface Expression permits Expression.Simple, Expression.Composite {
    record Simple(ValueType valueType) implements Expression {
    }

    record Composite(
            ValueType valueType,
            Operation operation,
            Expression nextExpression) implements Expression {
    }

    enum Operation {
        PLUS, MINUS;
    }

    sealed interface ValueType permits ValueType.Int, ValueType.Str, ValueType.ColumnName {
        record Int(int value) implements ValueType {
        }

        record Str(String value) implements ValueType {
        }

        record ColumnName(String value) implements ValueType {
        }
    }
}
