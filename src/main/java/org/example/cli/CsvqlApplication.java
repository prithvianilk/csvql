package org.example.cli;

import org.example.cli.command.CsvqlCommand;
import org.example.cli.command.LexerCommand;
import org.example.cli.command.ParserCommand;
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