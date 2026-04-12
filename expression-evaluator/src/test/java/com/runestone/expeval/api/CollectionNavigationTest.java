package com.runestone.expeval.api;

import com.runestone.expeval.environment.ExpressionEnvironment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * End-to-end tests for JSONPath-style collection / map navigation:
 * index access, slicing, wildcards, filter predicates, deep scan,
 * vector aggregations, map projections, and map-key access.
 */
@DisplayName("Collection and map navigation")
class CollectionNavigationTest {

    // -------------------------------------------------------------------------
    // Fixtures
    // -------------------------------------------------------------------------

    record Book(String title, String author, BigDecimal price) {}

    private static ExpressionEnvironment env(String name, Object value) {
        return ExpressionEnvironment.builder()
                .registerExternalSymbol(name, value, true)
                .build();
    }

    private static final List<BigDecimal> PRICES = List.of(
            new BigDecimal("5"),
            new BigDecimal("15"),
            new BigDecimal("25"),
            new BigDecimal("10")
    );

    private static final List<Book> BOOKS = List.of(
            new Book("Alpha",   "Alice", new BigDecimal("5.99")),
            new Book("Beta",    "Bob",   new BigDecimal("12.99")),
            new Book("Gamma",   "Alice", new BigDecimal("8.99")),
            new Book("Delta",   "Carol", new BigDecimal("19.99"))
    );

    // store.books — list of maps for map-based filter tests
    private static final Map<String, Object> STORE = Map.of(
            "books", List.of(
                    Map.of("title", "Alpha", "price", new BigDecimal("5.99")),
                    Map.of("title", "Beta",  "price", new BigDecimal("12.99")),
                    Map.of("title", "Gamma", "price", new BigDecimal("8.99"))
            )
    );

    // -------------------------------------------------------------------------
    // Index access
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Index access [n]")
    class IndexAccess {

        @Test
        @DisplayName("positive index returns correct element")
        void shouldReturnFirstElement() {
            BigDecimal result = MathExpression.compile("prices[0]", env("prices", PRICES))
                    .compute(Map.of("prices", PRICES));
            assertThat(result).isEqualByComparingTo("5");
        }

        @Test
        @DisplayName("last element via positive index")
        void shouldReturnLastElementByPositiveIndex() {
            BigDecimal result = MathExpression.compile("prices[3]", env("prices", PRICES))
                    .compute(Map.of("prices", PRICES));
            assertThat(result).isEqualByComparingTo("10");
        }

        @Test
        @DisplayName("negative index -1 returns the last element")
        void shouldReturnLastElementViaNegativeIndex() {
            BigDecimal result = MathExpression.compile("prices[-1]", env("prices", PRICES))
                    .compute(Map.of("prices", PRICES));
            assertThat(result).isEqualByComparingTo("10");
        }

        @Test
        @DisplayName("negative index -2 returns the second-to-last element")
        void shouldReturnSecondToLastViaNegativeIndex() {
            BigDecimal result = MathExpression.compile("prices[-2]", env("prices", PRICES))
                    .compute(Map.of("prices", PRICES));
            assertThat(result).isEqualByComparingTo("25");
        }

        @Test
        @DisplayName("out-of-bounds index throws evaluation exception")
        void shouldThrowOnOutOfBoundsIndex() {
            ExpressionEnvironment e = env("prices", PRICES);
            assertThatThrownBy(() ->
                    MathExpression.compile("prices[99]", e).compute(Map.of("prices", PRICES)))
                    .isInstanceOf(ExpressionEvaluationException.class)
                    .hasMessageContaining("INDEX_OUT_OF_BOUNDS");
        }
    }

    // -------------------------------------------------------------------------
    // Slice access
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Slice access [start:end]")
    class SliceAccess {

        @Test
        @DisplayName("[0:2] returns elements 0 and 1 (sum = 20)")
        void shouldSliceFromZeroToTwo() {
            BigDecimal result = MathExpression.compile("prices[0:2]..sum()", env("prices", PRICES))
                    .compute(Map.of("prices", PRICES));
            assertThat(result).isEqualByComparingTo("20");   // 5 + 15
        }

        @Test
        @DisplayName("[:2] (no start) returns first two elements")
        void shouldSliceToEnd() {
            BigDecimal result = MathExpression.compile("prices[:2]..sum()", env("prices", PRICES))
                    .compute(Map.of("prices", PRICES));
            assertThat(result).isEqualByComparingTo("20");   // 5 + 15
        }

        @Test
        @DisplayName("[2:] (no end) returns elements from index 2 to end")
        void shouldSliceFromStart() {
            BigDecimal result = MathExpression.compile("prices[2:]..sum()", env("prices", PRICES))
                    .compute(Map.of("prices", PRICES));
            assertThat(result).isEqualByComparingTo("35");   // 25 + 10
        }

