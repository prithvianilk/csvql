package org.example.lexer;

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
                selectTwoColumnsFromCsvFile()
        );
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

    private static Arguments selectSingleColumnFromCsvFile() {
        return Arguments.of(
                "select a from results.csv",
                List.of(
                        new Token.Select(),
                        new Token.Identifier("a"),
                        new Token.From(),
                        new Token.Identifier("results.csv")));
    }

    private static Arguments selectTwoColumnsFromCsvFile() {
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