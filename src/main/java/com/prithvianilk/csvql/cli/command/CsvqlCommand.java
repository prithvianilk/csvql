package com.prithvianilk.csvql.cli.command;

import picocli.CommandLine;

import java.io.File;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "csvql")
public class CsvqlCommand implements Callable<Integer> {
    @CommandLine.Parameters(index = "0")
    File file;

    @CommandLine.Parameters(index = "1")
    String query;

    @Override
    public Integer call() {
        throw new RuntimeException();
    }
}
