package com.prithvianilk.csvql.interpreter.parser;

import com.prithvianilk.csvql.interpreter.ast.ColumnProjection;
import com.prithvianilk.csvql.interpreter.ast.Conditional;
import com.prithvianilk.csvql.interpreter.ast.Expression;
import com.prithvianilk.csvql.interpreter.ast.Query;
import com.prithvianilk.csvql.interpreter.Token;
import com.prithvianilk.csvql.interpreter.lexer.Lexer;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collections;
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
                selectTwoColumnsFromCsvFile(),
                selectAllColumnsFromCsvFileWhereSingleColumnEqualsIntegerValue(),
                selectAllColumnsFromCsvFileWhereColumn1EqualsIntegerValueAndColumn2EqualsIntegerValue(),
                selectAllColumnsFromCsvFileWhereColumn1EqualsColumn2MultipliedByTwo(),
                selectAllColumnsFromCsvFileWhereColumn1EqualsColumn2DividedByTwo(),
                selectCountAllColumnsFromCsvFile(),
                selectCountSingleColumnFromCsvFile(),
                selectSumAllColumnsFromCsvFile(),
                selectSumSingleColumnFromCsvFile()
        );
    }

    static Arguments selectAllColumnsFromCsvFile() {
        return Arguments.of(
                "select * from results.csv",
                new Query(List.of(new ColumnProjection.Column(new Token.Asterisks())),
                        new Token.Identifier("results.csv"),
                        Collections.emptyList()));
    }

    static Arguments selectSingleColumnFromCsvFile() {
        return Arguments.of(
                "select a from results.csv",
                new Query(List.of(new ColumnProjection.Column(new Token.Identifier("a"))),
                        new Token.Identifier("results.csv"),
                        Collections.emptyList()));
    }

    static Arguments selectTwoColumnsFromCsvFile() {
        return Arguments.of(
                "select a, b from results.csv",
                new Query(
                        List.of(new ColumnProjection.Column(new Token.Identifier("a")),
                                new ColumnProjection.Column(new Token.Identifier("b"))),
                        new Token.Identifier("results.csv"), Collections.emptyList()));
    }

    static Arguments selectAllColumnsFromCsvFileWhereSingleColumnEqualsIntegerValue() {
        return Arguments.of(
                "select * from results.csv where a = 1",
                new Query(
                        List.of(new ColumnProjection.Column(new Token.Asterisks())),
                        new Token.Identifier("results.csv"),
                        Collections.singletonList(
                                new Conditional(
                                        new Expression.Simple(new Expression.Value.ColumnName("a")),
                                        Conditional.Predicate.EQUALS,
                                        new Expression.Simple(new Expression.Value.Int(1))))));
    }

    static Arguments selectAllColumnsFromCsvFileWhereColumn1EqualsIntegerValueAndColumn2EqualsIntegerValue() {
        return Arguments.of(
                "select * from results.csv where a = 1 and b = 2",
                new Query(
                        List.of(new ColumnProjection.Column(new Token.Asterisks())),
                        new Token.Identifier("results.csv"),
                        List.of(new Conditional(
                                        new Expression.Simple(new Expression.Value.ColumnName("a")),
                                        Conditional.Predicate.EQUALS,
                                        new Expression.Simple(new Expression.Value.Int(1))),
                                new Conditional(
                                        new Expression.Simple(new Expression.Value.ColumnName("b")),
                                        Conditional.Predicate.EQUALS,
                                        new Expression.Simple(new Expression.Value.Int(2))))));
    }

    static Arguments selectAllColumnsFromCsvFileWhereColumn1EqualsColumn2MultipliedByTwo() {
        return Arguments.of(
                "select * from results.csv where a = b * 2",
                new Query(
                        List.of(new ColumnProjection.Column(new Token.Asterisks())),
                        new Token.Identifier("results.csv"),
                        List.of(new Conditional(
                                new Expression.Simple(new Expression.Value.ColumnName("a")),
                                Conditional.Predicate.EQUALS,
                                new Expression.Composite(new Expression.Value.ColumnName("b"), Expression.Operation.MULTIPLY, new Expression.Simple(new Expression.Value.Int(2))))
                        )));
    }

    static Arguments selectAllColumnsFromCsvFileWhereColumn1EqualsColumn2DividedByTwo() {
        return Arguments.of(
                "select * from results.csv where a = b / 2",
                new Query(
                        List.of(new ColumnProjection.Column(new Token.Asterisks())),
                        new Token.Identifier("results.csv"),
                        List.of(new Conditional(
                                new Expression.Simple(new Expression.Value.ColumnName("a")),
                                Conditional.Predicate.EQUALS,
                                new Expression.Composite(new Expression.Value.ColumnName("b"), Expression.Operation.DIVIDE, new Expression.Simple(new Expression.Value.Int(2))))
                        )));
    }

    static Arguments selectCountSingleColumnFromCsvFile() {
        return Arguments.of(
                "select count(a) from results.csv",
                new Query(List.of(ColumnProjection.Aggregation.Count.fromColumn(new Token.Identifier("a"))),
                        new Token.Identifier("results.csv"),
                        Collections.emptyList()));
    }

    static Arguments selectCountAllColumnsFromCsvFile() {
        return Arguments.of(
                "select count(*) from results.csv",
                new Query(List.of(ColumnProjection.Aggregation.Count.fromColumn(new Token.Asterisks())),
                        new Token.Identifier("results.csv"),
                        Collections.emptyList()));
    }

    static Arguments selectSumAllColumnsFromCsvFile() {
        return Arguments.of(
                "select sum(*) from results.csv",
                new Query(List.of(ColumnProjection.Aggregation.Sum.fromColumn(new Token.Asterisks())),
                        new Token.Identifier("results.csv"),
                        Collections.emptyList()));
    }

    static Arguments selectSumSingleColumnFromCsvFile() {
        return Arguments.of(
                "select sum(a) from results.csv",
                new Query(List.of(ColumnProjection.Aggregation.Sum.fromColumn(new Token.Identifier("a"))),
                        new Token.Identifier("results.csv"),
                        Collections.emptyList()));
    }
}