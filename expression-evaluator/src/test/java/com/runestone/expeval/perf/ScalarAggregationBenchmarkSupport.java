package com.runestone.expeval.perf;

import com.runestone.expeval.api.MathExpression;
import com.runestone.expeval.environment.ExpressionEnvironment;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ScalarAggregationBenchmarkSupport {

    private static final int FRAME_COUNT = 256;
    private static final int FRAME_MASK = FRAME_COUNT - 1;

    public static final String DIRECT_SUM = "prices..sum()";
    public static final String DIRECT_COUNT = "prices..count()";
    public static final String FILTER_PROPERTY_AVG = "books[?(@.author = \"Alice\")].price..avg()";
    public static final String SLICE_PROPERTY_SUM = "books[1:5].price..sum()";
    public static final String MAP_TRANSFORM_SUM = "prices..map(@ -> @ * 2)..sum()";
    public static final String MAP_FILTER_VALUES_SUM = "bookByIsbn[?(@.value.category = \"fiction\")]..values()..sum(@ -> @.price)";
    public static final String COUNT_AFTER_MAP_TRANSFORM = "books..map(@ -> @.price * @.qty)..count()";

    private static final Frame[] FRAMES = buildFrames();
    private static final ExpressionEnvironment ENVIRONMENT = buildEnvironment(FRAMES[0]);

    private static final MathExpression DIRECT_SUM_COMPILED =
            MathExpression.compile(DIRECT_SUM, ENVIRONMENT);
    private static final MathExpression DIRECT_COUNT_COMPILED =
            MathExpression.compile(DIRECT_COUNT, ENVIRONMENT);
    private static final MathExpression FILTER_PROPERTY_AVG_COMPILED =
            MathExpression.compile(FILTER_PROPERTY_AVG, ENVIRONMENT);
    private static final MathExpression SLICE_PROPERTY_SUM_COMPILED =
            MathExpression.compile(SLICE_PROPERTY_SUM, ENVIRONMENT);
    private static final MathExpression MAP_TRANSFORM_SUM_COMPILED =
            MathExpression.compile(MAP_TRANSFORM_SUM, ENVIRONMENT);
    private static final MathExpression MAP_FILTER_VALUES_SUM_COMPILED =
            MathExpression.compile(MAP_FILTER_VALUES_SUM, ENVIRONMENT);
    private static final MathExpression COUNT_AFTER_MAP_TRANSFORM_COMPILED =
            MathExpression.compile(COUNT_AFTER_MAP_TRANSFORM, ENVIRONMENT);

    private ScalarAggregationBenchmarkSupport() {
    }

    public static BigDecimal evaluateDirectSum(int index) {
        return DIRECT_SUM_COMPILED.compute(FRAMES[index & FRAME_MASK].pricesValues());
    }

    public static BigDecimal evaluateDirectCount(int index) {
        return DIRECT_COUNT_COMPILED.compute(FRAMES[index & FRAME_MASK].pricesValues());
    }

    public static BigDecimal evaluateFilterPropertyAvg(int index) {
        return FILTER_PROPERTY_AVG_COMPILED.compute(FRAMES[index & FRAME_MASK].booksValues());
    }

    public static BigDecimal evaluateSlicePropertySum(int index) {
        return SLICE_PROPERTY_SUM_COMPILED.compute(FRAMES[index & FRAME_MASK].booksValues());
    }

    public static BigDecimal evaluateMapTransformSum(int index) {
        return MAP_TRANSFORM_SUM_COMPILED.compute(FRAMES[index & FRAME_MASK].pricesValues());
    }

    public static BigDecimal evaluateMapFilterValuesSum(int index) {
        return MAP_FILTER_VALUES_SUM_COMPILED.compute(FRAMES[index & FRAME_MASK].bookByIsbnValues());
    }

    public static BigDecimal evaluateCountAfterMapTransform(int index) {
        return COUNT_AFTER_MAP_TRANSFORM_COMPILED.compute(FRAMES[index & FRAME_MASK].booksValues());
    }

    private static ExpressionEnvironment buildEnvironment(Frame frame) {
        return ExpressionEnvironment.builder()
                .registerExternalSymbol("prices", frame.pricesValues().get("prices"), true)
                .registerExternalSymbol("books", frame.booksValues().get("books"), true)
                .registerExternalSymbol("bookByIsbn", frame.bookByIsbnValues().get("bookByIsbn"), true)
                .build();
    }

    private static Frame[] buildFrames() {
        Frame[] frames = new Frame[FRAME_COUNT];
        for (int index = 0; index < FRAME_COUNT; index++) {
            List<BigDecimal> prices = buildPrices(index);
            List<Map<String, Object>> books = buildBooks(index);
            Map<String, Object> bookByIsbn = buildBookByIsbn(index, books);
            frames[index] = new Frame(
                    Map.of("prices", prices),
                    Map.of("books", books),
                    Map.of("bookByIsbn", bookByIsbn));
        }
        return frames;
    }

    private static List<BigDecimal> buildPrices(int frameIndex) {
        List<BigDecimal> prices = new ArrayList<>(8);
        for (int slot = 0; slot < 8; slot++) {
            prices.add(price(frameIndex, slot));
        }
        return List.copyOf(prices);
    }

    private static List<Map<String, Object>> buildBooks(int frameIndex) {
        List<Map<String, Object>> books = new ArrayList<>(6);
        for (int slot = 0; slot < 6; slot++) {
            books.add(book(frameIndex, slot));
        }
        return List.copyOf(books);
    }

    private static Map<String, Object> buildBookByIsbn(int frameIndex, List<Map<String, Object>> books) {
        Map<String, Object> map = new LinkedHashMap<>(books.size());
        for (int slot = 0; slot < books.size(); slot++) {
            map.put("isbn-" + frameIndex + '-' + slot, books.get(slot));
        }
        return Map.copyOf(map);
    }

    private static Map<String, Object> book(int frameIndex, int slot) {
        return Map.of(
                "author", slot % 2 == 0 ? "Alice" : "Bob",
                "category", slot % 3 == 0 ? "reference" : "fiction",
                "price", price(frameIndex, slot),
                "qty", BigDecimal.valueOf(slot + 1L)
        );
    }

    private static BigDecimal price(int frameIndex, int slot) {
        long cents = switch (slot) {
            case 0 -> 500L;
            case 1 -> 925L;
            case 2 -> 1250L;
            case 3 -> 1875L;
            case 4 -> 2100L;
            case 5 -> 3450L;
            case 6 -> 4200L;
            default -> 5500L;
        };
        return BigDecimal.valueOf(cents + (frameIndex % 10L), 2);
    }

    private record Frame(
            Map<String, Object> pricesValues,
            Map<String, Object> booksValues,
            Map<String, Object> bookByIsbnValues) {
    }
}
