package com.prithvianilk.csvql.interpreter.parser;

import com.prithvianilk.csvql.interpreter.ast.Query;
import com.prithvianilk.csvql.interpreter.Token;
import com.prithvianilk.csvql.interpreter.lexer.Lexer;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ParserTest {
    Parser parser;

    @ParameterizedTest(name = "{0}")
    @MethodSource("getTestCases")
    void testQueries(String query, Query expectedQuery) {
        parser = new Parser(new Lexer(query));
        assertEquals(expectedQuery, parser.parse());
    }

    static Stream<Arguments> getTestCases() {
        return Stream.of(
                selectAllColumnsFromCsvFile(),
                selectSingleColumnFromCsvFile(),
                selectTwoColumnsFromCsvFile()
        );
    }

    static Arguments selectAllColumnsFromCsvFile() {
        return Arguments.of(
                "select * from results.csv",
                new Query(List.of(new Token.AllColumns()), new Token.Identifier("results.csv")));
    }

    static Arguments selectSingleColumnFromCsvFile() {
        return Arguments.of(
                "select a from results.csv",
                new Query(List.of(new Token.Identifier("a")), new Token.Identifier("results.csv")));
    }

    static Arguments selectTwoColumnsFromCsvFile() {
        return Arguments.of(
                "select a, b from results.csv",
                new Query(
                        List.of(new Token.Identifier("a"), new Token.Identifier("b")),
                        new Token.Identifier("results.csv")));
    }
}