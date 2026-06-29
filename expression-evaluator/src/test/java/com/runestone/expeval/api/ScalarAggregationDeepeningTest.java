package com.runestone.expeval.api;

import com.runestone.expeval.environment.ExpressionEnvironment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Scalar Aggregation fast path")
class ScalarAggregationDeepeningTest {

    private static final List<Map<String, Object>> BOOKS = List.of(
            Map.of("author", "Alice", "price", new BigDecimal("5.99"), "qty", BigDecimal.ONE),
            Map.of("author", "Bob", "price", new BigDecimal("12.99"), "qty", new BigDecimal("2")),
            Map.of("author", "Alice", "price", new BigDecimal("8.99"), "qty", new BigDecimal("3"))
    );

    @Test
    @DisplayName("filter + property projection + avg preserves compute and audit results")
    void filterPropertyAvgPreservesComputeAndAuditResults() {
        ExpressionEnvironment environment = env("books", BOOKS);
        MathExpression expression = MathExpression.compile("books[?(@.author = \"Alice\")].price..avg()", environment);

        Map<String, Object> values = Map.of("books", BOOKS);

        assertThat(expression.compute(values)).isEqualByComparingTo("7.49");
        assertThat(expression.computeWithAudit(values).value()).isEqualByComparingTo("7.49");
    }

    @Test
    @DisplayName("upstream map transform is still evaluated before count")
    void mapTransformBeforeCountStillEvaluates() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .registerStaticProvider(FailingFunctions.class)
                .registerExternalSymbol("nums", List.of(BigDecimal.ONE, new BigDecimal("2")), true)
                .build();
        MathExpression expression = MathExpression.compile("nums..map(@ -> fail(@))..count()", environment);

        assertThatThrownBy(() -> expression.compute(Map.of("nums", List.of(BigDecimal.ONE, new BigDecimal("2")))))
                .isInstanceOf(FunctionInvocationException.class)
                .hasMessageContaining("failed to invoke function 'fail'")
                .cause()
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("boom");
    }

    @Test
    @DisplayName("map filter keeps Map Entry Context for values aggregation")
    void mapFilterKeepsEntryContextForValuesAggregation() {
        Map<Key, BigDecimal> inventory = new LinkedHashMap<>();
        inventory.put(new Key("acme", "a"), new BigDecimal("10"));
        inventory.put(new Key("other", "b"), new BigDecimal("100"));
        inventory.put(new Key("acme", "c"), new BigDecimal("5"));

        ExpressionEnvironment environment = env("inventory", inventory);
        MathExpression expression = MathExpression.compile(
                "inventory[?(@.key.domain = \"acme\")]..values()..sum()", environment);

        assertThat(expression.compute(Map.of("inventory", inventory))).isEqualByComparingTo("15");
    }

    @Test
    @DisplayName("empty scalar aggregation identities are preserved")
    void emptyScalarAggregationIdentitiesArePreserved() {
        List<BigDecimal> empty = List.of();
        ExpressionEnvironment environment = env("nums", empty);
        Map<String, Object> values = Map.of("nums", empty);

        assertThat(MathExpression.compile("nums..sum()", environment).compute(values)).isEqualByComparingTo("0");
        assertThat(MathExpression.compile("nums..prod()", environment).compute(values)).isEqualByComparingTo("1");
        assertThat(MathExpression.compile("nums..count()", environment).compute(values)).isEqualByComparingTo("0");
    }

    private static ExpressionEnvironment env(String name, Object value) {
        return ExpressionEnvironment.builder()
                .registerExternalSymbol(name, value, true)
                .build();
    }

    public record Key(String domain, String code) {
    }

    public static final class FailingFunctions {

        private FailingFunctions() {
        }

        public static BigDecimal fail(BigDecimal value) {
            throw new IllegalStateException("boom " + value);
        }
    }
}
