package com.prithvianilk.csvql.interpreter.ast;

import com.prithvianilk.csvql.interpreter.Token;

public sealed interface ColumnProjection permits ColumnProjection.Aggregation.Count, ColumnProjection.Aggregation.Sum, ColumnProjection.Column {
    record Column(Token token) implements ColumnProjection {
    }

    interface Aggregation {
        record Count(Aggregate countable) implements ColumnProjection {
            public static Count fromColumn(Token token) {
                return new Count(new Aggregate.Column(token));
            }
        }

        record Sum(Aggregate summable) implements ColumnProjection {
            public static ColumnProjection fromColumn(Token token) {
                return new Sum(new Aggregate.Column(token));
            }
        }
    }

    sealed interface Aggregate permits Aggregate.Column {
        record Column(Token token) implements Aggregate {
        }
    }

    default boolean isAggregation() {
        return switch (this) {
            case Aggregation.Count ignored -> true;
            case Aggregation.Sum ignored -> true;
            case Column ignored -> false;
        };
    }
}
