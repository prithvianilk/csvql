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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class QueryExecutor {
    private final Query query;

    private BufferedReader csvReader;

    private final Map<String, Integer> columnNameToIndexMap;

    private List<Integer> projectedColumnIndices;

    private final StringWriter writer;

    public QueryExecutor(String query) {
        Parser parser = new Parser(new Lexer(query));
        this.query = parser.parse();
        this.writer = new StringWriter();
        this.columnNameToIndexMap = new HashMap<>();
        initCsvReader();
    }

    private void initCsvReader() {
        try {
            csvReader = new BufferedReader(new FileReader(query.csvFileName().value()));
        } catch (FileNotFoundException e) {
            throw new QueryExecutionException.FileDoesNotExist(query.csvFileName().value());
        }
    }

    public String executeQuery() {
        executeColumnNames();
        executeAllRows();
        return writer.toString();
    }

    private void executeColumnNames() {
        String line = readLine().orElseThrow(QueryExecutionException.UnhandledError::new);

        List<String> columns = Arrays.asList(line.split(","));

        initColumnNameToIndexMap(columns);
        initProjectedColumnIndices(columns);

        String columnNamesRow = projectedColumnIndices
                .stream()
                .map(columns::get)
                .collect(Collectors.joining(","));

        writer.append(columnNamesRow);
        writer.append("\n");
    }

    private void initColumnNameToIndexMap(List<String> columns) {
        for (int i = 0; i < columns.size(); ++i) {
            columnNameToIndexMap.put(columns.get(i), i);
        }
    }

    private void initProjectedColumnIndices(List<String> columns) {
        if (query.columnNameTokens().getFirst() instanceof Token.AllColumns) {
            projectedColumnIndices = IntStream
                    .range(0, columns.size())
                    .boxed()
                    .toList();
        } else {
            projectedColumnIndices = query
                    .getProjectableColumnNames()
                    .stream()
                    .map(columns::indexOf)
                    .toList();
        }
    }

    private void executeAllRows() {
        while (true) {
            Optional<String> line = readLine();
            if (line.isEmpty()) {
                break;
            }

            line.flatMap(this::executeRow).ifPresent(projectedRow -> {
                writer.append(projectedRow);
                writer.append('\n');
            });
        }
    }

    private Optional<String> executeRow(String line) {
        List<String> items = Arrays.asList(line.split(","));

        if (!rowSatisfiesAllConditions(items)) {
            return Optional.empty();
        }

        String projectedRow = projectedColumnIndices
                .stream()
                .map(items::get)
                .collect(Collectors.joining(","));

        return Optional.of(projectedRow);
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
            case Expression.Simple(Expression.ValueType valueType) -> {
                int intValue = getIntValue(valueType, items);
                yield new Expression.ValueType.Int(intValue);
            }
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
            default -> throw new QueryExecutionException.InvalidArgument();
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
            throw new QueryExecutionException.UnhandledError();
        }
    }
}
