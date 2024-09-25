package com.prithvianilk.csvql.executor;

public sealed interface AggregationResult permits AggregationResult.Int {
    record Int(int value) implements AggregationResult {
    }

    default String print() {
        return switch (this) {
            case Int(int value) -> String.format("%d", value);
        };
    }
}