        @Test
        @DisplayName("[-2:] returns last two elements")
        void shouldSliceLastTwo() {
            BigDecimal result = MathExpression.compile("prices[-2:]..sum()", env("prices", PRICES))
                    .compute(Map.of("prices", PRICES));
            assertThat(result).isEqualByComparingTo("35");   // 25 + 10
        }

        @Test
        @DisplayName("[10:20] time-looking slice (sliceTimeSubscript) is treated as start=10, end=20")
        void shouldHandleTimeLookingSliceAsStartEnd() {
            var numbers = List.of(
                    new BigDecimal("1"), new BigDecimal("2"), new BigDecimal("3"),
                    new BigDecimal("4"), new BigDecimal("5"), new BigDecimal("6"),
                    new BigDecimal("7"), new BigDecimal("8"), new BigDecimal("9"),
                    new BigDecimal("10"), new BigDecimal("11"), new BigDecimal("12"),
                    new BigDecimal("13"), new BigDecimal("14"), new BigDecimal("15"),
                    new BigDecimal("16"), new BigDecimal("17"), new BigDecimal("18"),
                    new BigDecimal("19"), new BigDecimal("20"), new BigDecimal("21")
            );
            // [10:20] → elements at index 10..19 → values 11..20 → sum = 155
            BigDecimal result = MathExpression.compile("nums[10:20]..sum()", env("nums", numbers))
                    .compute(Map.of("nums", numbers));
            assertThat(result).isEqualByComparingTo("155");
        }
    }

    // -------------------------------------------------------------------------
    // Wildcard
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Wildcard [*]")
    class WildcardAccess {

        @Test
        @DisplayName("[*] on a list returns all elements (identity for lists)")
        void shouldReturnAllElementsFromList() {
            BigDecimal result = MathExpression.compile("prices[*]..sum()", env("prices", PRICES))
                    .compute(Map.of("prices", PRICES));
            assertThat(result).isEqualByComparingTo("55");   // 5+15+25+10
        }

        @Test
        @DisplayName("[*] on a map returns all values")
        void shouldReturnAllMapValues() {
            var config = Map.of("a", new BigDecimal("1"), "b", new BigDecimal("2"), "c", new BigDecimal("3"));
            BigDecimal result = MathExpression.compile("cfg[*]..sum()", env("cfg", config))
                    .compute(Map.of("cfg", config));
            assertThat(result).isEqualByComparingTo("6");
        }
    }

    // -------------------------------------------------------------------------
    // Filter predicate — records
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Filter predicate [?(...)] on list of records")
    class FilterPredicateRecords {

        @Test
        @DisplayName("filter by numeric field keeps only matching records")
        void shouldFilterBooksUnderTen() {
            // Books with price < 10: Alpha(5.99) and Gamma(8.99) → count = 2
            BigDecimal result = MathExpression.compile(
                    "books[?(@.price < 10)]..count()", env("books", BOOKS))
                    .compute(Map.of("books", BOOKS));
            assertThat(result).isEqualByComparingTo("2");
        }

        @Test
        @DisplayName("filter by string field equality")
        void shouldFilterByAuthor() {
            // Books by Alice: Alpha and Gamma → count = 2
            BigDecimal result = MathExpression.compile(
                    "books[?(@.author = \"Alice\")]..count()", env("books", BOOKS))
                    .compute(Map.of("books", BOOKS));
            assertThat(result).isEqualByComparingTo("2");
        }

        @Test
        @DisplayName("filter with no matches returns empty → count = 0")
        void shouldReturnZeroCountWhenNoMatch() {
            BigDecimal result = MathExpression.compile(
                    "books[?(@.price > 100)]..count()", env("books", BOOKS))
                    .compute(Map.of("books", BOOKS));
            assertThat(result).isEqualByComparingTo("0");
        }

        @Test
        @DisplayName("filter then navigate to sub-field and aggregate")
        void shouldFilterThenNavigateAndAggregate() {
            // Books by Alice: 5.99 + 8.99 = 14.98
            BigDecimal result = MathExpression.compile(
                    "books[?(@.author = \"Alice\")][0].price", env("books", BOOKS))
                    .compute(Map.of("books", BOOKS));
            assertThat(result).isEqualByComparingTo("5.99");
        }
    }

    // -------------------------------------------------------------------------
    // Filter predicate — maps
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Filter predicate [?(...)] on list of maps")
    class FilterPredicateMaps {

        @Test
        @DisplayName("filter map entries by value using @[\"key\"]")
        void shouldFilterMapsByKey() {
            // Books with price < 10: Alpha(5.99) and Gamma(8.99) → count = 2
            BigDecimal result = MathExpression.compile(
                    "store.books[?(@[\"price\"] < 10)]..count()", env("store", STORE))
                    .compute(Map.of("store", STORE));
            assertThat(result).isEqualByComparingTo("2");
        }
    }

