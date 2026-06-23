package com.runestone.expeval.api;

import com.runestone.expeval.api.support.FoldingNavigationFixtures;
import com.runestone.expeval.api.support.FoldingNavigationFixtures.CountingBox;
import com.runestone.expeval.api.support.FoldingNavigationFixtures.TrackedList;
import com.runestone.expeval.environment.ExpressionEnvironment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;

import static com.runestone.expeval.api.support.FoldingNavigationFixtures.bd;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Collection navigation barriers for runtime-resolved symbols")
class OverridableSymbolCollectionNavigationTest {

    @Test
    @DisplayName("overridable roots are not folded")
    void overridableRootsAreNotFolded() {
        TrackedList<BigDecimal> prices = FoldingNavigationFixtures.prices();
        MathExpression expression = MathExpression.compile("prices[0]", pricesEnv(prices));
        prices.resetAccessCount();

        expression.compute(Map.of("prices", prices));
        expression.compute(Map.of("prices", prices));
        expression.compute(Map.of("prices", prices));

        assertThat(prices.accessCount()).isGreaterThanOrEqualTo(3);
    }

    @Test
    @DisplayName("filters with runtime identifiers are kept in the runtime suffix")
    void filtersWithRuntimeDependencyAreNotFolded() {
        TrackedList<BigDecimal> prices = FoldingNavigationFixtures.prices();
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .registerExternalSymbol("PRICES", prices, false)
                .registerExternalSymbol("threshold", bd("10"), true)
                .build();
        MathExpression expression = MathExpression.compile("PRICES[?(@ > threshold)]..count()", environment);
        prices.resetAccessCount();

        expression.compute(Map.of("threshold", bd("10")));
        expression.compute(Map.of("threshold", bd("10")));

        assertThat(prices.accessCount()).isGreaterThanOrEqualTo(2);
    }

    @Test
    @DisplayName("deep scan remains a runtime barrier")
    void deepScanIsNotFolded() {
        TrackedList<Map<String, BigDecimal>> items = new TrackedList<>(
                java.util.List.of(Map.of("price", bd("5")), Map.of("price", bd("15"))));
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .registerExternalSymbol("ITEMS", items, false)
                .build();
        MathExpression expression = MathExpression.compile("ITEMS..ds(price)..sum()", environment);
        items.resetAccessCount();

        assertThat(expression.compute()).isEqualByComparingTo("20");
        assertThat(items.accessCount()).isPositive();
    }

    @Test
    @DisplayName("reflective methods without type hints remain in runtime")
    void reflectiveMethodsWithoutTypeHintsAreNotFolded() {
        CountingBox box = new CountingBox();
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .registerExternalSymbol("BOX", box, false)
                .build();
        MathExpression expression = MathExpression.compile("BOX.amount()", environment);
        box.resetCalls();

        expression.compute();
        expression.compute();

        assertThat(box.calls()).isEqualTo(2);
    }

    private static ExpressionEnvironment pricesEnv(TrackedList<BigDecimal> prices) {
        return ExpressionEnvironment.builder()
                .registerExternalSymbol("prices", prices, true)
                .build();
    }
}
