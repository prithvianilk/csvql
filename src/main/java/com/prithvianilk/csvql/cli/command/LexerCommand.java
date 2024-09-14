package com.prithvianilk.csvql.cli.command;

import com.prithvianilk.csvql.interpreter.lexer.Lexer;
import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(name = "lexer")
public class LexerCommand implements Callable<Integer> {
    @CommandLine.Parameters(index = "0")
    String query;

    @Override
    public Integer call() {
        Lexer lexer = new Lexer(query);
        System.out.println(lexer.tokens().toList());
        return 0;
    }
}
