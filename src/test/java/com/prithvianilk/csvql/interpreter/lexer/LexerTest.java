package com.prithvianilk.csvql.interpreter.lexer;

import com.prithvianilk.csvql.interpreter.lexer.Lexer;
import com.prithvianilk.csvql.interpreter.Token;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LexerTest {
    Lexer lexer;

    @ParameterizedTest(name = "{0}")
    @MethodSource("getTestCases")
    void testQueries(String query, List<Token> expectedTokens) {
        lexer = new Lexer(query);
        assertEquals(expectedTokens, lexer.tokens().toList());
    }

    static Stream<Arguments> getTestCases() {
        return Stream.of(
                selectAllColumnsFromCsvFile(),
                selectSingleColumnFromCsvFile(),
                selectSingleColumnWithUnderscoreFromCsvFile(),
                selectTwoColumnsFromCsvFile(),
                selectAllColumnsFromCsvFileWhereSingleColumnEqualsIntegerValue(),
                selectAllColumnsFromCsvFileWhereSingleColumnEqualsStringValue(),
                selectAllColumnsFromCsvFileWhereColumn1EqualsIntegerValueAndColumn2EqualsIntegerValue(),
                selectCountAllColumnsFromCsvFile(),
                selectCountSingleColumnFromCsvFile()
        );
    }

    static Arguments selectCountAllColumnsFromCsvFile() {
        return Arguments.of(
                "select count(*) from results.csv",
                List.of(
                        new Token.Select(),
                        new Token.Count(),
                        new Token.LeftBracket(),
                        new Token.AllColumns(),
                        new Token.RightBracket(),
                        new Token.From(),
                        new Token.Identifier("results.csv")));
    }


    static Arguments selectCountSingleColumnFromCsvFile() {
        return Arguments.of(
                "select count(a) from results.csv",
                List.of(
                        new Token.Select(),
                        new Token.Count(),
                        new Token.LeftBracket(),
                        new Token.Identifier("a"),
                        new Token.RightBracket(),
                        new Token.From(),
                        new Token.Identifier("results.csv")));
    }

    static Arguments selectAllColumnsFromCsvFileWhereColumn1EqualsIntegerValueAndColumn2EqualsIntegerValue() {
        return Arguments.of(
                "select * from results.csv where a = 1 and b = 2",
                List.of(
                        new Token.Select(),
                        new Token.AllColumns(),
                        new Token.From(),
                        new Token.Identifier("results.csv"),
                        new Token.Where(),
                        new Token.Identifier("a"),
                        new Token.Equals(),
                        new Token.Identifier("1"),
                        new Token.And(),
                        new Token.Identifier("b"),
                        new Token.Equals(),
                        new Token.Identifier("2")));
    }

    static Arguments selectAllColumnsFromCsvFileWhereSingleColumnEqualsIntegerValue() {
        return Arguments.of(
                "select * from results.csv where a = 1",
                List.of(
                        new Token.Select(),
                        new Token.AllColumns(),
                        new Token.From(),
                        new Token.Identifier("results.csv"),
                        new Token.Where(),
                        new Token.Identifier("a"),
                        new Token.Equals(),
                        new Token.Identifier("1")));
    }

    static Arguments selectAllColumnsFromCsvFileWhereSingleColumnEqualsStringValue() {
        return Arguments.of(
                "select * from results.csv where a = \"lol\"",
                List.of(
                        new Token.Select(),
                        new Token.AllColumns(),
                        new Token.From(),
                        new Token.Identifier("results.csv"),
                        new Token.Where(),
                        new Token.Identifier("a"),
                        new Token.Equals(),
                        new Token.Identifier("\"lol\"")));
    }

    static Arguments selectAllColumnsFromCsvFile() {
        return Arguments.of(
                "select * from results.csv",
                List.of(
                        new Token.Select(),
                        new Token.AllColumns(),
                        new Token.From(),
                        new Token.Identifier("results.csv")));
    }

    static Arguments selectSingleColumnFromCsvFile() {
        return Arguments.of(
                "select a from results.csv",
                List.of(
                        new Token.Select(),
                        new Token.Identifier("a"),
                        new Token.From(),
                        new Token.Identifier("results.csv")));
    }

    static Arguments selectSingleColumnWithUnderscoreFromCsvFile() {
        return Arguments.of(
                "select a_1 from results.csv",
                List.of(
                        new Token.Select(),
                        new Token.Identifier("a_1"),
                        new Token.From(),
                        new Token.Identifier("results.csv")));
    }

    static Arguments selectTwoColumnsFromCsvFile() {
        return Arguments.of(
                "select a, b from results.csv",
                List.of(
                        new Token.Select(),
                        new Token.Identifier("a"),
                        new Token.Comma(),
                        new Token.Identifier("b"),
                        new Token.From(),
                        new Token.Identifier("results.csv")));
    }
}