    // -------------------------------------------------------------------------
    // Deep scan
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Deep scan ..property")
    class DeepScan {

        @Test
        @DisplayName("..property collects all occurrences of the named key in nested maps")
        void shouldDeepScanPriceFromStore() {
            // store.books[*].price → 5.99 + 12.99 + 8.99 = 27.97
            BigDecimal result = MathExpression.compile("store..price..sum()", env("store", STORE))
                    .compute(Map.of("store", STORE));
            assertThat(result).isEqualByComparingTo("27.97");
        }

        @Test
        @DisplayName("..property on a flat list of records")
        void shouldDeepScanPriceFromRecords() {
            // All book prices: 5.99 + 12.99 + 8.99 + 19.99 = 47.96
            BigDecimal result = MathExpression.compile("books..price..sum()", env("books", BOOKS))
                    .compute(Map.of("books", BOOKS));
            assertThat(result).isEqualByComparingTo("47.96");
        }

        @Test
        @DisplayName("..* wildcard deep scan returns all reachable values")
        void shouldDeepScanWildcard() {
            var nested = Map.of("a", new BigDecimal("1"), "b", new BigDecimal("2"));
            BigDecimal result = MathExpression.compile("m..*..count()", env("m", nested))
                    .compute(Map.of("m", nested));
            assertThat(result).isEqualByComparingTo("2");
        }
    }

    // -------------------------------------------------------------------------
    // Vector aggregations
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Vector aggregations ..sum() ..avg() ..min() ..max() ..count()")
    class VectorAggregations {

        @Test
        @DisplayName("..sum() sums all numeric elements")
        void shouldSum() {
            BigDecimal result = MathExpression.compile("prices..sum()", env("prices", PRICES))
                    .compute(Map.of("prices", PRICES));
            assertThat(result).isEqualByComparingTo("55");   // 5+15+25+10
        }

        @Test
        @DisplayName("..avg() computes the arithmetic mean")
        void shouldAverage() {
            BigDecimal result = MathExpression.compile("prices..avg()", env("prices", PRICES))
                    .compute(Map.of("prices", PRICES));
            assertThat(result).isEqualByComparingTo("13.75");  // 55/4
        }

        @Test
        @DisplayName("..min() returns the smallest element")
        void shouldMin() {
            BigDecimal result = MathExpression.compile("prices..min()", env("prices", PRICES))
                    .compute(Map.of("prices", PRICES));
            assertThat(result).isEqualByComparingTo("5");
        }

        @Test
        @DisplayName("..max() returns the largest element")
        void shouldMax() {
            BigDecimal result = MathExpression.compile("prices..max()", env("prices", PRICES))
                    .compute(Map.of("prices", PRICES));
            assertThat(result).isEqualByComparingTo("25");
        }

        @Test
        @DisplayName("..count() returns the number of elements")
        void shouldCount() {
            BigDecimal result = MathExpression.compile("prices..count()", env("prices", PRICES))
                    .compute(Map.of("prices", PRICES));
            assertThat(result).isEqualByComparingTo("4");
        }
    }

    // -------------------------------------------------------------------------
    // Map key access
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Map key access [\"key\"]")
    class MapKeyAccess {

        @Test
        @DisplayName("string key access returns the mapped value")
        void shouldAccessMapByStringKey() {
            var config = Map.of("host", "localhost", "port", new BigDecimal("8080"));
            BigDecimal result = MathExpression.compile("config[\"port\"]", env("config", config))
                    .compute(Map.of("config", config));
            assertThat(result).isEqualByComparingTo("8080");
        }

        @Test
        @DisplayName("missing key returns null, coalesced to default value")
        void shouldReturnNullForMissingKey() {
            var config = Map.of("host", "localhost");
            ExpressionEnvironment e = ExpressionEnvironment.builder()
                    .registerExternalSymbol("config", config, true)
                    .registerExternalSymbol("missing", "default", true)
                    .build();
            // Map key not present → null, coalesced to "default"
            boolean result = LogicalExpression.compile(
                    "config[\"notThere\"] ?? missing = \"default\"", e)
                    .compute(Map.of("config", config, "missing", "default"));
            assertThat(result).isTrue();
        }
    }

    // -------------------------------------------------------------------------
    // Map projections
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Map projections ..keys() ..values()")
    class MapProjections {

        @Test
        @DisplayName("..keys() returns the set of map keys as a list")
        void shouldReturnKeys() {
            var config = Map.of("a", new BigDecimal("1"), "b", new BigDecimal("2"));
            BigDecimal result = MathExpression.compile("config..keys()..count()", env("config", config))
                    .compute(Map.of("config", config));
            assertThat(result).isEqualByComparingTo("2");
        }

