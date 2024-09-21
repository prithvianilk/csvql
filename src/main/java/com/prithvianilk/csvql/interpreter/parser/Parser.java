package com.prithvianilk.csvql.interpreter.parser;

import com.prithvianilk.csvql.interpreter.Token;
import com.prithvianilk.csvql.interpreter.ast.Conditional;
import com.prithvianilk.csvql.interpreter.ast.Expression;
import com.prithvianilk.csvql.interpreter.ast.Query;
import com.prithvianilk.csvql.interpreter.lexer.Lexer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class Parser {
    private final Lexer lexer;

    private Token currentToken;

    public Parser(Lexer lexer) {
        this.lexer = lexer;
    }

    public Query parse() {
        parseSelect();
        List<Token> columns = parserColumns();
        parseFrom();
        Token.Identifier csvFileName = parseCsvFileName();
        List<Conditional> conditionals = parseConditionals();
        return new Query(columns, csvFileName, conditionals);
    }

    private List<Conditional> parseConditionals() {
        if (!(currentToken instanceof Token.Where)) {
            return Collections.emptyList();
        }
        currentToken = lexer.nextToken();

        List<Conditional> conditionals = new ArrayList<>();
        while (true) {
            Optional<Conditional> conditional = parseConditional();
            if (conditional.isEmpty()) {
                break;
            }
            conditional.ifPresent(conditionals::add);
            if (!(currentToken instanceof Token.And)) {
                break;
            }
            currentToken = lexer.nextToken();
        }

        return conditionals;
    }

    private Optional<Conditional> parseConditional() {
        if (currentToken instanceof Token.Eof) {
            return Optional.empty();
        }

        Expression lhs = parseExpression();
        Conditional.Predicate predicate = parsePredicate();
        Expression rhs = parseExpression();
        return Optional.of(new Conditional(lhs, predicate, rhs));
    }

    private Expression parseExpression() {
        Token prevToken = this.currentToken;
        if (!(prevToken instanceof Token.Identifier(String value))) {
            throw new ParserException();
        }

        Expression.ValueType valueType = parseValueType(value);

        currentToken = lexer.nextToken();
        return switch (currentToken) {
            case Token.Plus ignored -> {
                currentToken = lexer.nextToken();
                yield new Expression.Composite(valueType, Expression.Operation.PLUS, parseExpression());
            }
            case Token.Minus ignored -> {
                currentToken = lexer.nextToken();
                yield new Expression.Composite(valueType, Expression.Operation.MINUS, parseExpression());
            }
            default -> new Expression.Simple(valueType);
        };
    }

    private Expression.ValueType parseValueType(String value) {
        if (value.charAt(0) == '"' && value.charAt(value.length() - 1) == '"') {
            return new Expression.ValueType.Str(value.substring(1, value.length() - 1));
        }

        try {
            int intValue = Integer.parseInt(value);
            return new Expression.ValueType.Int(intValue);
        } catch (Exception e) {
            return new Expression.ValueType.ColumnName(value);
        }
    }

    private Conditional.Predicate parsePredicate() {
        if (!(currentToken instanceof Token.Equals)) {
            throw new ParserException();
        }
        currentToken = lexer.nextToken();

        return Conditional.Predicate.EQUALS;
    }

    private Token.Identifier parseCsvFileName() {
        if (!(currentToken instanceof Token.Identifier csvFileNameToken)) {
            throw new ParserException();
        }
        currentToken = lexer.nextToken();
        return csvFileNameToken;
    }

    private void parseFrom() {
        if (!(currentToken instanceof Token.From)) {
            throw new ParserException();
        }
        currentToken = lexer.nextToken();
    }

    private void parseSelect() {
        currentToken = lexer.nextToken();
        if (!(currentToken instanceof Token.Select)) {
            throw new ParserException();
        }
        currentToken = lexer.nextToken();
    }

    private List<Token> parserColumns() {
        List<Token> columns = new ArrayList<>();

        while (true) {
            if (currentToken instanceof Token.AllColumns allColumns) {
                columns.add(allColumns);
                currentToken = lexer.nextToken();
                break;
            }

            if (currentToken instanceof Token.Comma) {
                currentToken = lexer.nextToken();
                continue;
            }

            if (!(currentToken instanceof Token.Identifier identifier)) {
                break;
            }

            columns.add(identifier);
            currentToken = lexer.nextToken();
        }

        return columns;
    }
}
