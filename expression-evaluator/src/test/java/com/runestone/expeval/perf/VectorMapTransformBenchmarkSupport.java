package com.runestone.expeval.perf;

import com.runestone.expeval.api.MathExpression;
import com.runestone.expeval.environment.ExpressionEnvironment;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Pre-compiled expressions and rotating frame data for {@code ..map(@ -> expr)} benchmarks.
 *
 * <p>All expressions are compiled once at class-load time against a representative environment.
 * The 256 rotating frames prevent the JIT from specialising on a single constant input.
 *
 * <p>Scenarios covered:
 * <ol>
 *   <li>{@code books[*].price..sum()} — wildcard projection + aggregation (baseline)</li>
 *   <li>{@code books..map(@ -> @.price)..sum()} — map step extracting a property</li>
 *   <li>{@code books..map(@ -> @.price * @.qty)..sum()} — computed field, the motivating example</li>
 *   <li>{@code books..map(@ -> @.price * @.qty)..avg()} — computed field with avg</li>
 *   <li>{@code books..map(@ -> @.price)..map(@ -> @ * 2)..sum()} — two chained map steps</li>
 *   <li>{@code mapObj..map(@ -> @.value * 2)..sum()} — map-entry transform</li>
 * </ol>
 */
public final class VectorMapTransformBenchmarkSupport {

    private static final int FRAME_COUNT = 256;
    private static final int FRAME_MASK  = FRAME_COUNT - 1;

    // Expression strings (public for documentation in the runner output)
    public static final String WILDCARD_PROJECTION_SUM   = "books[*].price..sum()";
    public static final String MAP_EXTRACT_PROPERTY_SUM  = "books..map(@ -> @.price)..sum()";
    public static final String MAP_COMPUTED_FIELD_SUM    = "books..map(@ -> @.price * @.qty)..sum()";
    public static final String MAP_COMPUTED_FIELD_AVG    = "books..map(@ -> @.price * @.qty)..avg()";
    public static final String MAP_CHAINED_SUM           = "books..map(@ -> @.price)..map(@ -> @ * 2)..sum()";
    public static final String MAP_ENTRY_VALUES_SUM      = "mapObj..map(@ -> @.value * 2)..sum()";

    private static final Frame[] FRAMES = buildFrames();

    private static final ExpressionEnvironment ENVIRONMENT = buildEnvironment(FRAMES[0]);

    private static final MathExpression WILDCARD_PROJECTION_SUM_COMPILED =
            MathExpression.compile(WILDCARD_PROJECTION_SUM, ENVIRONMENT);
    private static final MathExpression MAP_EXTRACT_PROPERTY_SUM_COMPILED =
            MathExpression.compile(MAP_EXTRACT_PROPERTY_SUM, ENVIRONMENT);
    private static final MathExpression MAP_COMPUTED_FIELD_SUM_COMPILED =
            MathExpression.compile(MAP_COMPUTED_FIELD_SUM, ENVIRONMENT);
    private static final MathExpression MAP_COMPUTED_FIELD_AVG_COMPILED =
            MathExpression.compile(MAP_COMPUTED_FIELD_AVG, ENVIRONMENT);
    private static final MathExpression MAP_CHAINED_SUM_COMPILED =
            MathExpression.compile(MAP_CHAINED_SUM, ENVIRONMENT);
    private static final MathExpression MAP_ENTRY_VALUES_SUM_COMPILED =
            MathExpression.compile(MAP_ENTRY_VALUES_SUM, ENVIRONMENT);

    private VectorMapTransformBenchmarkSupport() {
    }

    public static BigDecimal evaluateWildcardProjectionSum(int index) {
        return WILDCARD_PROJECTION_SUM_COMPILED.compute(FRAMES[index & FRAME_MASK].listValues());
    }

    public static BigDecimal evaluateMapExtractPropertySum(int index) {
        return MAP_EXTRACT_PROPERTY_SUM_COMPILED.compute(FRAMES[index & FRAME_MASK].listValues());
    }

    public static BigDecimal evaluateMapComputedFieldSum(int index) {
        return MAP_COMPUTED_FIELD_SUM_COMPILED.compute(FRAMES[index & FRAME_MASK].listValues());
    }

    public static BigDecimal evaluateMapComputedFieldAvg(int index) {
        return MAP_COMPUTED_FIELD_AVG_COMPILED.compute(FRAMES[index & FRAME_MASK].listValues());
    }

    public static BigDecimal evaluateMapChainedSum(int index) {
        return MAP_CHAINED_SUM_COMPILED.compute(FRAMES[index & FRAME_MASK].listValues());
    }

    public static BigDecimal evaluateMapEntryValuesSum(int index) {
        return MAP_ENTRY_VALUES_SUM_COMPILED.compute(FRAMES[index & FRAME_MASK].mapValues());
    }

    // -------------------------------------------------------------------------
    // Frame construction
    // -------------------------------------------------------------------------

    private static ExpressionEnvironment buildEnvironment(Frame seed) {
        return ExpressionEnvironment.builder()
                .registerExternalSymbol("books",  seed.listValues().get("books"),  true)
                .registerExternalSymbol("mapObj", seed.mapValues().get("mapObj"),  true)
                .build();
    }

    private static Frame[] buildFrames() {
        Frame[] frames = new Frame[FRAME_COUNT];
        for (int i = 0; i < FRAME_COUNT; i++) {
            List<Map<String, Object>> books = buildBooks(i);
            Map<String, BigDecimal> mapObj  = buildMapObj(i);
            frames[i] = new Frame(
                    Map.of("books", books),
                    Map.of("mapObj", mapObj));
        }
        return frames;
    }

    private static List<Map<String, Object>> buildBooks(int frameIndex) {
        List<Map<String, Object>> books = new ArrayList<>(4);
        for (int slot = 0; slot < 4; slot++) {
            books.add(book(frameIndex, slot));
        }
        return List.copyOf(books);
    }

    private static Map<String, Object> book(int frameIndex, int slot) {
        return Map.of(
                "price", price(frameIndex, slot),
                "qty",   qty(slot)
        );
    }

    /** Prices shift slightly per frame to prevent constant-folding by the JIT. */
    private static BigDecimal price(int frameIndex, int slot) {
        long base = switch (slot) {
            case 0 -> 500L;
            case 1 -> 925L;
            case 2 -> 1250L;
            default -> 1875L;
        };
        return BigDecimal.valueOf(base + (frameIndex % 10L), 2);
    }

    private static BigDecimal qty(int slot) {
        return BigDecimal.valueOf(slot + 1);
    }

    /** Four entries whose values rotate with the frame index. */
    private static Map<String, BigDecimal> buildMapObj(int frameIndex) {
        Map<String, BigDecimal> map = new LinkedHashMap<>(4);
        map.put("a", BigDecimal.valueOf(100L + frameIndex % 10L, 2));
        map.put("b", BigDecimal.valueOf(200L + frameIndex % 10L, 2));
        map.put("c", BigDecimal.valueOf(300L + frameIndex % 10L, 2));
        map.put("d", BigDecimal.valueOf(400L + frameIndex % 10L, 2));
        return Map.copyOf(map);
    }

    private record Frame(
            Map<String, Object> listValues,
            Map<String, Object> mapValues) {
    }
}