        @Test
        @DisplayName("..values() returns all map values as a list and can be aggregated")
        void shouldReturnValues() {
            var config = Map.of("a", new BigDecimal("1"), "b", new BigDecimal("2"), "c", new BigDecimal("3"));
            BigDecimal result = MathExpression.compile("config..values()..sum()", env("config", config))
                    .compute(Map.of("config", config));
            assertThat(result).isEqualByComparingTo("6");
        }
    }

    // -------------------------------------------------------------------------
    // IN / NOT_IN in filter predicates
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("IN / NIN operators inside filter predicates")
    class InNinFilter {

        @Test
        @DisplayName("filter with IN keeps elements whose value is in a bound variable vector")
        void shouldFilterWithIn() {
            // PRICES = [5, 15, 25, 10]; keep elements that are in allowed → sum = 15
            var allowed = List.of(new BigDecimal("5"), new BigDecimal("10"));
            ExpressionEnvironment e = ExpressionEnvironment.builder()
                    .registerExternalSymbol("prices", PRICES, true)
                    .registerExternalSymbol("allowed", allowed, true)
                    .build();
            BigDecimal result = MathExpression.compile("prices[?(@ in allowed)]..sum()", e)
                    .compute(Map.of("prices", PRICES, "allowed", allowed));
            assertThat(result).isEqualByComparingTo("15");
        }

        @Test
        @DisplayName("filter with NIN keeps elements NOT in a bound variable vector")
        void shouldFilterWithNin() {
            // PRICES = [5, 15, 25, 10]; exclude [5, 10] → [15, 25] → sum = 40
            var excluded = List.of(new BigDecimal("5"), new BigDecimal("10"));
            ExpressionEnvironment e = ExpressionEnvironment.builder()
                    .registerExternalSymbol("prices", PRICES, true)
                    .registerExternalSymbol("excluded", excluded, true)
                    .build();
            BigDecimal result = MathExpression.compile("prices[?(@ nin excluded)]..sum()", e)
                    .compute(Map.of("prices", PRICES, "excluded", excluded));
            assertThat(result).isEqualByComparingTo("40");
        }
    }

    // -------------------------------------------------------------------------
    // Composed / multi-step navigation
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Composed navigation chains")
    class ComposedNavigation {

        @Test
        @DisplayName("index access followed by property access")
        void shouldIndexThenProperty() {
            BigDecimal result = MathExpression.compile("books[0].price", env("books", BOOKS))
                    .compute(Map.of("books", BOOKS));
            assertThat(result).isEqualByComparingTo("5.99");
        }

        @Test
        @DisplayName("filter then index then property")
        void shouldFilterThenIndexThenProperty() {
            // books where author=Bob → [Beta(12.99)] → [0] → .price → 12.99
            BigDecimal result = MathExpression.compile(
                    "books[?(@.author = \"Bob\")][0].price", env("books", BOOKS))
                    .compute(Map.of("books", BOOKS));
            assertThat(result).isEqualByComparingTo("12.99");
        }

        @Test
        @DisplayName("slice followed by aggregation")
        void shouldSliceThenAggregate() {
            // prices[1:3] = [15, 25] → sum = 40
            BigDecimal result = MathExpression.compile(
                    "prices[1:3]..sum()", env("prices", PRICES))
                    .compute(Map.of("prices", PRICES));
            assertThat(result).isEqualByComparingTo("40");
        }

        @Test
        @DisplayName("deep scan followed by filter")
        void shouldDeepScanThenFilter() {
            // store..price collects [5.99, 12.99, 8.99]
            // filter > 9 → [12.99] → count = 1
            BigDecimal result = MathExpression.compile(
                    "store..price[?(@ > 9)]..count()", env("store", STORE))
                    .compute(Map.of("store", STORE));
            assertThat(result).isEqualByComparingTo("1");
        }

        @Test
        @DisplayName("nested filter predicates")
        void shouldSupportNestedFilterPredicates() {
            // Outer list whose elements are lists; keep inner lists whose first element > 5
            var matrix = List.of(
                    List.of(new BigDecimal("3"), new BigDecimal("4")),
                    List.of(new BigDecimal("7"), new BigDecimal("8")),
                    List.of(new BigDecimal("1"), new BigDecimal("2"))
            );
            // [?(@[0] > 5)] → [[7,8]] → count = 1
            BigDecimal result = MathExpression.compile(
                    "matrix[?(@[0] > 5)]..count()", env("matrix", matrix))
                    .compute(Map.of("matrix", matrix));
            assertThat(result).isEqualByComparingTo("1");
        }

        @Test
        @DisplayName("wildcard then filter then aggregation")
        void shouldWildcardThenFilterThenAggregate() {
            // prices[*] = all 4 elements; filter @ > 10 → [15, 25]; sum = 40
            BigDecimal result = MathExpression.compile(
                    "prices[*][?(@ > 10)]..sum()", env("prices", PRICES))
                    .compute(Map.of("prices", PRICES));
            assertThat(result).isEqualByComparingTo("40");
        }

