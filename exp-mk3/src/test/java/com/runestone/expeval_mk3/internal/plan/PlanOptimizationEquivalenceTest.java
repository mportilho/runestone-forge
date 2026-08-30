package com.runestone.expeval_mk3.internal.plan;

import com.runestone.expeval_mk3.api.ExpressionEnvironment;
import com.runestone.expeval_mk3.api.ExternalSymbolOverwritePolicy;
import com.runestone.expeval_mk3.api.FunctionPurity;
import com.runestone.expeval_mk3.api.ScalarType;
import com.runestone.expeval_mk3.internal.ast.ExpressionFileNode;
import com.runestone.expeval_mk3.internal.ast.SemanticAstBuildSuccess;
import com.runestone.expeval_mk3.internal.ast.SemanticAstBuilder;
import com.runestone.expeval_mk3.internal.parser.ExpressionParser;
import com.runestone.expeval_mk3.internal.parser.ParseSuccess;
import com.runestone.expeval_mk3.internal.semantics.SemanticModel;
import com.runestone.expeval_mk3.internal.semantics.SemanticResolutionSuccess;
import com.runestone.expeval_mk3.internal.semantics.SemanticResolver;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.constraints.BigRange;
import net.jqwik.api.constraints.Scale;

import java.math.BigDecimal;
import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The ADR 0019 property gate for issue #113, driven by generated inputs rather than a fixed corpus case:
 * for every generated pair, {@code build} and {@code buildOracle} must agree in value, in failure, and in
 * observable effect order. The source exercises division (failure on a generated zero divisor), root
 * (failure outside the real domain on a generated negative operand), factorial (failure outside its
 * domain or bound on a generated negative or non-integral operand), and scale (generated operands keep a
 * fractional part). No transformation is installed yet, so this property is born green; it is the
 * regression net every later optimization increment in Etapa 7 must keep green.
 */
class PlanOptimizationEquivalenceTest {

    private static final String SOURCE =
            "p := track(x); q := track(y); r := p / q; s := sqrt(p); t := p!; u := sqrt(x) + sqrt(x); r + s + t + u";

    /**
     * Issue #126's own comparison/equality specialization pilot, reusing this same property test's
     * generated pairs so the specialized paths ({@code NumberComparisonExecutableNode},
     * {@code NumberEqualityExecutableNode}) are exercised generatively rather than only by fixed
     * corpus cases, matching the ticket's "extended jqwik suite" testing expectation. {@code x} and
     * {@code y} are generated at different scales specifically to keep landing on the numeric equality
     * risk the ticket calls out: a scale difference alone must never flip {@code =}/{@code <>}.
     */
    private static final String COMPARISON_AND_EQUALITY_SOURCE =
            "(x > y) = (y < x) and (x >= y) = (y <= x) and (x = x) and (x <> y or x = y)";

    @Property
    void buildAndBuildOracleAgreeOnValueFailureOrderAndEffects(
            @ForAll @BigRange(min = "-20", max = "20") @Scale(2) BigDecimal x,
            @ForAll @BigRange(min = "-20", max = "20") @Scale(2) BigDecimal y) {
        EffectProbe probe = new EffectProbe();
        ExpressionEnvironment environment = environment(probe);
        SemanticModel model = resolve(SOURCE, environment);
        Map<String, Object> inputs = Map.of("x", x, "y", y);

        PlanEquivalenceHarness.assertEquivalent(
                model, environment, inputs, Clock.systemUTC(), probe::reset, probe::orderSnapshot);
    }

    @Property
    void buildAndBuildOracleAgreeOnComparisonAndEqualityAcrossScales(
            @ForAll @BigRange(min = "-20", max = "20") @Scale(2) BigDecimal x,
            @ForAll @BigRange(min = "-20", max = "20") @Scale(0) BigDecimal y) {
        EffectProbe probe = new EffectProbe();
        ExpressionEnvironment environment = environment(probe);
        SemanticModel model = resolve(COMPARISON_AND_EQUALITY_SOURCE, environment);
        Map<String, Object> inputs = Map.of("x", x, "y", y);

        PlanEquivalenceHarness.assertEquivalent(
                model, environment, inputs, Clock.systemUTC(), probe::reset, probe::orderSnapshot);
    }

    @Property
    void memoizedMemoryAgreesWhenTheFirstSourceOccurrenceMayBeUnreached(
            @ForAll boolean enabled,
            @ForAll @BigRange(min = "0", max = "20") @Scale(2) BigDecimal x) {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol("enabled", false, ExternalSymbolOverwritePolicy.OVERRIDABLE)
                .externalSymbol("x", ScalarType.NUMBER, BigDecimal.ZERO, ExternalSymbolOverwritePolicy.OVERRIDABLE)
                .build();
        SemanticModel model = resolve("if(enabled; sqrt(x); 0) + sqrt(x)", environment);

        PlanEquivalenceHarness.assertEquivalent(
                model, environment, Map.of("enabled", enabled, "x", x), Clock.systemUTC());
    }

    private static ExpressionEnvironment environment(EffectProbe probe) {
        return ExpressionEnvironment.builder()
                .externalSymbol("x", ScalarType.NUMBER, BigDecimal.ZERO, ExternalSymbolOverwritePolicy.OVERRIDABLE)
                .externalSymbol("y", ScalarType.NUMBER, BigDecimal.ZERO, ExternalSymbolOverwritePolicy.OVERRIDABLE)
                .functionsFrom(probe, FunctionPurity.IMPURE)
                .build();
    }

    private static SemanticModel resolve(String source, ExpressionEnvironment environment) {
        ExpressionFileNode ast = ast(source);
        SemanticResolutionSuccess result = (SemanticResolutionSuccess) new SemanticResolver().resolve(ast, environment);
        return result.model();
    }

    private static ExpressionFileNode ast(String source) {
        ParseSuccess parse = (ParseSuccess) new ExpressionParser().parse(source);
        return ((SemanticAstBuildSuccess) new SemanticAstBuilder().build(parse)).file();
    }

    /** Records the order impure function calls are observed in, as prior art in {@code ScalarEvaluationPolicyRuntimeTest} does. */
    public static final class EffectProbe {
        private final List<BigDecimal> order = new ArrayList<>();

        public BigDecimal track(BigDecimal tag) {
            order.add(tag);
            return tag;
        }

        void reset() {
            order.clear();
        }

        List<BigDecimal> orderSnapshot() {
            return List.copyOf(order);
        }
    }
}
