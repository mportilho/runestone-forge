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
// * Mirror of {@link FoldedSymbolCollectionNavigationTest} with {@code overridable=true}.
// *
// * <p>Verifies that overridable symbols:
// * <ul>
// *   <li>produce the same functional results as their folded counterparts;</li>
// *   <li>are NOT evaluated at compile time — list access only happens during {@code compute()};</li>
// *   <li>are re-evaluated on every {@code compute()} call (navigation runs each time).</li>
// * </ul>
// *
// * <p>All tests in this class should be GREEN from the start. They document current runtime-only
// * behavior and serve as a regression guard against accidentally applying folding to overridable
// * symbols.
// */
//@DisplayName("Collection navigation on overridable (runtime-resolved) external symbols")
//class OverridableSymbolCollectionNavigationTest {
//
//    // ---- fixtures (mirrors FoldedSymbolCollectionNavigationTest) ---------
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
//                .registerExternalSymbol("prices", prices, true)
//                .build();
//    }
//
//    private static ExpressionEnvironment booksEnv(TrackedList<Book> books) {
//        return ExpressionEnvironment.builder()
//                .registerExternalSymbol("books", books, true)
//                .build();
//    }
//
//    private static ExpressionEnvironment catalogEnv(Catalog catalog) {
//        return ExpressionEnvironment.builder()
//                .registerExternalSymbol("catalog", catalog, true)
//                .registerTypeHint(Catalog.class)
//                .build();
//    }
//
//    // ---- [n] — index access ----------------------------------------------
//
//    @Nested
//    @DisplayName("[n] — index access on overridable list")
//    class IndexAccessOnOverridableList {
//
//        @Test
//        @DisplayName("prices[0] returns 5")
//        void firstElement() {
//            var prices = newPrices();
//            BigDecimal result = MathExpression.compile("prices[0]", pricesEnv(prices))
//                    .compute(Map.of("prices", prices));
//            assertThat(result).isEqualByComparingTo("5");
//        }
//
//        @Test
//        @DisplayName("prices[-1] returns 10 (last via negative index)")
//        void lastElementViaNegativeIndex() {
//            var prices = newPrices();
//            BigDecimal result = MathExpression.compile("prices[-1]", pricesEnv(prices))
//                    .compute(Map.of("prices", prices));
//            assertThat(result).isEqualByComparingTo("10");
//        }
//
//        @Test
//        @DisplayName("prices[1] returns 15")
//        void secondElement() {
//            var prices = newPrices();
//            BigDecimal result = MathExpression.compile("prices[1]", pricesEnv(prices))
//                    .compute(Map.of("prices", prices));
//            assertThat(result).isEqualByComparingTo("15");
//        }
//
//        @Test
//        @DisplayName("prices[0] NOT folded at compile time — list accessed on each compute()")
//        void indexNotFoldedAtCompileTime() {
//            var prices = newPrices();
//            MathExpression expr = MathExpression.compile("prices[0]", pricesEnv(prices));
//            prices.accessCount = 0; // nothing accessed during compile
//
//            int n = 3;
//            for (int i = 0; i < n; i++) {
//                expr.compute(Map.of("prices", prices));
//            }
//
//            assertThat(prices.accessCount).isGreaterThanOrEqualTo(n);
//        }
//    }
//
//    // ---- [start:end] — slice ---------------------------------------------
//
//    @Nested
//    @DisplayName("[start:end] — slice on overridable list")
//    class SliceAccessOnOverridableList {
//
//        @Test
//        @DisplayName("prices[1:3]..sum() returns 40")
//        void sliceMiddleSum() {
//            var prices = newPrices();
//            BigDecimal result = MathExpression.compile("prices[1:3]..sum()", pricesEnv(prices))
//                    .compute(Map.of("prices", prices));
//            assertThat(result).isEqualByComparingTo("40");
//        }
//
//        @Test
//        @DisplayName("prices[:2]..sum() returns 20")
//        void sliceFromStartSum() {
//            var prices = newPrices();
//            BigDecimal result = MathExpression.compile("prices[:2]..sum()", pricesEnv(prices))
//                    .compute(Map.of("prices", prices));
//            assertThat(result).isEqualByComparingTo("20");
//        }
//
//        @Test
//        @DisplayName("prices[1:3]..sum() NOT folded at compile time — list accessed on each compute()")
//        void sliceNotFoldedAtCompileTime() {
//            var prices = newPrices();
//            MathExpression expr = MathExpression.compile("prices[1:3]..sum()", pricesEnv(prices));
//            prices.accessCount = 0;
//
//            int n = 3;
//            for (int i = 0; i < n; i++) {
//                expr.compute(Map.of("prices", prices));
//            }
//
//            assertThat(prices.accessCount).isGreaterThanOrEqualTo(n);
//        }
//    }
//
//    // ---- [*] — wildcard --------------------------------------------------
//
//    @Nested
//    @DisplayName("[*] — wildcard on overridable list")
//    class WildcardOnOverridableList {
//
//        @Test
//        @DisplayName("prices[*]..sum() returns 55")
//        void wildcardSum() {
//            var prices = newPrices();
//            BigDecimal result = MathExpression.compile("prices[*]..sum()", pricesEnv(prices))
//                    .compute(Map.of("prices", prices));
//            assertThat(result).isEqualByComparingTo("55");
//        }
//
//        @Test
//        @DisplayName("prices[*]..count() returns 4")
//        void wildcardCount() {
//            var prices = newPrices();
//            BigDecimal result = MathExpression.compile("prices[*]..count()", pricesEnv(prices))
//                    .compute(Map.of("prices", prices));
//            assertThat(result).isEqualByComparingTo("4");
//        }
//
//        @Test
//        @DisplayName("prices[*]..sum() NOT folded at compile time — list accessed on each compute()")
//        void wildcardNotFoldedAtCompileTime() {
//            var prices = newPrices();
//            MathExpression expr = MathExpression.compile("prices[*]..sum()", pricesEnv(prices));
//            prices.accessCount = 0;
//
//            int n = 3;
//            for (int i = 0; i < n; i++) {
//                expr.compute(Map.of("prices", prices));
//            }
//
//            assertThat(prices.accessCount).isGreaterThanOrEqualTo(n);
//        }
//    }
//
//    // ---- [?(@ op literal)] — filter on scalar list -----------------------
//
//    @Nested
//    @DisplayName("[?(@ > x)] — filter predicate on overridable scalar list")
//    class FilterPredicateOnOverridableScalarList {
//
//        @Test
//        @DisplayName("prices[?(@ > 10)]..count() returns 2")
//        void filterGreaterThan() {
//            var prices = newPrices();
//            BigDecimal result = MathExpression.compile(
//                    "prices[?(@ > 10)]..count()", pricesEnv(prices))
//                    .compute(Map.of("prices", prices));
//            assertThat(result).isEqualByComparingTo("2");
//        }
//
//        @Test
//        @DisplayName("prices[?(@ >= 5)]..count() returns 4")
//        void filterAllQualify() {
//            var prices = newPrices();
//            BigDecimal result = MathExpression.compile(
//                    "prices[?(@ >= 5)]..count()", pricesEnv(prices))
//                    .compute(Map.of("prices", prices));
//            assertThat(result).isEqualByComparingTo("4");
//        }
//
//        @Test
//        @DisplayName("prices[?(@ > 10)]..count() NOT folded — list accessed on each compute()")
//        void filterNotFoldedAtCompileTime() {
//            var prices = newPrices();
//            MathExpression expr = MathExpression.compile(
//                    "prices[?(@ > 10)]..count()", pricesEnv(prices));
//            prices.accessCount = 0;
//
//            int n = 3;
//            for (int i = 0; i < n; i++) {
//                expr.compute(Map.of("prices", prices));
//            }
//
//            assertThat(prices.accessCount).isGreaterThanOrEqualTo(n);
//        }
//    }
//
//    // ---- ..agg() — aggregation -------------------------------------------
//
//    @Nested
//    @DisplayName("..sum() ..avg() ..count() — aggregation on overridable list")
//    class AggregationOnOverridableList {
//
//        @Test
//        @DisplayName("prices..sum() returns 55")
//        void sum() {
//            var prices = newPrices();
//            BigDecimal result = MathExpression.compile("prices..sum()", pricesEnv(prices))
//                    .compute(Map.of("prices", prices));
//            assertThat(result).isEqualByComparingTo("55");
//        }
//
//        @Test
//        @DisplayName("prices..count() returns 4")
//        void count() {
//            var prices = newPrices();
//            BigDecimal result = MathExpression.compile("prices..count()", pricesEnv(prices))
//                    .compute(Map.of("prices", prices));
//            assertThat(result).isEqualByComparingTo("4");
//        }
//
//        @Test
//        @DisplayName("prices..avg() returns 13.75")
//        void avg() {
//            var prices = newPrices();
//            BigDecimal result = MathExpression.compile("prices..avg()", pricesEnv(prices))
//                    .compute(Map.of("prices", prices));
//            assertThat(result).isEqualByComparingTo("13.75");
//        }
//
//        @Test
//        @DisplayName("prices..sum() NOT folded — list accessed on each compute()")
//        void aggregationNotFoldedAtCompileTime() {
//            var prices = newPrices();
//            MathExpression expr = MathExpression.compile("prices..sum()", pricesEnv(prices));
//            prices.accessCount = 0;
//
//            int n = 3;
//            for (int i = 0; i < n; i++) {
//                expr.compute(Map.of("prices", prices));
//            }
//
//            assertThat(prices.accessCount).isGreaterThanOrEqualTo(n);
//        }
//    }
//
//    // ---- [?(@.field op x)] — filter on list of records ------------------
//
//    @Nested
//    @DisplayName("[?(@.field = x)] — filter on overridable list of records")
//    class FilterOnOverridableListOfRecords {
//
//        @Test
//        @DisplayName("books[?(@.author = \"Alice\")]..count() returns 2")
//        void filterByAuthor() {
//            var books = newBooks();
//            BigDecimal result = MathExpression.compile(
//                    "books[?(@.author = \"Alice\")]..count()", booksEnv(books))
//                    .compute(Map.of("books", books));
//            assertThat(result).isEqualByComparingTo("2");
//        }
//
//        @Test
//        @DisplayName("books[?(@.price > 10)]..count() returns 2")
//        void filterByPrice() {
//            var books = newBooks();
//            BigDecimal result = MathExpression.compile(
//                    "books[?(@.price > 10)]..count()", booksEnv(books))
//                    .compute(Map.of("books", books));
//            assertThat(result).isEqualByComparingTo("2");
//        }
//
//        @Test
//        @DisplayName("books[?(@.author = \"Alice\")]..count() NOT folded — list accessed on each compute()")
//        void filterOnRecordsNotFoldedAtCompileTime() {
//            var books = newBooks();
//            MathExpression expr = MathExpression.compile(
//                    "books[?(@.author = \"Alice\")]..count()", booksEnv(books));
//            books.accessCount = 0;
//
//            int n = 3;
//            for (int i = 0; i < n; i++) {
//                expr.compute(Map.of("books", books));
//            }
//
//            assertThat(books.accessCount).isGreaterThanOrEqualTo(n);
//        }
//    }
//
//    // ---- [?(@.x)].field..agg() — filter + projection + aggregation ------
//
//    @Nested
//    @DisplayName("[?(@.x)].field..agg() — filter, projection, aggregation on overridable list of records")
//    class ProjectionOnOverridableListOfRecords {
//
//        @Test
//        @DisplayName("books[?(@.author = \"Alice\")].price..sum() returns 14.98")
//        void filterProjectSum() {
//            var books = newBooks();
//            BigDecimal result = MathExpression.compile(
//                    "books[?(@.author = \"Alice\")].price..sum()", booksEnv(books))
//                    .compute(Map.of("books", books));
//            assertThat(result).isEqualByComparingTo("14.98");
//        }
//
//        @Test
//        @DisplayName("books[*].title..count() returns 4")
//        void wildcardProjectCount() {
//            var books = newBooks();
//            BigDecimal result = MathExpression.compile(
//                    "books[*].title..count()", booksEnv(books))
//                    .compute(Map.of("books", books));
//            assertThat(result).isEqualByComparingTo("4");
//        }
//
//        @Test
//        @DisplayName("books[?(@.author = \"Alice\")].price..sum() NOT folded — list accessed on each compute()")
//        void filterProjectionNotFoldedAtCompileTime() {
//            var books = newBooks();
//            MathExpression expr = MathExpression.compile(
//                    "books[?(@.author = \"Alice\")].price..sum()", booksEnv(books));
//            books.accessCount = 0;
//
//            int n = 3;
//            for (int i = 0; i < n; i++) {
//                expr.compute(Map.of("books", books));
//            }
//
//            assertThat(books.accessCount).isGreaterThanOrEqualTo(n);
//        }
//    }
//
//    // ---- catalog.books[n] — navigation into list field of overridable object
//
//    @Nested
//    @DisplayName("catalog.books[n] — navigation into list field of overridable complex object")
//    class NestedObjectNavigation {
//
//        @Test
//        @DisplayName("catalog.books[0].title equals \"Alpha\"")
//        void firstBookTitle() {
//            var books = newBooks();
//            Catalog catalog = new Catalog("shop", books);
//            boolean result = LogicalExpression.compile(
//                    "catalog.books[0].title = \"Alpha\"",
//                    catalogEnv(catalog)).compute(Map.of("catalog", catalog));
//            assertThat(result).isTrue();
//        }
//
//        @Test
//        @DisplayName("catalog.books..count() returns 4")
//        void catalogBooksCount() {
//            var books = newBooks();
//            Catalog catalog = new Catalog("shop", books);
//            BigDecimal result = MathExpression.compile(
//                    "catalog.books..count()",
//                    catalogEnv(catalog)).compute(Map.of("catalog", catalog));
//            assertThat(result).isEqualByComparingTo("4");
//        }
//
//        @Test
//        @DisplayName("catalog.books[?(@.author = \"Alice\")].price..sum() returns 14.98")
//        void catalogFilterProjectSum() {
//            var books = newBooks();
//            Catalog catalog = new Catalog("shop", books);
//            BigDecimal result = MathExpression.compile(
//                    "catalog.books[?(@.author = \"Alice\")].price..sum()",
//                    catalogEnv(catalog)).compute(Map.of("catalog", catalog));
//            assertThat(result).isEqualByComparingTo("14.98");
//        }
//
//        @Test
//        @DisplayName("catalog.books[0].title NOT folded — books list accessed on each compute()")
//        void catalogNavigationNotFoldedAtCompileTime() {
//            var books = newBooks();
//            Catalog catalog = new Catalog("shop", books);
//            LogicalExpression expr = LogicalExpression.compile(
//                    "catalog.books[0].title = \"Alpha\"", catalogEnv(catalog));
//            books.accessCount = 0;
//
//            int n = 3;
//            for (int i = 0; i < n; i++) {
//                expr.compute(Map.of("catalog", catalog));
//            }
//
//            assertThat(books.accessCount).isGreaterThanOrEqualTo(n);
//        }
//    }
//
//    // ---- audit trail -----------------------------------------------------
//
//    @Nested
//    @DisplayName("Audit trail — overridable symbol produces runtime VariableRead (not pre-stored)")
//    class OverridableChainAuditTrail {
//
//        @Test
//        @DisplayName("prices[0]: VariableRead for 'prices' appears in audit trail (runtime, not pre-stored)")
//        void pricesIndexAuditRead() {
//            var prices = newPrices();
//            AuditResult<BigDecimal> result = MathExpression.compile(
//                    "prices[0]", pricesEnv(prices))
//                    .computeWithAudit(Map.of("prices", prices));
//            assertThat(variableReads(result))
//                    .extracting(AuditEvent.VariableRead::name)
//                    .contains("prices");
//        }
//
//        @Test
//        @DisplayName("books[?(@.author = \"Alice\")].price..sum(): VariableRead for 'books' appears at runtime")
//        void booksFilterProjectAuditRead() {
//            var books = newBooks();
//            AuditResult<BigDecimal> result = MathExpression.compile(
//                    "books[?(@.author = \"Alice\")].price..sum()", booksEnv(books))
//                    .computeWithAudit(Map.of("books", books));
//            assertThat(variableReads(result))
//                    .extracting(AuditEvent.VariableRead::name)
//                    .contains("books");
//        }
//
//        @Test
//        @DisplayName("zero accesses to list before first compute() — no compile-time evaluation occurs")
//        void noListAccessBeforeFirstCompute() {
//            var prices = newPrices();
//            MathExpression.compile("prices[0]", pricesEnv(prices));
//            assertThat(prices.accessCount).isEqualTo(0);
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
