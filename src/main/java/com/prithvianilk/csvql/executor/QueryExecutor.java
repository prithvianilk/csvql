package com.prithvianilk.csvql.executor;

import com.prithvianilk.csvql.interpreter.Token;
import com.prithvianilk.csvql.interpreter.ast.Conditional;
import com.prithvianilk.csvql.interpreter.ast.Expression;
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
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class QueryExecutor {
    private final Query query;

    private BufferedReader csvReader;

    private Map<String, Integer> columnNameToIndexMap;

    private List<Integer> projectedColumnIndices;

    private final StringWriter writer;

    public QueryExecutor(String query) {
        Parser parser = new Parser(new Lexer(query));
        this.query = parser.parse();
        this.writer = new StringWriter();
        initCsvReader();
    }

    private void initCsvReader() {
        try {
            csvReader = new BufferedReader(new FileReader(query.csvFileName().value()));
        } catch (FileNotFoundException e) {
            throw new QueryExecutionException();
        }
    }

    public String executeQuery() {
        executeColumnNames();
        executeAllRows();
        return writer.toString();
    }

    private void executeColumnNames() {
        String line = readLine().orElseThrow();
        List<String> columns = Arrays.asList(line.split(","));

        projectedColumnIndices = initColumnIndices(columns);

        String columnNamesRow = projectedColumnIndices
                .stream()
                .map(columns::get)
                .collect(Collectors.joining(","));

        writer.append(columnNamesRow);
        writer.append("\n");
    }

    private List<Integer> initColumnIndices(List<String> columns) {
        if (query.columnNameTokens().getFirst() instanceof Token.AllColumns) {
            return IntStream
                    .range(0, columns.size())
                    .boxed()
                    .toList();
        }

        return query
                .getProjectableColumnNames()
                .stream()
                .map(columns::indexOf)
                .toList();
    }

    private void executeAllRows() {
        int rowCount = 0;
        while (true) {
            Optional<String> line = readLine();
            if (line.isEmpty()) {
                break;
            }

            if (rowCount++ > 0) {
                writer.append('\n');
            }

            line.ifPresent(this::executeRow);
        }
    }

    private void executeRow(String line) {
        List<String> items = Arrays.asList(line.split(","));

        if (!rowSatisfiesAllConditions(items)) {
            return;
        }

        String projectedRow = projectedColumnIndices
                .stream()
                .map(items::get)
                .collect(Collectors.joining(","));

        writer.append(projectedRow);
    }

    private boolean rowSatisfiesAllConditions(List<String> items) {
        if (query.conditionals().isEmpty()) {
            return true;
        }

        return query
                .conditionals()
                .stream()
                .allMatch(conditional -> rowSatisfiesCondition(conditional, items));
    }

    private boolean rowSatisfiesCondition(Conditional conditional, List<String> items) {
        return switch (conditional.predicate()) {
            case EQUALS -> {
                Expression.ValueType lhsValue = executeExpression(conditional.lhs(), items);
                Expression.ValueType rhsValue = executeExpression(conditional.rhs(), items);
                yield Objects.equals(lhsValue, rhsValue);
            }
        };
    }

    private Expression.ValueType executeExpression(Expression expression, List<String> items) {
        return switch (expression) {
            case Expression.Simple(Expression.ValueType value) -> value;
            case Expression.Composite composite -> executeCompositeExpression(composite, items);
        };
    }

    private Expression.ValueType executeCompositeExpression(Expression.Composite composite, List<String> items) {
        int value = getIntValue(composite.valueType(), items);
        int nextValue = getIntValue(executeExpression(composite.nextExpression(), items), items);

        int finalValue = switch (composite.operation()) {
            case PLUS -> value + nextValue;
            case MINUS -> value - nextValue;
        };

        return new Expression.ValueType.Int(finalValue);
    }

    private int getIntValue(Expression.ValueType valueType, List<String> items) {
        return switch (valueType) {
            case Expression.ValueType.ColumnName(String columnName) -> getItemValue(columnName, items);
            case Expression.ValueType.Int(int intValue) -> intValue;
            default -> throw new QueryExecutionException();
        };
    }

    private int getItemValue(String columnName, List<String> items) {
        String item = items.get(columnNameToIndexMap.get(columnName));
        return Integer.parseInt(item);
    }

    private Optional<String> readLine() {
        try {
            return Optional.ofNullable(csvReader.readLine());
        } catch (IOException e) {
            throw new QueryExecutionException();
        }
    }
}
