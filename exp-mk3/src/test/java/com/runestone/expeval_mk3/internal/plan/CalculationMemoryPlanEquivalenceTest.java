package com.runestone.expeval_mk3.internal.plan;

import com.runestone.expeval_mk3.api.CalculationEntry;
import com.runestone.expeval_mk3.api.CalculationKind;
import com.runestone.expeval_mk3.api.CollectionType;
import com.runestone.expeval_mk3.api.ComputationWithMemory;
import com.runestone.expeval_mk3.api.ExpressionEnvironment;
import com.runestone.expeval_mk3.api.ExpressionExecutionException;
import com.runestone.expeval_mk3.api.ExternalSymbolOverwritePolicy;
import com.runestone.expeval_mk3.api.FunctionPurity;
import com.runestone.expeval_mk3.api.ObjectType;
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
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class CalculationMemoryPlanEquivalenceTest {

    @Test
    void memoHitRepublishesEveryReachedSourceOccurrenceWithoutReinvokingTheFunction() {
        AtomicInteger invocations = new AtomicInteger();
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol("x", BigDecimal.ONE, ExternalSymbolOverwritePolicy.OVERRIDABLE)
                .functionsFrom(new CountingFunctions(invocations), FunctionPurity.PURE)
                .build();
        SemanticModel model = resolve("count(x) + count(x)", environment);
        ExecutionPlanBuilder builder = new ExecutionPlanBuilder();

        ComputationWithMemory<Object> optimized = builder.build(model, environment)
                .computeWithMemory(Map.of("x", BigDecimal.TEN), Clock.systemUTC());
        assertThat(invocations).hasValue(1);
        ComputationWithMemory<Object> oracle = builder.buildOracle(model, environment)
                .computeWithMemory(Map.of("x", BigDecimal.TEN), Clock.systemUTC());

        assertThat(invocations).hasValue(3);
        assertThat(optimized.memory().calculations()).isEqualTo(oracle.memory().calculations());
        assertThat(optimized.memory().calculations()).extracting(CalculationEntry::value)
                .containsExactly(BigDecimal.TEN, BigDecimal.TEN);
    }

    @Test
    void firstReachedMemoOccurrencePublishesOnlyItsOwnProvenance() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol("enabled", false, ExternalSymbolOverwritePolicy.OVERRIDABLE)
                .externalSymbol("x", BigDecimal.ONE, ExternalSymbolOverwritePolicy.OVERRIDABLE)
                .functionsFrom(new Functions(), FunctionPurity.PURE)
                .build();
        SemanticModel model = resolve("if enabled then track(x) else 0 endif + track(x)", environment);

        assertMemoryEquivalent(model, environment, Map.of("enabled", false, "x", BigDecimal.TEN));

        ComputationWithMemory<Object> optimized = new ExecutionPlanBuilder().build(model, environment)
                .computeWithMemory(Map.of("enabled", false, "x", BigDecimal.TEN), Clock.systemUTC());
        assertThat(optimized.memory().calculations()).singleElement().satisfies(calculation ->
                assertThat(calculation.key().sourceSpan().offset()).isGreaterThan(20));
    }

    @Test
    void cseSharedByAssignmentAndResultRespectsTheExecutedView() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol("x", BigDecimal.ONE, ExternalSymbolOverwritePolicy.OVERRIDABLE)
                .functionsFrom(new Functions(), FunctionPurity.PURE)
                .build();
        SemanticModel model = resolve("assigned := track(x); track(x)", environment);
        ExecutionPlanBuilder builder = new ExecutionPlanBuilder();
        ExecutionPlan optimizedPlan = builder.build(model, environment);
        ExecutionPlan oraclePlan = builder.buildOracle(model, environment);

        ComputationWithMemory<Map<String, Object>> optimizedAssignments = optimizedPlan
                .computeAssignmentsWithMemory(Map.of("x", BigDecimal.TEN), Clock.systemUTC());
        ComputationWithMemory<Map<String, Object>> oracleAssignments = oraclePlan
                .computeAssignmentsWithMemory(Map.of("x", BigDecimal.TEN), Clock.systemUTC());
        ComputationWithMemory<Object> optimizedResult = optimizedPlan
                .computeWithMemory(Map.of("x", BigDecimal.TEN), Clock.systemUTC());
        ComputationWithMemory<Object> oracleResult = oraclePlan
                .computeWithMemory(Map.of("x", BigDecimal.TEN), Clock.systemUTC());

        assertThat(optimizedAssignments.memory().calculations())
                .isEqualTo(oracleAssignments.memory().calculations()).hasSize(1);
        assertThat(optimizedResult.memory().calculations())
                .isEqualTo(oracleResult.memory().calculations()).hasSize(2);
    }

    @Test
    void nestedMemoGroupsReplayNestedCalculationPointsInEvaluationOrder() {
        AtomicInteger invocations = new AtomicInteger();
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol("x", BigDecimal.ONE, ExternalSymbolOverwritePolicy.OVERRIDABLE)
                .functionsFrom(new CountingFunctions(invocations), FunctionPurity.PURE)
                .build();
        SemanticModel model = resolve("count(count(x)) + count(count(x))", environment);
        ExecutionPlanBuilder builder = new ExecutionPlanBuilder();

        ComputationWithMemory<Object> optimized = builder.build(model, environment)
                .computeWithMemory(Map.of("x", BigDecimal.TEN), Clock.systemUTC());
        assertThat(invocations).hasValue(2);
        invocations.set(0);
        ComputationWithMemory<Object> oracle = builder.buildOracle(model, environment)
                .computeWithMemory(Map.of("x", BigDecimal.TEN), Clock.systemUTC());

        assertThat(invocations).hasValue(4);
        assertThat(optimized.memory().calculations()).isEqualTo(oracle.memory().calculations());
        assertThat(optimized.memory().calculations()).extracting(CalculationEntry::value)
                .containsExactly(BigDecimal.TEN, BigDecimal.TEN, BigDecimal.TEN, BigDecimal.TEN);
    }

    @Test
    void memoHitReplaysAStaticFoldedGroupInsideTheMemoizedSubtree() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol("x", BigDecimal.ONE, ExternalSymbolOverwritePolicy.OVERRIDABLE)
                .functionsFrom(new Functions(), FunctionPurity.FOLDABLE)
                .build();
        SemanticModel model = resolve("(track(1) + x) * (track(1) + x)", environment);

        assertMemoryEquivalent(model, environment, Map.of("x", BigDecimal.TEN));

        ComputationWithMemory<Object> optimized = new ExecutionPlanBuilder().build(model, environment)
                .computeWithMemory(Map.of("x", BigDecimal.TEN), Clock.systemUTC());
        assertThat(optimized.memory().calculations()).extracting(CalculationEntry::value)
                .containsExactly(BigDecimal.ONE, BigDecimal.ONE);
    }

    @Test
    void memoHitReplaysReachedNullFromASafeRegisteredProperty() {
        ObjectType accountType = new ObjectType(NullableAccount.class.getName());
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol("accounts", new com.runestone.expeval_mk3.api.MapType(accountType), Map.of(),
                        ExternalSymbolOverwritePolicy.OVERRIDABLE)
                .registerJavaType(NullableAccount.class)
                .build();
        String access = "accounts?.[\"missing\"]?.amount";
        SemanticModel model = resolve("(" + access + " ?? 0) + (" + access + " ?? 0)", environment);

        assertMemoryEquivalent(model, environment, Map.of());

        ComputationWithMemory<Object> optimized = new ExecutionPlanBuilder().build(model, environment)
                .computeWithMemory(Map.of(), Clock.systemUTC());
        assertThat(optimized.memory().calculations()).hasSize(2)
                .allSatisfy(calculation -> assertThat(calculation.value()).isNull());
    }

    @Test
    void replayPreservesReachabilityOfEquivalentPointsInMutuallyExclusiveBranches() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol("enabled", true, ExternalSymbolOverwritePolicy.OVERRIDABLE)
                .externalSymbol("x", BigDecimal.ONE, ExternalSymbolOverwritePolicy.OVERRIDABLE)
                .functionsFrom(new Functions(), FunctionPurity.PURE)
                .build();
        String conditional = "if enabled then track(x) else track(x) endif";
        SemanticModel model = resolve("(" + conditional + ") + (" + conditional + ")", environment);

        assertMemoryEquivalent(model, environment, Map.of("enabled", true, "x", BigDecimal.TEN));
        assertMemoryEquivalent(model, environment, Map.of("enabled", false, "x", BigDecimal.TEN));

        ComputationWithMemory<Object> optimized = new ExecutionPlanBuilder().build(model, environment)
                .computeWithMemory(Map.of("enabled", true, "x", BigDecimal.TEN), Clock.systemUTC());
        assertThat(optimized.memory().calculations()).hasSize(2);
    }

    @Test
    void opaqueOccurrenceCanSeedAReplayValueWithoutPublishingItsInactiveOrdinal() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol("x", BigDecimal.ONE, ExternalSymbolOverwritePolicy.OVERRIDABLE)
                .build();
        SemanticModel model = resolve(
                "items := [1, 2]; items.map(@ -> sqrt(x)).sum() + sqrt(x)", environment);

        assertMemoryEquivalent(model, environment, Map.of("x", BigDecimal.TEN));

        ComputationWithMemory<Object> optimized = new ExecutionPlanBuilder().build(model, environment)
                .computeWithMemory(Map.of("x", BigDecimal.TEN), Clock.systemUTC());
        assertThat(optimized.memory().calculations()).singleElement().satisfies(calculation ->
                assertThat(calculation.key().name()).isEqualTo("sqrt"));
    }

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

    @Test
    void optimizedAndOraclePlansAgreeForRegisteredMembersAndOpaqueCollectionBodies() {
        Account account = new Account(BigDecimal.TEN);
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol("account", new ObjectType(Account.class.getName()), account,
                        ExternalSymbolOverwritePolicy.OVERRIDABLE)
                .externalSymbol("accounts", new CollectionType(new ObjectType(Account.class.getName())), List.of(account),
                        ExternalSymbolOverwritePolicy.OVERRIDABLE)
                .registerJavaType(Account.class)
                .registerJavaTypeMethod(Account.class, "fee", FunctionPurity.IMPURE, BigDecimal.class)
                .functionsFrom(new Functions(), FunctionPurity.IMPURE)
                .build();
        SemanticModel model = resolve(
                "account.amount + account.fee(1) + accounts.map(@ -> track(@.amount)).sum()", environment);
        ExecutionPlanBuilder builder = new ExecutionPlanBuilder();

        ComputationWithMemory<Object> optimized = builder.build(model, environment)
                .computeWithMemory(Map.of(), Clock.systemUTC());
        ComputationWithMemory<Object> oracle = builder.buildOracle(model, environment)
                .computeWithMemory(Map.of(), Clock.systemUTC());

        assertThat(optimized.result()).isEqualTo(oracle.result());
        assertThat(optimized.memory().variables()).isEqualTo(oracle.memory().variables());
        assertThat(optimized.memory().calculations()).isEqualTo(oracle.memory().calculations());
        assertThat(optimized.memory().calculations()).hasSize(2);
    }

    @Test
    void optimizedAndOraclePlansAgreeForNestedFoldedCalculations() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .functionsFrom(new Functions(), FunctionPurity.FOLDABLE)
                .build();
        String source = "track(track(2)) + track(3)";
        SemanticModel model = resolve(source, environment);

        assertMemoryEquivalent(model, environment, Map.of());

        ExecutionPlan plan = new ExecutionPlanBuilder().build(model, environment);
        ComputationWithMemory<Object> optimized = plan.computeWithMemory(Map.of(), Clock.systemUTC());
        assertThat(optimized.memory().calculations()).extracting(CalculationEntry::value)
                .containsExactly(new BigDecimal("2"), new BigDecimal("2"), new BigDecimal("3"));
        assertThat(optimized.memory().calculations())
                .extracting(entry -> source.substring(
                        entry.key().sourceSpan().offset(), entry.key().sourceSpan().endOffset()))
                .containsExactly("track(2)", "track(track(2))", "track(3)");
        ComputationWithMemory<Object> second = plan.computeWithMemory(Map.of(), Clock.systemUTC());
        for (int index = 0; index < optimized.memory().calculationCount(); index++) {
            assertThat(second.memory().calculationKeyAt(index)).isSameAs(optimized.memory().calculationKeyAt(index));
            assertThat(second.memory().calculationValueAt(index)).isSameAs(optimized.memory().calculationValueAt(index));
        }
    }

    @Test
    void foldedCalculationGroupIsPublishedOnlyWhenItsBranchIsReached() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol("enabled", false, ExternalSymbolOverwritePolicy.OVERRIDABLE)
                .functionsFrom(new Functions(), FunctionPurity.FOLDABLE)
                .build();
        SemanticModel model = resolve("if enabled then track(track(2)) else 3 endif", environment);

        assertMemoryEquivalent(model, environment, Map.of("enabled", false));
        assertMemoryEquivalent(model, environment, Map.of("enabled", true));

        ExecutionPlan optimized = new ExecutionPlanBuilder().build(model, environment);
        assertThat(optimized.computeWithMemory(Map.of("enabled", false), Clock.systemUTC())
                .memory().calculations()).isEmpty();
        assertThat(optimized.computeWithMemory(Map.of("enabled", true), Clock.systemUTC())
                .memory().calculations()).extracting(CalculationEntry::value)
                .containsExactly(new BigDecimal("2"), new BigDecimal("2"));
    }

    @Test
    void foldedConditionRetainsItsCalculationBeforeTheSelectedBranch() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .functionsFrom(new Functions(), FunctionPurity.FOLDABLE)
                .build();
        SemanticModel model = resolve("if trackBoolean(false) then track(1) else track(2) endif", environment);

        assertMemoryEquivalent(model, environment, Map.of());

        ComputationWithMemory<Object> optimized = new ExecutionPlanBuilder().build(model, environment)
                .computeWithMemory(Map.of(), Clock.systemUTC());
        assertThat(optimized.memory().calculations()).extracting(CalculationEntry::value)
                .containsExactly(false, new BigDecimal("2"));
    }

    @Test
    void foldingInsideOpaqueCollectionOperationDoesNotPublishCalculations() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .functionsFrom(new Functions(), FunctionPurity.FOLDABLE)
                .build();
        SemanticModel model = resolve("items := [1, 2]; items.map(@ -> track(3))", environment);

        assertMemoryEquivalent(model, environment, Map.of());

        ComputationWithMemory<Object> optimized = new ExecutionPlanBuilder().build(model, environment)
                .computeWithMemory(Map.of(), Clock.systemUTC());
        assertThat(optimized.memory().calculations()).isEmpty();
    }

    @Test
    void foldedRegisteredMemberChainKeepsPropertyArgumentAndMethodProvenance() {
        Account account = new Account(BigDecimal.TEN);
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol("account", new ObjectType(Account.class.getName()), account,
                        ExternalSymbolOverwritePolicy.FIXED)
                .registerJavaType(Account.class)
                .registerJavaTypeMethod(Account.class, "fee", FunctionPurity.PURE, BigDecimal.class)
                .functionsFrom(new Functions(), FunctionPurity.FOLDABLE)
                .build();
        SemanticModel model = resolve("account.amount + account.fee(track(1))", environment);

        assertMemoryEquivalent(model, environment, Map.of());

        ComputationWithMemory<Object> optimized = new ExecutionPlanBuilder().build(model, environment)
                .computeWithMemory(Map.of(), Clock.systemUTC());
        assertThat(optimized.memory().calculations()).extracting(entry -> entry.key().kind())
                .containsExactly(CalculationKind.PROPERTY, CalculationKind.FUNCTION, CalculationKind.METHOD);
        assertThat(optimized.memory().calculations()).extracting(CalculationEntry::value)
                .containsExactly(BigDecimal.TEN, BigDecimal.ONE, new BigDecimal("11"));
        assertThat(optimized.memory().variables()).singleElement().satisfies(variable -> {
            assertThat(variable.key().name()).isEqualTo("account");
            assertThat(variable.value()).isSameAs(account);
        });
    }

    @Test
    void elidedAssertionKeepsOnlyItsFoldedArgumentCalculation() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .functionsFrom(new Functions(), FunctionPurity.FOLDABLE)
                .build();
        SemanticModel model = resolve("asNumber(track(5))", environment);

        assertMemoryEquivalent(model, environment, Map.of());

        ComputationWithMemory<Object> optimized = new ExecutionPlanBuilder().build(model, environment)
                .computeWithMemory(Map.of(), Clock.systemUTC());
        assertThat(optimized.memory().calculations()).singleElement().satisfies(calculation -> {
            assertThat(calculation.key().name()).isEqualTo("track");
            assertThat(calculation.value()).isSameAs(optimized.result());
        });
    }

    @Test
    void foldedGroupKeepsItsOrderBetweenDynamicCalculationNeighbors() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .functionsFrom(new Functions(), FunctionPurity.FOLDABLE)
                .functionsFrom(new DynamicFunctions(), FunctionPurity.IMPURE)
                .build();
        SemanticModel model = resolve("dynamic(1) + track(track(2)) + dynamic(3)", environment);

        assertMemoryEquivalent(model, environment, Map.of());

        ComputationWithMemory<Object> optimized = new ExecutionPlanBuilder().build(model, environment)
                .computeWithMemory(Map.of(), Clock.systemUTC());
        assertThat(optimized.memory().calculations()).extracting(CalculationEntry::value)
                .containsExactly(
                        BigDecimal.ONE, new BigDecimal("2"), new BigDecimal("2"), new BigDecimal("3"));
    }

    @Test
    void failedOuterFoldKeepsTheExecutableFailureAfterAFoldedChild() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .functionsFrom(new Functions(), FunctionPurity.FOLDABLE)
                .build();
        SemanticModel model = resolve("fail(track(1))", environment);
        ExecutionPlanBuilder builder = new ExecutionPlanBuilder();

        ExpressionExecutionException optimized = catchExecutionFailure(builder.build(model, environment));
        ExpressionExecutionException oracle = catchExecutionFailure(builder.buildOracle(model, environment));

        assertThat(optimized.diagnostic()).isEqualTo(oracle.diagnostic());
    }

    @Test
    void assignmentOnlyExecutionPublishesItsFoldedCalculationGroup() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .functionsFrom(new Functions(), FunctionPurity.FOLDABLE)
                .build();
        SemanticModel model = resolve("assigned := track(track(2)); track(3)", environment);
        ExecutionPlanBuilder builder = new ExecutionPlanBuilder();

        ComputationWithMemory<Map<String, Object>> optimized = builder.build(model, environment)
                .computeAssignmentsWithMemory(Map.of(), Clock.systemUTC());
        ComputationWithMemory<Map<String, Object>> oracle = builder.buildOracle(model, environment)
                .computeAssignmentsWithMemory(Map.of(), Clock.systemUTC());

        assertThat(optimized.result()).isEqualTo(oracle.result());
        assertThat(optimized.memory().variables()).isEqualTo(oracle.memory().variables());
        assertThat(optimized.memory().calculations()).isEqualTo(oracle.memory().calculations());
        assertThat(optimized.memory().calculations()).extracting(CalculationEntry::value)
                .containsExactly(new BigDecimal("2"), new BigDecimal("2"));
    }

    private static ExpressionExecutionException catchExecutionFailure(ExecutionPlan plan) {
        try {
            plan.computeWithMemory(Map.of(), Clock.systemUTC());
            throw new AssertionError("expected expression execution to fail");
        } catch (ExpressionExecutionException failure) {
            return failure;
        }
    }

    private static void assertMemoryEquivalent(
            SemanticModel model, ExpressionEnvironment environment, Map<String, ?> overrides) {
        ExecutionPlanBuilder builder = new ExecutionPlanBuilder();
        ComputationWithMemory<Object> optimized = builder.build(model, environment)
                .computeWithMemory(overrides, Clock.systemUTC());
        ComputationWithMemory<Object> oracle = builder.buildOracle(model, environment)
                .computeWithMemory(overrides, Clock.systemUTC());

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

        public boolean trackBoolean(boolean value) {
            return value;
        }

        public BigDecimal fail(BigDecimal value) {
            throw new ArithmeticException("failure for " + value);
        }
    }

    public static final class DynamicFunctions {

        public BigDecimal dynamic(BigDecimal value) {
            return value;
        }
    }

    public static final class CountingFunctions {

        private final AtomicInteger invocations;

        CountingFunctions(AtomicInteger invocations) {
            this.invocations = invocations;
        }

        public BigDecimal count(BigDecimal value) {
            invocations.incrementAndGet();
            return value;
        }
    }

    public record Account(BigDecimal amount) {

        public BigDecimal fee(BigDecimal increment) {
            return amount.add(increment);
        }
    }

    public record NullableAccount(BigDecimal amount) {
    }
}
