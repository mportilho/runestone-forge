//package com.runestone.expeval.api;
//
//import com.runestone.expeval.environment.ExpressionEnvironment;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Nested;
//import org.junit.jupiter.api.Test;
//
//import java.math.BigDecimal;
//import java.util.ArrayList;
//import java.util.Iterator;
//import java.util.List;
//import java.util.Map;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
///**
// * TDD specification for full-chain constant folding of collection navigation on non-overridable
// * external symbols.
// *
// * <p>Functional tests (correct result assertions) pass with the current root-only folding
// * implementation. Folding tests (accessCount == 0 after compile) are RED until the full-chain
// * folding feature is implemented: when all inputs of a navigation chain are compile-time constants,
// * the entire chain must be evaluated once at compile time and stored as {@code ExecutableLiteral}.
// */
//@DisplayName("Collection navigation on folded (non-overridable) external symbols")
//class FoldedSymbolCollectionNavigationTest {
//
//    // ---- fixtures --------------------------------------------------------
//
//    static class TrackedList<T> extends ArrayList<T> {
//        int accessCount = 0;
//
//        TrackedList(List<T> items) { super(items); }
//
//        @Override public T get(int i) { accessCount++; return super.get(i); }
//        @Override public Iterator<T> iterator() { accessCount++; return super.iterator(); }
//        @Override public int size() { accessCount++; return super.size(); }
//        @Override public List<T> subList(int f, int t) { accessCount++; return super.subList(f, t); }
//    }
//
//    record Book(String title, String author, BigDecimal price) {}
//
//    record Catalog(String name, List<Book> books) {}
//
//    private static final List<BigDecimal> PRICE_VALUES = List.of(
//            bd("5"), bd("15"), bd("25"), bd("10"));
//
//    private static final List<Book> BOOK_VALUES = List.of(
//            new Book("Alpha", "Alice", bd("5.99")),
//            new Book("Beta",  "Bob",   bd("12.99")),
//            new Book("Gamma", "Alice", bd("8.99")),
//            new Book("Delta", "Carol", bd("19.99")));
//
//    private static BigDecimal bd(String s) { return new BigDecimal(s); }
//
//    private static TrackedList<BigDecimal> newPrices() { return new TrackedList<>(PRICE_VALUES); }
//    private static TrackedList<Book>       newBooks()  { return new TrackedList<>(BOOK_VALUES); }
//
//    private static ExpressionEnvironment pricesEnv(TrackedList<BigDecimal> prices) {
//        return ExpressionEnvironment.builder()
//                .registerExternalSymbol("PRICES", prices, false)
//                .build();
//    }
//
//    private static ExpressionEnvironment booksEnv(TrackedList<Book> books) {
//        return ExpressionEnvironment.builder()
//                .registerExternalSymbol("BOOKS", books, false)
//                .build();
//    }
//
//    private static ExpressionEnvironment catalogEnv(Catalog catalog) {
//        return ExpressionEnvironment.builder()
//                .registerExternalSymbol("CATALOG", catalog, false)
//                .registerTypeHint(Catalog.class)
//                .build();
//    }
//
//    // ---- [n] — index access ----------------------------------------------
//
//    @Nested
//    @DisplayName("[n] — index access on folded list")
//    class IndexAccessOnFoldedList {
//
//        @Test
//        @DisplayName("PRICES[0] returns 5")
//        void firstElement() {
//            BigDecimal result = MathExpression.compile("PRICES[0]", pricesEnv(newPrices())).compute();
//            assertThat(result).isEqualByComparingTo("5");
//        }
//
//        @Test
//        @DisplayName("PRICES[-1] returns 10 (last via negative index)")
//        void lastElementViaNegativeIndex() {
//            BigDecimal result = MathExpression.compile("PRICES[-1]", pricesEnv(newPrices())).compute();
//            assertThat(result).isEqualByComparingTo("10");
//        }
//
//        @Test
//        @DisplayName("PRICES[1] returns 15")
//        void secondElement() {
//            BigDecimal result = MathExpression.compile("PRICES[1]", pricesEnv(newPrices())).compute();
//            assertThat(result).isEqualByComparingTo("15");
//        }
//
//        @Test
//        @DisplayName("PRICES[0] folded at compile time — no list access during compute()")
//        void indexFoldedAtCompileTime() {
//            var prices = newPrices();
//            MathExpression expr = MathExpression.compile("PRICES[0]", pricesEnv(prices));
//            prices.accessCount = 0;
//
//            expr.compute(); expr.compute(); expr.compute();
//
//            assertThat(prices.accessCount).isEqualTo(0);
//        }
//    }
//
//    // ---- [start:end] — slice ---------------------------------------------
//
//    @Nested
//    @DisplayName("[start:end] — slice on folded list")
//    class SliceAccessOnFoldedList {
//
//        @Test
//        @DisplayName("PRICES[1:3]..sum() returns 40 (elements at index 1 and 2)")
//        void sliceMiddleSum() {
//            BigDecimal result = MathExpression.compile("PRICES[1:3]..sum()", pricesEnv(newPrices())).compute();
//            assertThat(result).isEqualByComparingTo("40");
//        }
//
//        @Test
//        @DisplayName("PRICES[:2]..sum() returns 20 (first two elements)")
//        void sliceFromStartSum() {
//            BigDecimal result = MathExpression.compile("PRICES[:2]..sum()", pricesEnv(newPrices())).compute();
//            assertThat(result).isEqualByComparingTo("20");
//        }
//
//        @Test
//        @DisplayName("PRICES[1:3]..sum() folded at compile time — no list access during compute()")
//        void sliceFoldedAtCompileTime() {
//            var prices = newPrices();
//            MathExpression expr = MathExpression.compile("PRICES[1:3]..sum()", pricesEnv(prices));
//            prices.accessCount = 0;
//
//            expr.compute(); expr.compute(); expr.compute();
//
//            assertThat(prices.accessCount).isEqualTo(0);
//        }
//    }
//
//    // ---- [*] — wildcard --------------------------------------------------
//
//    @Nested
//    @DisplayName("[*] — wildcard on folded list")
//    class WildcardOnFoldedList {
//
//        @Test
//        @DisplayName("PRICES[*]..sum() returns 55 (all elements)")
//        void wildcardSum() {
//            BigDecimal result = MathExpression.compile("PRICES[*]..sum()", pricesEnv(newPrices())).compute();
//            assertThat(result).isEqualByComparingTo("55");
//        }
//
//        @Test
//        @DisplayName("PRICES[*]..count() returns 4")
//        void wildcardCount() {
//            BigDecimal result = MathExpression.compile("PRICES[*]..count()", pricesEnv(newPrices())).compute();
//            assertThat(result).isEqualByComparingTo("4");
//        }
//
//        @Test
//        @DisplayName("PRICES[*]..sum() folded at compile time — no list access during compute()")
//        void wildcardFoldedAtCompileTime() {
//            var prices = newPrices();
//            MathExpression expr = MathExpression.compile("PRICES[*]..sum()", pricesEnv(prices));
//            prices.accessCount = 0;
//
//            expr.compute(); expr.compute(); expr.compute();
//
//            assertThat(prices.accessCount).isEqualTo(0);
//        }
//    }
//
//    // ---- [?(@ op literal)] — filter on scalar list -----------------------
//
//    @Nested
//    @DisplayName("[?(@ > x)] — filter predicate on folded scalar list")
//    class FilterPredicateOnFoldedScalarList {
//
//        @Test
//        @DisplayName("PRICES[?(@ > 10)]..count() returns 2 (elements 15 and 25)")
//        void filterGreaterThan() {
//            BigDecimal result = MathExpression.compile(
//                    "PRICES[?(@ > 10)]..count()", pricesEnv(newPrices())).compute();
//            assertThat(result).isEqualByComparingTo("2");
//        }
//
//        @Test
//        @DisplayName("PRICES[?(@ >= 5)]..count() returns 4 (all elements qualify)")
//        void filterAllQualify() {
//            BigDecimal result = MathExpression.compile(
//                    "PRICES[?(@ >= 5)]..count()", pricesEnv(newPrices())).compute();
//            assertThat(result).isEqualByComparingTo("4");
//        }
//
//        @Test
//        @DisplayName("PRICES[?(@ > 10)]..count() folded at compile time — no list access during compute()")
//        void filterFoldedAtCompileTime() {
//            var prices = newPrices();
//            MathExpression expr = MathExpression.compile(
//                    "PRICES[?(@ > 10)]..count()", pricesEnv(prices));
//            prices.accessCount = 0;
//
//            expr.compute(); expr.compute(); expr.compute();
//
//            assertThat(prices.accessCount).isEqualTo(0);
//        }
//
//        @Test
//        @DisplayName("filter with runtime dependency is NOT folded — list accessed on each compute()")
//        void filterWithRuntimeDependencyIsNotFolded() {
//            var prices = newPrices();
//            ExpressionEnvironment env = ExpressionEnvironment.builder()
//                    .registerExternalSymbol("PRICES",     prices,     false)
//                    .registerExternalSymbol("threshold",  bd("10"),   true)
//                    .build();
//
//            MathExpression expr = MathExpression.compile("PRICES[?(@ > threshold)]..count()", env);
//            prices.accessCount = 0;
//
//            int n = 3;
//            for (int i = 0; i < n; i++) {
//                expr.compute(Map.of("threshold", bd("10")));
//            }
//
//            assertThat(prices.accessCount).isGreaterThanOrEqualTo(n);
//        }
//    }
//
//    // ---- ..agg() — aggregation -------------------------------------------
//
//    @Nested
//    @DisplayName("..sum() ..avg() ..count() — aggregation on folded list")
//    class AggregationOnFoldedList {
//
//        @Test
//        @DisplayName("PRICES..sum() returns 55")
//        void sum() {
//            BigDecimal result = MathExpression.compile("PRICES..sum()", pricesEnv(newPrices())).compute();
//            assertThat(result).isEqualByComparingTo("55");
//        }
//
//        @Test
//        @DisplayName("PRICES..count() returns 4")
//        void count() {
//            BigDecimal result = MathExpression.compile("PRICES..count()", pricesEnv(newPrices())).compute();
//            assertThat(result).isEqualByComparingTo("4");
//        }
//
//        @Test
//        @DisplayName("PRICES..avg() returns 13.75")
//        void avg() {
//            BigDecimal result = MathExpression.compile("PRICES..avg()", pricesEnv(newPrices())).compute();
//            assertThat(result).isEqualByComparingTo("13.75");
//        }
//
//        @Test
//        @DisplayName("PRICES..sum() folded at compile time — no list access during compute()")
//        void aggregationFoldedAtCompileTime() {
//            var prices = newPrices();
//            MathExpression expr = MathExpression.compile("PRICES..sum()", pricesEnv(prices));
//            prices.accessCount = 0;
//
//            expr.compute(); expr.compute(); expr.compute();
//
//            assertThat(prices.accessCount).isEqualTo(0);
//        }
//    }
//
//    // ---- [?(@.field op x)] — filter on list of records ------------------
//
//    @Nested
//    @DisplayName("[?(@.field = x)] — filter on folded list of records")
//    class FilterOnFoldedListOfRecords {
//
//        @Test
//        @DisplayName("BOOKS[?(@.author = \"Alice\")]..count() returns 2")
//        void filterByAuthor() {
//            BigDecimal result = MathExpression.compile(
//                    "BOOKS[?(@.author = \"Alice\")]..count()", booksEnv(newBooks())).compute();
//            assertThat(result).isEqualByComparingTo("2");
//        }
//
//        @Test
//        @DisplayName("BOOKS[?(@.price > 10)]..count() returns 2 (Beta 12.99, Delta 19.99)")
//        void filterByPrice() {
//            BigDecimal result = MathExpression.compile(
//                    "BOOKS[?(@.price > 10)]..count()", booksEnv(newBooks())).compute();
//            assertThat(result).isEqualByComparingTo("2");
//        }
//
//        @Test
//        @DisplayName("BOOKS[?(@.author = \"Alice\")]..count() folded at compile time — no list access during compute()")
//        void filterOnRecordsFoldedAtCompileTime() {
//            var books = newBooks();
//            MathExpression expr = MathExpression.compile(
//                    "BOOKS[?(@.author = \"Alice\")]..count()", booksEnv(books));
//            books.accessCount = 0;
//
//            expr.compute(); expr.compute(); expr.compute();
//
//            assertThat(books.accessCount).isEqualTo(0);
//        }
//    }
//
//    // ---- [?(@.x)].field..agg() — filter + projection + aggregation ------
//
//    @Nested
//    @DisplayName("[?(@.x)].field..agg() — filter, projection, aggregation on folded list of records")
//    class ProjectionOnFoldedListOfRecords {
//
//        @Test
//        @DisplayName("BOOKS[?(@.author = \"Alice\")].price..sum() returns 14.98")
//        void filterProjectSum() {
//            BigDecimal result = MathExpression.compile(
//                    "BOOKS[?(@.author = \"Alice\")].price..sum()", booksEnv(newBooks())).compute();
//            assertThat(result).isEqualByComparingTo("14.98");
//        }
//
//        @Test
//        @DisplayName("BOOKS[*].title..count() returns 4 (all titles projected)")
//        void wildcardProjectCount() {
//            BigDecimal result = MathExpression.compile(
//                    "BOOKS[*].title..count()", booksEnv(newBooks())).compute();
//            assertThat(result).isEqualByComparingTo("4");
//        }
//
//        @Test
//        @DisplayName("BOOKS[?(@.author = \"Alice\")].price..sum() folded at compile time — no list access during compute()")
//        void filterProjectionFoldedAtCompileTime() {
//            var books = newBooks();
//            MathExpression expr = MathExpression.compile(
//                    "BOOKS[?(@.author = \"Alice\")].price..sum()", booksEnv(books));
//            books.accessCount = 0;
//
//            expr.compute(); expr.compute(); expr.compute();
//
//            assertThat(books.accessCount).isEqualTo(0);
//        }
//    }
//
//    // ---- catalog.books[n] — navigation into list field of folded object --
//
//    @Nested
//    @DisplayName("catalog.books[n] — navigation into list field of folded complex object")
//    class NestedObjectNavigation {
//
//        @Test
//        @DisplayName("CATALOG.books[0].title equals \"Alpha\"")
//        void firstBookTitle() {
//            var books = newBooks();
//            boolean result = LogicalExpression.compile(
//                    "CATALOG.books[0].title = \"Alpha\"",
//                    catalogEnv(new Catalog("shop", books))).compute();
//            assertThat(result).isTrue();
//        }
//
//        @Test
//        @DisplayName("CATALOG.books..count() returns 4")
//        void catalogBooksCount() {
//            var books = newBooks();
//            BigDecimal result = MathExpression.compile(
//                    "CATALOG.books..count()",
//                    catalogEnv(new Catalog("shop", books))).compute();
//            assertThat(result).isEqualByComparingTo("4");
//        }
//
//        @Test
//        @DisplayName("CATALOG.books[?(@.author = \"Alice\")].price..sum() returns 14.98")
//        void catalogFilterProjectSum() {
//            var books = newBooks();
//            BigDecimal result = MathExpression.compile(
//                    "CATALOG.books[?(@.author = \"Alice\")].price..sum()",
//                    catalogEnv(new Catalog("shop", books))).compute();
//            assertThat(result).isEqualByComparingTo("14.98");
//        }
//
//        @Test
//        @DisplayName("CATALOG.books[0].title folded at compile time — books list not accessed during compute()")
//        void catalogNavigationFoldedAtCompileTime() {
//            var books = newBooks();
//            Catalog catalog = new Catalog("shop", books);
//            LogicalExpression expr = LogicalExpression.compile(
//                    "CATALOG.books[0].title = \"Alpha\"", catalogEnv(catalog));
//            books.accessCount = 0;
//
//            expr.compute(); expr.compute(); expr.compute();
//
//            assertThat(books.accessCount).isEqualTo(0);
//        }
//    }
//
//    // ---- ["key"] — map key access on folded map --------------------------
//
//    @Nested
//    @DisplayName("[\"key\"] — map key access on folded map")
//    class MapNavigationOnFoldedMap {
//
//        @Test
//        @DisplayName("STORE[\"books\"][0][\"title\"] returns \"Alpha\"")
//        void mapKeyChainNavigation() {
//            var store = Map.of("books", List.of(
//                    Map.of("title", "Alpha", "price", bd("5.99")),
//                    Map.of("title", "Beta",  "price", bd("12.99"))
//            ));
//            ExpressionEnvironment env = ExpressionEnvironment.builder()
//                    .registerExternalSymbol("STORE", store, false)
//                    .build();
//            boolean result = LogicalExpression.compile(
//                    "STORE[\"books\"][0][\"title\"] = \"Alpha\"", env).compute();
//            assertThat(result).isTrue();
//        }
//    }
//
//    // ---- audit trail -----------------------------------------------------
//
//    @Nested
//    @DisplayName("Audit trail — VariableRead pre-stored for folded root")
//    class FoldedChainAuditTrail {
//
//        @Test
//        @DisplayName("PRICES[0]: VariableRead for PRICES pre-stored even when chain is not folded")
//        void pricesIndexAuditRead() {
//            var prices = newPrices();
//            AuditResult<BigDecimal> result = MathExpression.compile(
//                    "PRICES[0]", pricesEnv(prices)).computeWithAudit();
//            assertThat(variableReads(result))
//                    .extracting(AuditEvent.VariableRead::name)
//                    .contains("PRICES");
//        }
//
//        @Test
//        @DisplayName("BOOKS[?(@.author = \"Alice\")].price..sum(): VariableRead for BOOKS pre-stored")
//        void booksFilterProjectAuditRead() {
//            var books = newBooks();
//            AuditResult<BigDecimal> result = MathExpression.compile(
//                    "BOOKS[?(@.author = \"Alice\")].price..sum()", booksEnv(books)).computeWithAudit();
//            assertThat(variableReads(result))
//                    .extracting(AuditEvent.VariableRead::name)
//                    .contains("BOOKS");
//        }
//
//        @Test
//        @DisplayName("overridable PRICES has no pre-stored VariableRead — read is runtime")
//        void overridableProducesRuntimeReadOnly() {
//            var prices = newPrices();
//            ExpressionEnvironment env = ExpressionEnvironment.builder()
//                    .registerExternalSymbol("PRICES", prices, true)
//                    .build();
//            // computeWithAudit without providing PRICES in the map — will fail if pre-stored read is absent
//            // and runtime lookup finds nothing. For overridable, the value must come from the map.
//            AuditResult<BigDecimal> result = MathExpression.compile("PRICES[0]", env)
//                    .computeWithAudit(Map.of("PRICES", prices));
//            assertThat(variableReads(result))
//                    .extracting(AuditEvent.VariableRead::name)
//                    .contains("PRICES");
//        }
//    }
//
//    // ---- folded vs overridable produce identical results -----------------
//
//    @Nested
//    @DisplayName("Folded and overridable produce identical results")
//    class FoldedVsOverridableEquivalence {
//
//        @Test
//        @DisplayName("PRICES[0]: folded and overridable return same value")
//        void indexResultEquivalent() {
//            var prices = newPrices();
//            ExpressionEnvironment foldedEnv  = ExpressionEnvironment.builder()
//                    .registerExternalSymbol("PRICES", prices, false).build();
//            ExpressionEnvironment runtimeEnv = ExpressionEnvironment.builder()
//                    .registerExternalSymbol("PRICES", prices, true).build();
//
//            BigDecimal folded  = MathExpression.compile("PRICES[0]", foldedEnv).compute();
//            BigDecimal runtime = MathExpression.compile("PRICES[0]", runtimeEnv)
//                    .compute(Map.of("PRICES", prices));
//
//            assertThat(folded).isEqualByComparingTo(runtime);
//        }
//
//        @Test
//        @DisplayName("PRICES[?(@ > 10)]..count(): folded and overridable return same value")
//        void filterResultEquivalent() {
//            var prices = newPrices();
//            ExpressionEnvironment foldedEnv  = ExpressionEnvironment.builder()
//                    .registerExternalSymbol("PRICES", prices, false).build();
//            ExpressionEnvironment runtimeEnv = ExpressionEnvironment.builder()
//                    .registerExternalSymbol("PRICES", prices, true).build();
//
//            BigDecimal folded  = MathExpression.compile(
//                    "PRICES[?(@ > 10)]..count()", foldedEnv).compute();
//            BigDecimal runtime = MathExpression.compile(
//                    "PRICES[?(@ > 10)]..count()", runtimeEnv)
//                    .compute(Map.of("PRICES", prices));
//
//            assertThat(folded).isEqualByComparingTo(runtime);
//        }
//
//        @Test
//        @DisplayName("BOOKS[?(@.author = \"Alice\")].price..sum(): folded and overridable return same value")
//        void filterProjectionResultEquivalent() {
//            var books = newBooks();
//            ExpressionEnvironment foldedEnv  = ExpressionEnvironment.builder()
//                    .registerExternalSymbol("BOOKS", books, false).build();
//            ExpressionEnvironment runtimeEnv = ExpressionEnvironment.builder()
//                    .registerExternalSymbol("BOOKS", books, true).build();
//
//            BigDecimal folded  = MathExpression.compile(
//                    "BOOKS[?(@.author = \"Alice\")].price..sum()", foldedEnv).compute();
//            BigDecimal runtime = MathExpression.compile(
//                    "BOOKS[?(@.author = \"Alice\")].price..sum()", runtimeEnv)
//                    .compute(Map.of("BOOKS", books));
//
//            assertThat(folded).isEqualByComparingTo(runtime);
//        }
//    }
//
//    // ---- helpers ---------------------------------------------------------
//
//    private static <T> List<AuditEvent.VariableRead> variableReads(AuditResult<T> result) {
//        return result.trace().events().stream()
//                .filter(AuditEvent.VariableRead.class::isInstance)
//                .map(AuditEvent.VariableRead.class::cast)
//                .toList();
//    }
//}
