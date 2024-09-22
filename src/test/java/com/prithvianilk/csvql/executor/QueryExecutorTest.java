package com.prithvianilk.csvql.executor;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class QueryExecutorTest {
    static final String BASE_DIR_PATH = "";

    @ParameterizedTest(name = "{0}")
    @MethodSource("getTestCases")
    void testQueries(String query, String expectedResult) {
        QueryExecutor executor = new QueryExecutor(query);
        assertEquals(expectedResult, executor.executeQuery());
    }

    static Stream<Arguments> getTestCases() {
        return Stream.of(
                getTestCase("select_all_columns_from_csv_file"));
    }

    static Arguments getTestCase(String testCaseDirName) {
        try {
            testCaseDirName = "./src/test/resources/query_executor_tests/" + testCaseDirName;
            String inputContent = new String(new FileInputStream(testCaseDirName + "/query.sql").readAllBytes());
            String resultContent = new String(new FileInputStream(testCaseDirName + "/expected_output.csv").readAllBytes());
            return Arguments.of(inputContent, resultContent);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}