package com.prithvianilk.csvql.cli.command;

import com.prithvianilk.csvql.interpreter.lexer.Lexer;
import com.prithvianilk.csvql.interpreter.parser.Parser;
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
