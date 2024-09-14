package com.prithvianilk.csvql.cli.command;

import com.prithvianilk.csvql.executor.QueryExecutor;
import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(name = "csvql")
public class CsvqlCommand implements Callable<Integer> {
    @CommandLine.Parameters(index = "0")
    String query;

    @Override
    public Integer call() {
        QueryExecutor queryExecutor = new QueryExecutor(query);
        queryExecutor.executeQuery();

        return 0;
    }
}