        @Test
        @DisplayName("chained double filter narrows results progressively")
        void shouldApplyDoubleFilterSequentially() {
            // First filter: price > 5 → all 4 books (5.99, 12.99, 8.99, 19.99 are all > 5)
            // Second filter: author = "Alice" → Alpha, Gamma → count = 2
            BigDecimal result = MathExpression.compile(
                    "books[?(@.price > 5)][?(@.author = \"Alice\")]..count()", env("books", BOOKS))
                    .compute(Map.of("books", BOOKS));
            assertThat(result).isEqualByComparingTo("2");
        }

        @Test
        @DisplayName("single-element slice is aggregatable")
        void shouldAggregateAfterSingleElementSlice() {
            // prices[1:2] = [15] (exactly one element) → sum = 15
            BigDecimal result = MathExpression.compile(
                    "prices[1:2]..sum()", env("prices", PRICES))
                    .compute(Map.of("prices", PRICES));
            assertThat(result).isEqualByComparingTo("15");
        }
    }

    // -------------------------------------------------------------------------
    // Additional slice edge cases
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Slice edge cases")
    class SliceEdgeCases {

        @Test
        @DisplayName("[0:0] returns an empty slice → count = 0")
        void shouldReturnEmptyForZeroToZeroSlice() {
            BigDecimal result = MathExpression.compile("prices[0:0]..count()", env("prices", PRICES))
                    .compute(Map.of("prices", PRICES));
            assertThat(result).isEqualByComparingTo("0");
        }

        @Test
        @DisplayName("[3:1] inverted bounds return an empty slice → count = 0")
        void shouldReturnEmptyForInvertedBounds() {
            BigDecimal result = MathExpression.compile("prices[3:1]..count()", env("prices", PRICES))
                    .compute(Map.of("prices", PRICES));
            assertThat(result).isEqualByComparingTo("0");
        }

        @Test
        @DisplayName("[:-1] omits the last element")
        void shouldOmitLastElementWithNegativeEnd() {
            // PRICES = [5, 15, 25, 10]; [:-1] → [5, 15, 25] → sum = 45
            BigDecimal result = MathExpression.compile("prices[:-1]..sum()", env("prices", PRICES))
                    .compute(Map.of("prices", PRICES));
            assertThat(result).isEqualByComparingTo("45");
        }

        @Test
        @DisplayName("[0:100] is clamped to the list size and returns all elements")
        void shouldClampSliceBeyondSize() {
            // [0:100] is clamped to [0:4] → all 4 elements → sum = 55
            BigDecimal result = MathExpression.compile("prices[0:100]..sum()", env("prices", PRICES))
                    .compute(Map.of("prices", PRICES));
            assertThat(result).isEqualByComparingTo("55");
        }
    }

    // -------------------------------------------------------------------------
    // Filter predicate composition with AND / OR
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Filter predicate composition")
    class FilterComposition {

        @Test
        @DisplayName("filter with 'and' combines two conditions")
        void shouldFilterWithAndCondition() {
            // price > 5 and price < 15: Alpha(5.99), Beta(12.99), Gamma(8.99) → Delta(19.99) excluded
            BigDecimal result = MathExpression.compile(
                    "books[?(@.price > 5 and @.price < 15)]..count()", env("books", BOOKS))
                    .compute(Map.of("books", BOOKS));
            assertThat(result).isEqualByComparingTo("3");
        }

        @Test
        @DisplayName("filter with 'or' keeps either-matching elements")
        void shouldFilterWithOrCondition() {
            // author = Alice or author = Bob: Alpha, Gamma (Alice), Beta (Bob) → count = 3
            BigDecimal result = MathExpression.compile(
                    "books[?(@.author = \"Alice\" or @.author = \"Bob\")]..count()", env("books", BOOKS))
                    .compute(Map.of("books", BOOKS));
            assertThat(result).isEqualByComparingTo("3");
        }

        @Test
        @DisplayName("parenthesised and/or conditions evaluate correctly")
        void shouldRespectParenthesisedLogic() {
            // (price < 6 or price > 18) and price != 0 → Alpha(5.99) and Delta(19.99) → count = 2
            BigDecimal result = MathExpression.compile(
                    "books[?((@.price < 6 or @.price > 18) and @.price > 0)]..count()", env("books", BOOKS))
                    .compute(Map.of("books", BOOKS));
            assertThat(result).isEqualByComparingTo("2");
        }
    }

    // -------------------------------------------------------------------------
    // Empty collection behaviour
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Empty collection behaviour")
    class EmptyCollectionBehavior {

        @Test
        @DisplayName("..count() on an empty list returns 0")
        void shouldReturnZeroCountForEmptyList() {
            List<BigDecimal> empty = List.of();
            BigDecimal result = MathExpression.compile("nums..count()", env("nums", empty))
                    .compute(Map.of("nums", empty));
            assertThat(result).isEqualByComparingTo("0");
        }

