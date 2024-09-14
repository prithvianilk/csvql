package org.example.interpreter.parser;

import org.example.interpreter.Token;
import org.example.interpreter.ast.Query;
import org.example.interpreter.lexer.Lexer;

import java.util.ArrayList;
import java.util.List;

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
        return new Query(columns, csvFileName);
    }

    private Token.Identifier parseCsvFileName() {
        if (!(currentToken instanceof Token.Identifier csvFileNameToken)) {
            throw new ParserException();
        }
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
