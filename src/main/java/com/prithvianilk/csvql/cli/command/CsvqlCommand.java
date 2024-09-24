package com.prithvianilk.csvql.cli.command;

import com.prithvianilk.csvql.executor.QueryExecutionException;
import com.prithvianilk.csvql.executor.QueryExecutor;
import com.prithvianilk.csvql.interpreter.parser.ParserException;
import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(name = "csvql")
public class CsvqlCommand implements Callable<Integer> {
    @CommandLine.Parameters(index = "0")
    String query;

    @Override
    public Integer call() {
        try {
            QueryExecutor queryExecutor = new QueryExecutor(query);
            System.out.println(queryExecutor.executeQuery());
            return 0;
        } catch (ParserException e) {
            return handleParserException(e);
        } catch (QueryExecutionException e) {
            return handleQueryExecutionException(e);
        }
    }

    private int handleParserException(ParserException e) {
        System.err.println("Parsing error: ");
        throw e;
    }

    private int handleQueryExecutionException(QueryExecutionException e) {
        System.err.println("Query execution error:");
        switch (e) {
            case QueryExecutionException.FileDoesNotExist fileDoesNotExistException -> {
                System.err.println("File does not exist: " + fileDoesNotExistException.getFileName());
                System.err.println();
            }
            default -> throw e;
        }

        return 1;
    }
}