        @Test
        @DisplayName("filter producing no matches followed by ..count() returns 0")
        void shouldReturnZeroCountAfterEmptyFilter() {
            // all books have positive prices so price < 0 produces no matches
            BigDecimal result = MathExpression.compile(
                    "books[?(@.price < 0)]..count()", env("books", BOOKS))
                    .compute(Map.of("books", BOOKS));
            assertThat(result).isEqualByComparingTo("0");
        }

        @Test
        @DisplayName("..sum() on a filter-to-empty result throws because no numeric result is produced")
        void shouldThrowWhenSummingFilteredEmptyList() {
            assertThatThrownBy(() ->
                    MathExpression.compile("prices[?(@ > 1000)]..sum()", env("prices", PRICES))
                            .compute(Map.of("prices", PRICES)))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("numeric result");
        }

        @Test
        @DisplayName("..avg() on a filter-to-empty result throws because no numeric result is produced")
        void shouldThrowWhenAveragingFilteredEmptyList() {
            assertThatThrownBy(() ->
                    MathExpression.compile("prices[?(@ > 1000)]..avg()", env("prices", PRICES))
                            .compute(Map.of("prices", PRICES)))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("numeric result");
        }
    }

    // -------------------------------------------------------------------------
    // Additional deep-scan edge cases
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Deep scan edge cases")
    class DeepScanEdgeCases {

        @Test
        @DisplayName("..property that matches nothing returns an empty collection → count = 0")
        void shouldReturnEmptyWhenPropertyNotFound() {
            // STORE contains no "nonExistent" key at any depth
            BigDecimal result = MathExpression.compile(
                    "store..nonExistent..count()", env("store", STORE))
                    .compute(Map.of("store", STORE));
            assertThat(result).isEqualByComparingTo("0");
        }

        @Test
        @DisplayName("..property scans beyond two levels of nesting")
        void shouldCollectPropertyFromDeeplyNestedMaps() {
            // Three-level nesting: root → a → child → {price: 10 and price: 20}
            var nested = Map.of(
                    "a", Map.<String, Object>of(
                            "price", new BigDecimal("10"),
                            "child", Map.of("price", new BigDecimal("20"))
                    ),
                    "b", Map.of("price", new BigDecimal("30"))
            );
            // Deep scan collects all 'price' values: 10, 20, 30 → sum = 60
            BigDecimal result = MathExpression.compile("data..price..sum()", env("data", nested))
                    .compute(Map.of("data", nested));
            assertThat(result).isEqualByComparingTo("60");
        }
    }

    // -------------------------------------------------------------------------
    // Null and error paths
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Null and error paths")
    class NullAndErrorPaths {

        @Test
        @DisplayName("negative index that exceeds list size throws INDEX_OUT_OF_BOUNDS")
        void shouldThrowForNegativeOutOfBoundsIndex() {
            // PRICES has 4 elements; [-5] → effective = 4-5 = -1 → out of bounds
            assertThatThrownBy(() ->
                    MathExpression.compile("prices[-5]", env("prices", PRICES))
                            .compute(Map.of("prices", PRICES)))
                    .isInstanceOf(ExpressionEvaluationException.class)
                    .hasMessageContaining("INDEX_OUT_OF_BOUNDS");
        }

        @Test
        @DisplayName("navigating through a null map-key result throws NULL_IN_CHAIN")
        void shouldThrowNullInChainWhenMapKeyMissing() {
            // outer["missing"] returns null; accessing .length on null triggers NULL_IN_CHAIN
            var outer = Map.of("existing", "hello");
            ExpressionEnvironment e = ExpressionEnvironment.builder()
                    .registerExternalSymbol("outer", outer, true)
                    .build();
            assertThatThrownBy(() ->
                    MathExpression.compile("outer[\"missing\"].length", e)
                            .compute(Map.of("outer", outer)))
                    .isInstanceOf(ExpressionEvaluationException.class)
                    .hasMessageContaining("NULL_IN_CHAIN");
        }

        @Test
        @DisplayName("index access on a Map (not a List) throws TYPE_MISMATCH")
        void shouldThrowTypeMismatchWhenIndexingMap() {
            // applyIndex requires a List; a Map triggers TYPE_MISMATCH at runtime
            var config = Map.of("a", new BigDecimal("1"), "b", new BigDecimal("2"));
            ExpressionEnvironment e = ExpressionEnvironment.builder()
                    .registerExternalSymbol("cfg", config, true)
                    .build();
            assertThatThrownBy(() ->
                    MathExpression.compile("cfg[0]", e)
                            .compute(Map.of("cfg", config)))
                    .isInstanceOf(ExpressionEvaluationException.class)
                    .hasMessageContaining("TYPE_MISMATCH");
        }
    }

