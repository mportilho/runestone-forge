package com.runestone.expeval_mk3.internal.plan;

import com.runestone.expeval_mk3.api.ComputationWithMemory;
import com.runestone.expeval_mk3.api.ExpressionEnvironment;
import com.runestone.expeval_mk3.api.ExternalSymbolOverwritePolicy;
import com.runestone.expeval_mk3.api.FunctionPurity;
import com.runestone.expeval_mk3.internal.ast.ExpressionFileNode;
import com.runestone.expeval_mk3.internal.ast.SemanticAstBuildSuccess;
import com.runestone.expeval_mk3.internal.ast.SemanticAstBuilder;
import com.runestone.expeval_mk3.internal.parser.ExpressionParser;
import com.runestone.expeval_mk3.internal.parser.ParseSuccess;
import com.runestone.expeval_mk3.internal.semantics.SemanticModel;
import com.runestone.expeval_mk3.internal.semantics.SemanticResolutionSuccess;
import com.runestone.expeval_mk3.internal.semantics.SemanticResolver;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class CalculationMemoryPlanEquivalenceTest {

    @Test
    void optimizedAndOraclePlansAgreeForNonFoldedNonMemoizedCalculationFlows() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol("enabled", false, ExternalSymbolOverwritePolicy.OVERRIDABLE)
                .functionsFrom(new Functions(), FunctionPurity.IMPURE)
                .build();
        SemanticModel model = resolve(
                "assigned := track(1); if enabled then track(track(2)) else track(3) endif", environment);
        ExecutionPlanBuilder builder = new ExecutionPlanBuilder();
        ExecutionPlan optimized = builder.build(model, environment);
        ExecutionPlan oracle = builder.buildOracle(model, environment);

        for (boolean enabled : new boolean[] {false, true}) {
            ComputationWithMemory<Object> optimizedResult = optimized.computeWithMemory(
                    Map.of("enabled", enabled), Clock.systemUTC());
            ComputationWithMemory<Object> oracleResult = oracle.computeWithMemory(
                    Map.of("enabled", enabled), Clock.systemUTC());

            assertThat(optimizedResult.result()).isEqualTo(oracleResult.result());
            assertThat(optimizedResult.memory().variables()).isEqualTo(oracleResult.memory().variables());
            assertThat(optimizedResult.memory().calculations())
                    .isEqualTo(oracleResult.memory().calculations());
        }
    }

    @Test
    void optimizedAndOraclePlansAgreeForCurrentTemporalMemory() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder().zoneId(ZoneOffset.UTC).build();
        SemanticModel model = resolve("day := currDate; currDateTime", environment);
        ExecutionPlanBuilder builder = new ExecutionPlanBuilder();
        Clock clock = Clock.fixed(Instant.parse("2024-03-15T10:20:30Z"), ZoneOffset.UTC);

        ComputationWithMemory<Object> optimized = builder.build(model, environment).computeWithMemory(Map.of(), clock);
        ComputationWithMemory<Object> oracle = builder.buildOracle(model, environment).computeWithMemory(Map.of(), clock);

        assertThat(optimized.result()).isEqualTo(oracle.result());
        assertThat(optimized.memory().variables()).isEqualTo(oracle.memory().variables());
        assertThat(optimized.memory().calculations()).isEqualTo(oracle.memory().calculations());
    }

    private static SemanticModel resolve(String source, ExpressionEnvironment environment) {
        ParseSuccess parse = (ParseSuccess) new ExpressionParser().parse(source);
        ExpressionFileNode ast = ((SemanticAstBuildSuccess) new SemanticAstBuilder().build(parse)).file();
        return ((SemanticResolutionSuccess) new SemanticResolver().resolve(ast, environment)).model();
    }

    public static final class Functions {

        public BigDecimal track(BigDecimal value) {
            return value;
        }
    }
}
