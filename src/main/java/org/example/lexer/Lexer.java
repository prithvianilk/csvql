package org.example.lexer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.OptionalInt;
import java.util.stream.Stream;

public class Lexer {
    private final BufferedReader queryReader;

    private OptionalInt oldChar;

    private static final char EOF = '\uFFFF';

    private static final String EOF_STRING = "\uFFFF";

    public Lexer(BufferedReader queryReader) {
        this.queryReader = queryReader;
        oldChar = OptionalInt.empty();
    }

    public Lexer(String query) {
        this.queryReader = new BufferedReader(new StringReader(query));
        oldChar = OptionalInt.empty();
    }

    public Stream<Token> tokens() {
        return Stream
                .generate(this::nextToken)
                .takeWhile(token -> !(token instanceof Token.Eof));
    }

    public Token nextToken() {
        String word = readWord();

        return switch (word) {
            case "*" -> new Token.AllColumns();
            case "," -> new Token.Comma();
            case "select" -> new Token.Select();
            case "from" -> new Token.From();
            case EOF_STRING -> new Token.Eof();
            default -> new Token.Identifier(word);
        };
    }

    // TODO: Clean up :(
    private String readWord() {
        char c = readCharacter();

        if (c == EOF) {
            return EOF_STRING;
        }

        while (c == ' ') {
            c = readCharacter();
        }

        if (c == EOF) {
            return EOF_STRING;
        } else if (c == '*') {
            return "*";
        } else if (c == ',') {
            return ",";
        }

        StringWriter wordWriter = new StringWriter();
        while (!(c == EOF || c == ' ' || c == ',')) {
            wordWriter.append(c);
            c = readCharacter();
        }

        if (c != EOF) {
            oldChar = OptionalInt.of(c);
        }

        return wordWriter.toString();
    }

    private char readCharacter() {
        try {
            if (oldChar.isPresent()) {
                char c = (char) oldChar.getAsInt();
                oldChar = OptionalInt.empty();
                return c;
            }
            return (char) queryReader.read();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
