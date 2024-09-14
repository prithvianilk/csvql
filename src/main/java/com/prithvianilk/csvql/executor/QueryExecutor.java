package com.prithvianilk.csvql.executor;

import com.prithvianilk.csvql.interpreter.Token;
import com.prithvianilk.csvql.interpreter.ast.Query;
import com.prithvianilk.csvql.interpreter.lexer.Lexer;
import com.prithvianilk.csvql.interpreter.parser.Parser;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class QueryExecutor {
    private final Query query;

    private BufferedReader csvReader;

    private List<Integer> columnIndices;

    private final StringWriter writer;

    public QueryExecutor(String query) {
        Parser parser = new Parser(new Lexer(query));
        this.query = parser.parse();
        this.writer = new StringWriter();
    }

    public String executeQuery() {
        initCsvReader();
        initColumnIndices();
        printRows();
        return writer.toString();
    }

    private void initCsvReader() {
        try {
            csvReader = new BufferedReader(new FileReader(query.csvFileName().value()));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private void initColumnIndices() {
        String line = readLine().orElseThrow();
        List<String> columns = Arrays.asList(line.split(","));

        if (query.columnNameTokens().getFirst() instanceof Token.AllColumns) {
            columnIndices = IntStream
                    .range(0, columns.size())
                    .boxed()
                    .toList();
        } else {
            columnIndices = query
                    .getProjectableColumnNames()
                    .stream()
                    .map(columns::indexOf)
                    .toList();
        }
    }

    private void printRows() {
        int rowCount = 0;
        while (true) {
            Optional<String> line = readLine();
            if (line.isEmpty()) {
                break;
            }

            if (rowCount++ > 0) {
                writer.append('\n');
            }

            line.ifPresent(this::printRow);
        }
    }

    private void printRow(String line) {
        List<String> items = Arrays.asList(line.split(","));

        String projectedRow = columnIndices
                .stream()
                .map(items::get)
                .collect(Collectors.joining(","));

        writer.append(projectedRow);
    }

    private Optional<String> readLine() {
        try {
            return Optional.ofNullable(csvReader.readLine());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
