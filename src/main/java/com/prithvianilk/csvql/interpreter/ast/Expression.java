package com.prithvianilk.csvql.interpreter.ast;

import com.prithvianilk.csvql.interpreter.Token;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

public sealed interface Expression permits Expression.Simple, Expression.Composite {
    record Simple(Token.Identifier identifier) implements Expression {
    }

    record Composite(Token.Identifier identifier, Operation operator, Expression nextExpression) implements Expression {
    }

    enum Operation {
        PLUS("+"), MINUS("-");

        private final String literalValue;

        Operation(String literalValue) {
            this.literalValue = literalValue;
        }

        public static Optional<Operation> fromLiteralValue(String literalValue) {
            return Arrays
                    .stream(Operation.values())
                    .filter(operation -> operation.matches(literalValue))
                    .findFirst();
        }

        private boolean matches(String literalValue) {
            return Objects.equals(this.literalValue, literalValue);
        }

        public static boolean matchesAny(String literalValue) {
            return Arrays
                    .stream(Operation.values())
                    .anyMatch(operation -> operation.matches(literalValue));
        }
    }
}
