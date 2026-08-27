package com.runestone.expeval_mk3.api;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;

class CalculationMemoryRegisteredMembersTest {

    @Test
    void publishesRegisteredPropertiesAndMethodsInEvaluationOrder() {
        String source = "city := account.address.city; amount := account.amount; account.fee(2)";
        CompiledExpression expression = ExpressionEngine.defaultEngine().compileOrThrow(source, environment());

        ComputationWithMemory<Object> computation = expression.asResult().computeWithMemory();

        assertThat(computation.result()).isEqualTo(new BigDecimal("12"));
        assertThat(computation.memory().calculations())
                .extracting(entry -> entry.key().kind(), entry -> entry.key().name(), CalculationEntry::value)
                .containsExactly(
                        tuple(CalculationKind.PROPERTY, "address", ACCOUNT.address()),
                        tuple(CalculationKind.PROPERTY, "city", "Manaus"),
                        tuple(CalculationKind.PROPERTY, "amount", BigDecimal.TEN),
                        tuple(CalculationKind.METHOD, "fee", new BigDecimal("12")));
        assertThat(computation.memory().calculations())
                .extracting(entry -> spanText(source, entry.key().sourceSpan()))
                .containsExactly(".address", ".city", ".amount", ".fee(2)");
        assertThat(computation.memory().calculations())
                .extracting(entry -> entry.key().nodeId())
                .doesNotHaveDuplicates();
    }

    @Test
    void publishesOnlyTheReachedMemberAndCapturesSafeNull() {
        ExpressionEnvironment environment = environment();
        CompiledExpression branch = ExpressionEngine.defaultEngine().compileOrThrow(
                "if enabled then account.amount else account.fee(3) endif", environment);
        CompiledExpression safe = ExpressionEngine.defaultEngine().compileOrThrow(
                "accounts?.[0]?.amount ?? 0", environment);

        CalculationMemory propertyMemory = branch.asMath()
                .computeWithMemory(Map.of("enabled", true))
                .memory();
        CalculationMemory methodMemory = branch.asMath()
                .computeWithMemory(Map.of("enabled", false))
                .memory();
        CalculationMemory safeNullMemory = safe.asMath()
                .computeWithMemory(Map.of("accounts", List.of()))
                .memory();

        assertThat(propertyMemory.calculations())
                .extracting(entry -> entry.key().kind(), CalculationEntry::value)
                .containsExactly(tuple(CalculationKind.PROPERTY, BigDecimal.TEN));
        assertThat(methodMemory.calculations())
                .extracting(entry -> entry.key().kind(), CalculationEntry::value)
                .containsExactly(tuple(CalculationKind.METHOD, new BigDecimal("13")));
        assertThat(safeNullMemory.calculations())
                .extracting(entry -> entry.key().kind(), CalculationEntry::value)
                .containsExactly(tuple(CalculationKind.PROPERTY, null));
    }

