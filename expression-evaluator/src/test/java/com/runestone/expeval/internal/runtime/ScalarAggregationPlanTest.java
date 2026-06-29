package com.runestone.expeval.internal.runtime;

import com.runestone.expeval.environment.ExpressionEnvironment;
import com.runestone.expeval.internal.grammar.ExpressionResultType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Scalar Aggregation - execution plan")
class ScalarAggregationPlanTest {

    private final ExpressionCompiler compiler = new ExpressionCompiler();

    @Test
    @DisplayName("eligible collection chain carries scalar aggregation program")
    void eligibleCollectionChainCarriesScalarAggregationProgram() {
        List<Map<String, Object>> books = List.of(
                Map.of("author", "Alice", "price", BigDecimal.ONE),
                Map.of("author", "Bob", "price", BigDecimal.TEN)
        );
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .registerExternalSymbol("books", books, true)
                .build();

        CompiledExpression compiled = compiler.compile(
                "books[?(@.author = \"Alice\")].price..avg()",
                ExpressionResultType.MATH,
                environment);

        assertThat(compiled.executionPlan().resultExpression()).isInstanceOf(ExecutablePropertyChain.class);
        ExecutablePropertyChain chain = (ExecutablePropertyChain) compiled.executionPlan().resultExpression();
        assertThat(chain.scalarAggregationProgram()).isNotNull();
        assertThat(chain.scalarAggregationProgram().startIndex()).isZero();
    }

    @Test
    @DisplayName("deep scan chain is not planned for scalar aggregation fast path")
    void deepScanChainIsNotPlannedForScalarAggregationFastPath() {
        Map<String, Object> store = Map.of("price", BigDecimal.ONE);
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .registerExternalSymbol("store", store, true)
                .build();

        CompiledExpression compiled = compiler.compile(
                "store..ds(price)..sum()",
                ExpressionResultType.MATH,
                environment);

        assertThat(compiled.executionPlan().resultExpression()).isInstanceOf(ExecutablePropertyChain.class);
        ExecutablePropertyChain chain = (ExecutablePropertyChain) compiled.executionPlan().resultExpression();
        assertThat(chain.scalarAggregationProgram()).isNull();
    }
}
