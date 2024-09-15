package com.prithvianilk.csvql.interpreter.ast;

public record Conditional(Expression lhs, Predicate predicate, Expression rhs) {
    public enum Predicate {
        EQUALS
    }
}