    @Test
    void safeNullMethodReceiverSkipsArgumentsAndPublishesTheReachedNullBoundary() {
        TrackingFunctions functions = new TrackingFunctions();
        CompiledExpression expression = ExpressionEngine.defaultEngine().compileOrThrow(
                "accounts?.[0]?.fee(mark(1)) ?? 0", environment(functions));

        ComputationWithMemory<BigDecimal> computation = expression.asMath()
                .computeWithMemory(Map.of("accounts", List.of()));

        assertThat(computation.result()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(functions.values()).isEmpty();
        assertThat(computation.memory().calculations())
                .extracting(entry -> entry.key().kind(), entry -> entry.key().name(), CalculationEntry::value)
                .containsExactly(tuple(CalculationKind.METHOD, "fee", null));
    }

    @Test
    void collectionFiltersAndLambdaBodiesAreOpaqueBetweenVisibleCalls() {
        TrackingFunctions functions = new TrackingFunctions();
        ExpressionEnvironment environment = environment(functions);
        String source = "before := mark(1); accounts[?(@.active)].map(@ -> @.fee(mark(@.amount))).sum() + mark(2)";
        CompiledExpression expression = ExpressionEngine.defaultEngine().compileOrThrow(source, environment);

        ComputationWithMemory<BigDecimal> computation = expression.asMath().computeWithMemory();

        assertThat(computation.result()).isEqualByComparingTo("22");
        assertThat(computation.memory().calculations())
                .extracting(entry -> entry.key().kind(), entry -> entry.key().name(), CalculationEntry::value)
                .containsExactly(
                        tuple(CalculationKind.FUNCTION, "mark", BigDecimal.ONE),
                        tuple(CalculationKind.FUNCTION, "mark", new BigDecimal("2")));
        assertThat(functions.values()).containsExactly(BigDecimal.ONE, BigDecimal.TEN, new BigDecimal("2"));
    }

    @Test
    void nestedLambdasAndCurrentTemporalValuesInsideCollectionOperationsAreOpaque() {
        CompiledExpression nested = ExpressionEngine.defaultEngine().compileOrThrow(
                "accounts.map(@ -> @.children.map(@ -> @.fee(1)))", environment());
        CompiledExpression temporal = ExpressionEngine.defaultEngine().compileOrThrow(
                "accounts.map(@ -> currDate)", environment());

        ComputationWithMemory<Object> nestedResult = nested.asResult().computeWithMemory();
        ComputationWithMemory<Object> temporalResult = temporal.asResult().computeWithMemory();

        assertThat(nestedResult.result()).isEqualTo(List.of(List.of(new BigDecimal("6"))));
        assertThat(nestedResult.memory().calculations()).isEmpty();
        assertThat((List<?>) temporalResult.result()).hasSize(1);
        assertThat(temporalResult.memory().calculations()).isEmpty();
    }

    @Test
    void safeNavigationDoesNotHideRegisteredMemberFailures() {
        CompiledExpression expression = ExpressionEngine.defaultEngine().compileOrThrow(
                "account?.fail() ?? 0", environment());

        assertThatThrownBy(() -> expression.asResult().computeWithMemory())
                .isInstanceOf(ExpressionExecutionException.class)
                .satisfies(thrown -> assertThat(((ExpressionExecutionException) thrown).diagnostic().code())
                        .isEqualTo("RUNTIME_MEMBER_ACCESS_FAILURE"))
                .hasRootCauseMessage("member failed");
    }

    private static ExpressionEnvironment environment() {
        return environment(null);
    }

    private static ExpressionEnvironment environment(TrackingFunctions functions) {
        ExpressionEnvironment.Builder builder = ExpressionEnvironment.builder()
                .externalSymbol("enabled", false, ExternalSymbolOverwritePolicy.OVERRIDABLE)
                .externalSymbol("account", ACCOUNT, ExternalSymbolOverwritePolicy.OVERRIDABLE)
                .externalSymbol(
                        "accounts",
                        new CollectionType(new ObjectType(Account.class.getName())),
                        List.of(ACCOUNT),
                        ExternalSymbolOverwritePolicy.OVERRIDABLE)
                .registerJavaType(Account.class)
                .registerJavaType(Address.class)
                .registerJavaTypeMethod(Account.class, "fee", FunctionPurity.IMPURE, BigDecimal.class)
                .registerJavaTypeMethod(Account.class, "fail", FunctionPurity.IMPURE);
        if (functions != null) {
            builder.functionsFrom(functions, FunctionPurity.IMPURE);
        }
        return builder.build();
    }

    private static String spanText(String source, SourceSpan span) {
        return source.substring(span.offset(), span.endOffset());
    }

    private static final Address ADDRESS = new Address("Manaus");
    private static final Account CHILD = new Account(BigDecimal.valueOf(5), true, ADDRESS, List.of());
    private static final Account ACCOUNT = new Account(BigDecimal.TEN, true, ADDRESS, List.of(CHILD));

    public record Address(String city) {
    }

    public record Account(BigDecimal amount, boolean active, Address address, List<Account> children) {

        public BigDecimal fee(BigDecimal increment) {
            return amount.add(increment);
        }

        public BigDecimal fail() {
            throw new IllegalStateException("member failed");
        }
    }

    public static final class TrackingFunctions {

        private final ArrayList<BigDecimal> values = new ArrayList<>();

        public BigDecimal mark(BigDecimal value) {
            values.add(value);
            return value;
        }

        List<BigDecimal> values() {
            return List.copyOf(values);
        }
    }
}
