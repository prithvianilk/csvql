package com.prithvianilk.csvql.interpreter.ast;

public record Conditional(Expression lhs, Predicate predicate, Expression rhs) {
    public enum Predicate {
        EQUALS, LESSER_THAN, GREATER_THAN, LESSER_THAN_EQUALS, GREATER_THAN_EQUALS
    }
}
