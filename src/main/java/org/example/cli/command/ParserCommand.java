package org.example.cli.command;

import org.example.interpreter.lexer.Lexer;
import org.example.interpreter.parser.Parser;
import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(name = "parser")
public class ParserCommand implements Callable<Integer> {
    @CommandLine.Parameters(index = "0")
    String query;

    @Override
    public Integer call() {
        Parser parser = new Parser(new Lexer(query));
        System.out.println(parser.parse());
        return 0;
    }
}