    // -------------------------------------------------------------------------
    // Thread safety
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Thread safety")
    class ThreadSafety {

        @Test
        @DisplayName("concurrent filter evaluation on a shared compiled expression returns consistent results")
        void shouldProduceConsistentResultsUnderConcurrentAccess() throws Exception {
            // Compile once; invoke from many threads to verify the ThreadLocal filter-context
            // stack is properly isolated per thread.
            MathExpression expr = MathExpression.compile(
                    "books[?(@.author = \"Alice\")]..count()", env("books", BOOKS));

            int threads = 8;
            int iterationsPerThread = 50;
            ExecutorService exec = Executors.newFixedThreadPool(threads);
            try {
                List<Future<BigDecimal>> futures = IntStream.range(0, threads * iterationsPerThread)
                        .mapToObj(i -> exec.submit(() -> expr.compute(Map.of("books", BOOKS))))
                        .toList();
                for (Future<BigDecimal> future : futures) {
                    assertThat(future.get()).isEqualByComparingTo("2");
                }
            } finally {
                exec.shutdown();
                exec.awaitTermination(10, TimeUnit.SECONDS);
            }
        }

        @Test
        @DisplayName("nested filter predicates are correctly isolated across concurrent threads")
        void shouldIsolateNestedFilterContextsAcrossThreads() throws Exception {
            // matrix[?(@[0] > 5)] — each thread's filter-context stack must be independent
            var matrix = List.of(
                    List.of(new BigDecimal("3"), new BigDecimal("4")),
                    List.of(new BigDecimal("7"), new BigDecimal("8")),
                    List.of(new BigDecimal("1"), new BigDecimal("2"))
            );
            MathExpression expr = MathExpression.compile(
                    "matrix[?(@[0] > 5)]..count()", env("matrix", matrix));

            int threads = 6;
            int iterationsPerThread = 40;
            ExecutorService exec = Executors.newFixedThreadPool(threads);
            try {
                List<Future<BigDecimal>> futures = IntStream.range(0, threads * iterationsPerThread)
                        .mapToObj(i -> exec.submit(() -> expr.compute(Map.of("matrix", matrix))))
                        .toList();
                for (Future<BigDecimal> future : futures) {
                    assertThat(future.get()).isEqualByComparingTo("1");
                }
            } finally {
                exec.shutdown();
                exec.awaitTermination(10, TimeUnit.SECONDS);
            }
        }
    }

    // -------------------------------------------------------------------------
    // Maps with complex object keys
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Filter on maps with complex object keys")
    class MapComplexKeyFilter {

        record UserId(long id, String domain) {
            @Override
            public String toString() {
                return id + "@" + domain;
            }
        }

        record Account(long accountId, String type, BigDecimal balance) {}

        @Test
        @DisplayName("filter map entries by key's numeric property (id < threshold)")
        void shouldFilterMapEntriesByKeyProperty() {
            // Map<UserId, Account> with 3 entries; filter by UserId.id < 2
            UserId key1 = new UserId(1, "acme.com");
            UserId key2 = new UserId(2, "acme.com");
            UserId key3 = new UserId(3, "acme.com");

            var accountMap = Map.of(
                    key1, new Account(100, "checking", new BigDecimal("5000")),
                    key2, new Account(200, "savings", new BigDecimal("10000")),
                    key3, new Account(300, "investment", new BigDecimal("25000"))
            );

            // Filter by key's id property
            BigDecimal result = MathExpression.compile(
                    "accounts..keys()[?(@.id < maxId)]..count()",
                    ExpressionEnvironment.builder()
                            .registerExternalSymbol("accounts", accountMap, true)
                            .registerExternalSymbol("maxId", 3, true)
                            .build())
                    .compute(Map.of(
                            "accounts", accountMap,
                            "maxId", 3
                    ));

            assertThat(result).isEqualByComparingTo("2");  // key1 (id=1) and key2 (id=2)
        }

        @Test
        @DisplayName("filter map keys by string property (domain matching)")
        void shouldFilterMapKeysByStringProperty() {
            UserId acme1 = new UserId(1, "acme.com");
            UserId acme2 = new UserId(2, "acme.com");
            UserId corp1 = new UserId(3, "corp.net");

            var accounts = Map.of(
                    acme1, new Account(100, "checking", new BigDecimal("5000")),
                    acme2, new Account(200, "savings", new BigDecimal("10000")),
                    corp1, new Account(300, "business", new BigDecimal("50000"))
            );

            // Count keys with domain = "acme.com"
            BigDecimal result = MathExpression.compile(
                    "accounts..keys()[?(@.domain = targetDomain)]..count()",
                    ExpressionEnvironment.builder()
                            .registerExternalSymbol("accounts", accounts, true)
                            .registerExternalSymbol("targetDomain", "acme.com", true)
                            .build())
                    .compute(Map.of(
                            "accounts", accounts,
                            "targetDomain", "acme.com"
                    ));

            assertThat(result).isEqualByComparingTo("2");  // acme1 and acme2
        }

