package com.prithvianilk.csvql.cli;

import com.prithvianilk.csvql.cli.command.CsvqlCommand;
import com.prithvianilk.csvql.cli.command.ParserCommand;
import com.prithvianilk.csvql.cli.command.LexerCommand;
import picocli.CommandLine;

@CommandLine.Command(subcommands = {
        CsvqlCommand.class,
        LexerCommand.class,
        ParserCommand.class
})
public class CsvqlApplication {
    public static void main(String[] args) {
        int exitCode = new CommandLine(new CsvqlApplication()).execute(args);
        System.exit(exitCode);
    }
}