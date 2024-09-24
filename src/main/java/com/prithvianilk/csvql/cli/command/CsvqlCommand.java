package com.prithvianilk.csvql.cli.command;

import com.prithvianilk.csvql.executor.QueryExecutionException;
import com.prithvianilk.csvql.executor.QueryExecutor;
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
        } catch (QueryExecutionException e) {
            System.err.println("Error:");
            switch (e) {
                case QueryExecutionException.FileDoesNotExist fileDoesNotExistException -> {
                    System.err.println("File does not exist: " + fileDoesNotExistException.getFileName());
                    System.err.println();
                }
                default -> System.err.println("Received exception: " + e);
            }
            return 1;
        }
    }
}