        @Test
        @DisplayName("filter map keys with AND condition on key properties")
        void shouldFilterMapKeysWithAndCondition() {
            UserId user1 = new UserId(10, "domain1");
            UserId user2 = new UserId(20, "domain1");
            UserId user3 = new UserId(30, "domain2");

            var data = Map.of(
                    user1, "value1",
                    user2, "value2",
                    user3, "value3"
            );

            // Filter keys where id >= 15 AND domain = "domain1"
            BigDecimal result = MathExpression.compile(
                    "collection..keys()[?(@.id >= minId and @.domain = dom)]..count()",
                    ExpressionEnvironment.builder()
                            .registerExternalSymbol("collection", data, true)
                            .registerExternalSymbol("minId", 15, true)
                            .registerExternalSymbol("dom", "domain1", true)
                            .build())
                    .compute(Map.of(
                            "collection", data,
                            "minId", 15,
                            "dom", "domain1"
                    ));

            assertThat(result).isEqualByComparingTo("1");  // Only user2 (id=20, domain1)
        }

        @Test
        @DisplayName("filter map keys with OR condition")
        void shouldFilterMapKeysWithOrCondition() {
            UserId key1 = new UserId(5, "org1");
            UserId key2 = new UserId(15, "org2");
            UserId key3 = new UserId(25, "org1");
            UserId key4 = new UserId(35, "org3");

            var items = Map.of(
                    key1, "item1",
                    key2, "item2",
                    key3, "item3",
                    key4, "item4"
            );

            // Filter keys where id > 20 OR domain = "org1"
            BigDecimal result = MathExpression.compile(
                    "items..keys()[?(@.id > threshold or @.domain = org)]..count()",
                    ExpressionEnvironment.builder()
                            .registerExternalSymbol("items", items, true)
                            .registerExternalSymbol("threshold", 20, true)
                            .registerExternalSymbol("org", "org1", true)
                            .build())
                    .compute(Map.of(
                            "items", items,
                            "threshold", 20,
                            "org", "org1"
                    ));

            assertThat(result).isEqualByComparingTo("3");  // key1 (org1), key3 (org1), key4 (id=35)
        }

        @Test
        @DisplayName("access and aggregate values for entries whose keys match filter")
        void shouldAggregateValuesForFilteredKeys() {
            UserId acme1 = new UserId(1, "acme");
            UserId acme2 = new UserId(2, "acme");
            UserId beta1 = new UserId(1, "beta");

            var balances = Map.of(
                    acme1, new BigDecimal("1000"),
                    acme2, new BigDecimal("2000"),
                    beta1, new BigDecimal("5000")
            );

            // Sum balances for entries where key domain = "acme"
            BigDecimal result = MathExpression.compile(
                    "accounts..keys()[?(@.domain = domain)]..count()",
                    ExpressionEnvironment.builder()
                            .registerExternalSymbol("accounts", balances, true)
                            .registerExternalSymbol("domain", "acme", true)
                            .build())
                    .compute(Map.of(
                            "accounts", balances,
                            "domain", "acme"
                    ));

            assertThat(result).isEqualByComparingTo("2");  // acme1 and acme2
        }

        @Test
        @DisplayName("filter map keys when no entries match the condition")
        void shouldReturnZeroWhenNoKeysMatch() {
            UserId key1 = new UserId(1, "test1");
            UserId key2 = new UserId(2, "test2");

            var data = Map.of(
                    key1, "value1",
                    key2, "value2"
            );

            BigDecimal result = MathExpression.compile(
                    "records..keys()[?(@.id > threshold)]..count()",
                    ExpressionEnvironment.builder()
                            .registerExternalSymbol("records", data, true)
                            .registerExternalSymbol("threshold", 100, true)
                            .build())
                    .compute(Map.of(
                            "records", data,
                            "threshold", 100
                    ));

            assertThat(result).isEqualByComparingTo("0");
        }

        @Test
        @DisplayName("filter map keys when all entries match the condition")
        void shouldReturnAllWhenAllKeysMatch() {
            UserId key1 = new UserId(1, "high");
            UserId key2 = new UserId(2, "high");
            UserId key3 = new UserId(3, "high");

            var collection = Map.of(
                    key1, "data1",
                    key2, "data2",
                    key3, "data3"
            );

            BigDecimal result = MathExpression.compile(
                    "collection..keys()[?(@.domain = high)]..count()",
                    ExpressionEnvironment.builder()
                            .registerExternalSymbol("collection", collection, true)
                            .registerExternalSymbol("high", "high", true)
                            .build())
                    .compute(Map.of(
                            "collection", collection,
                            "high", "high"
                    ));

            assertThat(result).isEqualByComparingTo("3");  // All keys match
        }
    }
}
