package com.prithvianilk.csvql.executor;

import com.prithvianilk.csvql.interpreter.Token;
import com.prithvianilk.csvql.interpreter.ast.ColumnProjection;
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
import java.util.ArrayList;
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

    private final boolean isAggregationQuery;

    private final List<AggregationResult> aggregationResults;

    private boolean isAggregationStarted;

    private List<Integer> projectedColumnIndices;

    private final StringWriter writer;

    public QueryExecutor(String query) {
        Parser parser = new Parser(new Lexer(query));
        this.query = parser.parse();
        this.writer = new StringWriter();
        this.columnNameToIndexMap = new HashMap<>();

        this.isAggregationQuery = this.query
                .columnProjections()
                .stream()
                .anyMatch(ColumnProjection::isAggregation);

        this.aggregationResults = new ArrayList<>();
        this.isAggregationStarted = false;

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
        executeAfterRowsCompletion();
        return writer.toString();
    }

    private void executeAfterRowsCompletion() {
        if (!isAggregationQuery) {
            return;
        }

        String aggregationRow = aggregationResults
                .stream()
                .map(AggregationResult::print)
                .collect(Collectors.joining(","));

        writer.append(aggregationRow);
        writer.append("\n");
    }

    private void executeColumnNames() {
        String line = readLine().orElseThrow(QueryExecutionException.UnhandledError::new);
        List<String> columns = Arrays.asList(line.split(","));
        initColumnNameToIndexMap(columns);

        if (isAggregationQuery) {
            String firstRow = query.columnProjections()
                    .stream()
                    .map(this::print)
                    .collect(Collectors.joining(","));

            writer.append(firstRow);
            writer.append("\n");
        } else {
            initProjectedColumnIndices(columns);

            String columnNamesRow = projectedColumnIndices
                    .stream()
                    .map(columns::get)
                    .collect(Collectors.joining(","));

            writer.append(columnNamesRow);
            writer.append("\n");
        }
    }

    private String print(ColumnProjection projection) {
        return switch (projection) {
            case ColumnProjection.Aggregation.Count(ColumnProjection.Aggregate.Column(Token.Asterisks())) -> {
                yield "count(*)";
            }
            case ColumnProjection.Aggregation.Count(
                    ColumnProjection.Aggregate.Column(Token.Identifier(String value))
            ) -> {
                yield "count(%s)".formatted(value);
            }
            case ColumnProjection.Aggregation.Count ignored -> throw new QueryExecutionException.InvalidArgument();

            case ColumnProjection.Aggregation.Sum(ColumnProjection.Aggregate.Column(Token.Asterisks())) -> {
                yield "sum(*)";
            }
            case ColumnProjection.Aggregation.Sum(
                    ColumnProjection.Aggregate.Column(Token.Identifier(String value))
            ) -> {
                yield "sum(%s)".formatted(value);
            }
            case ColumnProjection.Aggregation.Sum ignored -> throw new QueryExecutionException.InvalidArgument();

            case ColumnProjection.Column(Token.Asterisks()) -> "*";
            case ColumnProjection.Column(Token.Identifier(String value)) -> value;
            case ColumnProjection.Column ignored -> throw new QueryExecutionException.InvalidArgument();
        };
    }

    private void initColumnNameToIndexMap(List<String> columns) {
        for (int i = 0; i < columns.size(); ++i) {
            columnNameToIndexMap.put(columns.get(i), i);
        }
    }

    private void initProjectedColumnIndices(List<String> columns) {
        if (query.columnProjections().getFirst() instanceof ColumnProjection.Column(Token.Asterisks())) {
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

        if (isAggregationQuery) {
            executeRowAggregation(items);
            return Optional.empty();
        }

        String projectedRow = projectedColumnIndices
                .stream()
                .map(items::get)
                .collect(Collectors.joining(","));

        return Optional.of(projectedRow);
    }

    private void executeRowAggregation(List<String> items) {
        for (int i = 0; i < query.columnProjections().size(); ++i) {
            ColumnProjection projection = query.columnProjections().get(i);
            switch (projection) {
                case ColumnProjection.Aggregation.Count(ColumnProjection.Aggregate ignored) -> {
                    executeCountAggregation(i);
                }
                case ColumnProjection.Column ignored -> throw new QueryExecutionException.InvalidArgument();

                case ColumnProjection.Aggregation.Sum(
                        ColumnProjection.Aggregate.Column(Token.Identifier(String columnName))
                ) -> {
                    executeSumAggregation(items, columnName, i);
                }
                case ColumnProjection.Aggregation.Sum ignored -> throw new QueryExecutionException.InvalidArgument();
            }
        }

        isAggregationStarted = true;
    }

    private void executeSumAggregation(List<String> items, String columnName, int i) {
        AggregationResult aggregationResult = getRowAggregationResult(items, columnName);

        if (isAggregationStarted) {
            AggregationResult existingResult = aggregationResults.get(i);
            if (!(aggregationResult instanceof AggregationResult.Int(int value) &&
                    existingResult instanceof AggregationResult.Int(int existingValue))) {
                throw new QueryExecutionException.InvalidArgument();
            }

            aggregationResults.set(i, new AggregationResult.Int(value + existingValue));
        } else {
            aggregationResults.add(aggregationResult);
        }
    }

    private void executeCountAggregation(int i) {
        if (isAggregationStarted) {
            AggregationResult existingResult = aggregationResults.get(i);
            if (!(existingResult instanceof AggregationResult.Int(int value))) {
                throw new QueryExecutionException.InvalidArgument();
            }
            aggregationResults.set(i, new AggregationResult.Int(value + 1));
        } else {
            aggregationResults.add(new AggregationResult.Int(1));
        }
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
        ExpressionResult lhsResult = executeExpression(conditional.lhs(), items);
        ExpressionResult rhsResult = executeExpression(conditional.rhs(), items);

        return switch (conditional.predicate()) {
            case EQUALS -> Objects.equals(lhsResult, rhsResult);
            case LESSER_THAN -> {
                if (!(lhsResult instanceof ExpressionResult.Int(int lhs)
                        && rhsResult instanceof ExpressionResult.Int(int rhs))) {
                    throw new QueryExecutionException.InvalidArgument();
                }

                yield lhs < rhs;
            }
            case GREATER_THAN -> {
                if (!(lhsResult instanceof ExpressionResult.Int(int lhs)
                        && rhsResult instanceof ExpressionResult.Int(int rhs))) {
                    throw new QueryExecutionException.InvalidArgument();
                }

                yield lhs > rhs;
            }
            case LESSER_THAN_EQUALS -> {
                if (!(lhsResult instanceof ExpressionResult.Int(int lhs)
                        && rhsResult instanceof ExpressionResult.Int(int rhs))) {
                    throw new QueryExecutionException.InvalidArgument();
                }

                yield lhs <= rhs;
            }
            case GREATER_THAN_EQUALS -> {
                if (!(lhsResult instanceof ExpressionResult.Int(int lhs)
                        && rhsResult instanceof ExpressionResult.Int(int rhs))) {
                    throw new QueryExecutionException.InvalidArgument();
                }

                yield lhs >= rhs;
            }
        };
    }

    private ExpressionResult executeExpression(Expression expression, List<String> items) {
        return switch (expression) {
            case Expression.Simple(Expression.Value value) -> getResultType(value, items);
            case Expression.Composite composite -> executeCompositeExpression(composite, items);
        };
    }

    private ExpressionResult getResultType(Expression.Value valueType, List<String> items) {
        return switch (valueType) {
            case Expression.Value.ColumnName(String columnName) -> getRowResult(items, columnName);
            case Expression.Value.Int(int value) -> new ExpressionResult.Int(value);
            case Expression.Value.Str(String value) -> new ExpressionResult.Str(value);
        };
    }

    private ExpressionResult getRowResult(List<String> items, String columnName) {
        String item = items.get(columnNameToIndexMap.get(columnName));
        try {
            return new ExpressionResult.Int(Integer.parseInt(item));
        } catch (NumberFormatException e) {
            return new ExpressionResult.Str(item);
        }
    }

    private AggregationResult getRowAggregationResult(List<String> items, String columnName) {
        String item = items.get(columnNameToIndexMap.get(columnName));
        return new AggregationResult.Int(Integer.parseInt(item));
    }

    private ExpressionResult executeCompositeExpression(Expression.Composite composite, List<String> items) {
        ExpressionResult result = getResultType(composite.value(), items);
        ExpressionResult nextResult = executeExpression(composite.nextExpression(), items);

        if (result instanceof ExpressionResult.Int(int value)
                && nextResult instanceof ExpressionResult.Int(int nextValue)) {
            int finalValue = switch (composite.operation()) {
                case PLUS -> value + nextValue;
                case MINUS -> value - nextValue;
                case MULTIPLY -> value * nextValue;
                case DIVIDE -> value / nextValue;
            };

            return new ExpressionResult.Int(finalValue);
        }

        throw new QueryExecutionException.InvalidArgument();
    }

    private Optional<String> readLine() {
        try {
            return Optional.ofNullable(csvReader.readLine());
        } catch (IOException e) {
            throw new QueryExecutionException.UnhandledError();
        }
    }
}
