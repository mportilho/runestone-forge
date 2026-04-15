package com.runestone.expeval.perf;

import com.runestone.expeval.api.MathExpression;
import com.runestone.expeval.api.support.TestCollectionFunctions;
import com.runestone.expeval.environment.ExpressionEnvironment;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class CollectionNavigationBenchmarkSupport {

    private static final int FRAME_COUNT = 256;
    private static final int FRAME_MASK = FRAME_COUNT - 1;
    private static final BigDecimal THRESHOLD = new BigDecimal("10");

    public static final String INDEX_EXPRESSION = "prices[0]";
    public static final String LIST_FILTER_EXPRESSION = "books[?(@.price < threshold)]..count()";
    public static final String MAP_FILTER_EXPRESSION = "bookByIsbn[?(@.price < threshold)]..count()";
    public static final String MAP_VALUES_EXPRESSION = "bookByIsbn..values()..count()";
    public static final String CUSTOM_FUNCTION_EXPRESSION = "prices..countAbove(threshold)";
    public static final String DEEP_SCAN_EXPRESSION = "store..price..count()";

    private static final Frame[] FRAMES = buildFrames();
    private static final ExpressionEnvironment ENVIRONMENT = buildEnvironment(FRAMES[0]);

    private static final MathExpression INDEX_EXPRESSION_COMPILED =
            MathExpression.compile(INDEX_EXPRESSION, ENVIRONMENT);
    private static final MathExpression LIST_FILTER_EXPRESSION_COMPILED =
            MathExpression.compile(LIST_FILTER_EXPRESSION, ENVIRONMENT);
    private static final MathExpression MAP_FILTER_EXPRESSION_COMPILED =
            MathExpression.compile(MAP_FILTER_EXPRESSION, ENVIRONMENT);
    private static final MathExpression MAP_VALUES_EXPRESSION_COMPILED =
            MathExpression.compile(MAP_VALUES_EXPRESSION, ENVIRONMENT);
    private static final MathExpression CUSTOM_FUNCTION_EXPRESSION_COMPILED =
            MathExpression.compile(CUSTOM_FUNCTION_EXPRESSION, ENVIRONMENT);
    private static final MathExpression DEEP_SCAN_EXPRESSION_COMPILED =
            MathExpression.compile(DEEP_SCAN_EXPRESSION, ENVIRONMENT);

    private CollectionNavigationBenchmarkSupport() {
    }

    public static BigDecimal evaluateIndex(int index) {
        return INDEX_EXPRESSION_COMPILED.compute(FRAMES[index & FRAME_MASK].indexValues());
    }

    public static BigDecimal evaluateListFilterCount(int index) {
        return LIST_FILTER_EXPRESSION_COMPILED.compute(FRAMES[index & FRAME_MASK].listFilterValues());
    }

    public static BigDecimal evaluateMapFilterCount(int index) {
        return MAP_FILTER_EXPRESSION_COMPILED.compute(FRAMES[index & FRAME_MASK].mapFilterValues());
    }

    public static BigDecimal evaluateMapValuesCount(int index) {
        return MAP_VALUES_EXPRESSION_COMPILED.compute(FRAMES[index & FRAME_MASK].mapProjectionValues());
    }

    public static BigDecimal evaluateCustomFunctionCount(int index) {
        return CUSTOM_FUNCTION_EXPRESSION_COMPILED.compute(FRAMES[index & FRAME_MASK].customFunctionValues());
    }

    public static BigDecimal evaluateDeepScanCount(int index) {
        return DEEP_SCAN_EXPRESSION_COMPILED.compute(FRAMES[index & FRAME_MASK].deepScanValues());
    }

    private static ExpressionEnvironment buildEnvironment(Frame frame) {
        return ExpressionEnvironment.builder()
                .registerStaticProvider(TestCollectionFunctions.class)
                .registerExternalSymbol("prices", frame.indexValues().get("prices"), true)
                .registerExternalSymbol("books", frame.listFilterValues().get("books"), true)
                .registerExternalSymbol("bookByIsbn", frame.mapFilterValues().get("bookByIsbn"), true)
                .registerExternalSymbol("store", frame.deepScanValues().get("store"), true)
                .registerExternalSymbol("threshold", frame.listFilterValues().get("threshold"), true)
                .build();
    }

    private static Frame[] buildFrames() {
        Frame[] frames = new Frame[FRAME_COUNT];
        for (int index = 0; index < FRAME_COUNT; index++) {
            List<BigDecimal> prices = buildPrices(index);
            List<Map<String, Object>> books = buildBooks(index);
            Map<String, Object> bookByIsbn = buildBookByIsbn(index);
            Map<String, Object> store = buildStore(books, bookByIsbn);

            Map<String, Object> indexValues = Map.of("prices", prices);
            Map<String, Object> listFilterValues = Map.of("books", books, "threshold", THRESHOLD);
            Map<String, Object> mapFilterValues = Map.of("bookByIsbn", bookByIsbn, "threshold", THRESHOLD);
            Map<String, Object> mapProjectionValues = Map.of("bookByIsbn", bookByIsbn);
            Map<String, Object> customFunctionValues = Map.of("prices", prices, "threshold", THRESHOLD);
            Map<String, Object> deepScanValues = Map.of("store", store);
            frames[index] = new Frame(
                    indexValues,
                    listFilterValues,
                    mapFilterValues,
                    mapProjectionValues,
                    customFunctionValues,
                    deepScanValues);
        }
        return frames;
    }

    private static List<BigDecimal> buildPrices(int frameIndex) {
        List<BigDecimal> prices = new ArrayList<>(6);
        for (int slot = 0; slot < 6; slot++) {
            prices.add(price(frameIndex, slot));
        }
        return List.copyOf(prices);
    }

    private static List<Map<String, Object>> buildBooks(int frameIndex) {
        return List.of(
                book(frameIndex, 0, "Sayings of the Century", "Nigel Rees", "reference", "0-553-21311-3"),
                book(frameIndex, 1, "Sword of Honour", "Evelyn Waugh", "fiction", "0-395-19395-8"),
                book(frameIndex, 2, "Moby Dick", "Herman Melville", "fiction", "0-553-21311-7"),
                book(frameIndex, 3, "The Lord of the Rings", "J. R. R. Tolkien", "fiction", "0-395-19395-9")
        );
    }

    private static Map<String, Object> buildBookByIsbn(int frameIndex) {
        Map<String, Object> bookByIsbn = new LinkedHashMap<>();
        bookByIsbn.put("0-553-21311-3", book(frameIndex, 0, "Sayings of the Century", "Nigel Rees", "reference", "0-553-21311-3"));
        bookByIsbn.put("0-395-19395-8", book(frameIndex, 1, "Sword of Honour", "Evelyn Waugh", "fiction", "0-395-19395-8"));
        bookByIsbn.put("0-553-21311-7", book(frameIndex, 2, "Moby Dick", "Herman Melville", "fiction", "0-553-21311-7"));
        bookByIsbn.put("0-395-19395-9", book(frameIndex, 3, "The Lord of the Rings", "J. R. R. Tolkien", "fiction", "0-395-19395-9"));
        return Map.copyOf(bookByIsbn);
    }

    private static Map<String, Object> buildStore(
            List<Map<String, Object>> books,
            Map<String, Object> bookByIsbn) {
        Map<String, Object> store = new LinkedHashMap<>();
        store.put("books", books);
        store.put("bookByIsbn", bookByIsbn);
        store.put("self", store);
        return store;
    }

    private static Map<String, Object> book(
            int frameIndex,
            int slot,
            String title,
            String author,
            String category,
            String isbn) {
        return Map.of(
                "title", title,
                "author", author,
                "category", category,
                "isbn", isbn,
                "price", price(frameIndex, slot)
        );
    }

    private static BigDecimal price(int frameIndex, int slot) {
        long base = switch (slot) {
            case 0 -> 500L;
            case 1 -> 925L;
            case 2 -> 1250L;
            case 3 -> 1875L;
            case 4 -> 2100L;
            default -> 3450L;
        };
        return BigDecimal.valueOf(base + (frameIndex % 10L), 2);
    }

    private record Frame(
            Map<String, Object> indexValues,
            Map<String, Object> listFilterValues,
            Map<String, Object> mapFilterValues,
            Map<String, Object> mapProjectionValues,
            Map<String, Object> customFunctionValues,
            Map<String, Object> deepScanValues) {
    }
}
