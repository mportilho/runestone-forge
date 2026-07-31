package com.runestone.expeval_mk3.api;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Proves issue #97's runtime execution-failure contract through the public compile/compute
 * pipeline: every Phase 5 function-provider failure and return-contract violation surfaces as
 * {@link ExpressionExecutionException} with exactly one stable {@link ExpressionDiagnostic}, and
 * calls invoke only the already-resolved handle with left-to-right, stop-on-first-failure argument
 * evaluation.
 */
class ExpressionRuntimeProviderContractTest {

    @Test
    void wrapsADeclaredCheckedExceptionAsAProviderFailurePreservingCause() {
        CompiledExpression expression = ExpressionCompiler.compileOrThrow(
                "throwsChecked(1)", environmentFrom(FailingProviders.class));

        assertThatThrownBy(expression::compute)
                .isInstanceOf(ExpressionExecutionException.class)
                .hasCauseInstanceOf(IOException.class)
                .satisfies(exception -> assertThat(((ExpressionExecutionException) exception).diagnostic().code())
                        .isEqualTo("RUNTIME_PROVIDER_FAILURE"));
    }

    @Test
    void wrapsAnOrdinaryRuntimeExceptionAsAProviderFailurePreservingCause() {
        CompiledExpression expression = ExpressionCompiler.compileOrThrow(
                "throwsRuntime(1)", environmentFrom(FailingProviders.class));

        assertThatThrownBy(expression::compute)
                .isInstanceOf(ExpressionExecutionException.class)
                .hasCauseInstanceOf(IllegalStateException.class)
                .satisfies(exception -> {
                    ExpressionDiagnostic diagnostic = ((ExpressionExecutionException) exception).diagnostic();
                    assertThat(diagnostic.code()).isEqualTo("RUNTIME_PROVIDER_FAILURE");
                    assertThat(diagnostic.category()).isEqualTo(DiagnosticCategory.RUNTIME);
                    assertThat(diagnostic.severity()).isEqualTo(DiagnosticSeverity.ERROR);
                    assertThat(diagnostic.primarySpan()).isPresent();
                });
    }

    @Test
    void restoresInterruptedStatusBeforeWrappingAnInterruptedException() {
        CompiledExpression expression = ExpressionCompiler.compileOrThrow(
                "throwsInterrupted(1)", environmentFrom(FailingProviders.class));

        try {
            assertThatThrownBy(expression::compute)
                    .isInstanceOf(ExpressionExecutionException.class)
                    .hasCauseInstanceOf(InterruptedException.class);
            assertThat(Thread.interrupted()).isTrue();
        } finally {
            Thread.interrupted();
        }
    }

    @Test
    void propagatesAFatalJvmConditionUnwrapped() {
        CompiledExpression expression = ExpressionCompiler.compileOrThrow(
                "throwsFatal(1)", environmentFrom(FailingProviders.class));

        assertThatThrownBy(expression::compute).isInstanceOf(StackOverflowError.class);
    }

    @Test
    void rejectsANullProviderReturnAsADistinctReturnContractCode() {
        CompiledExpression expression = ExpressionCompiler.compileOrThrow(
                "returnsNull(1)", environmentFrom(ReturnContractProviders.class));

        assertThatThrownBy(expression::compute)
                .isInstanceOf(ExpressionExecutionException.class)
                .satisfies(exception -> assertThat(((ExpressionExecutionException) exception).diagnostic().code())
                        .isEqualTo("RUNTIME_RETURN_NULL"));
    }

    @Test
    void rejectsAProviderCollectionElementOfTheWrongTypeAsADistinctReturnContractCode() {
        CompiledExpression expression = ExpressionCompiler.compileOrThrow(
                "returnsListWithWrongElementType(1)", environmentFrom(ReturnContractProviders.class));

        assertThatThrownBy(expression::compute)
                .isInstanceOf(ExpressionExecutionException.class)
                .satisfies(exception -> assertThat(((ExpressionExecutionException) exception).diagnostic().code())
                        .isEqualTo("RUNTIME_RETURN_TYPE_MISMATCH"));
    }

    @Test
    void rejectsANestedInvalidContainerInsideAProviderCollectionReturnAsADistinctReturnContractCode() {
        CompiledExpression expression = ExpressionCompiler.compileOrThrow(
                "returnsNestedInvalidContainer(1)", environmentFrom(ReturnContractProviders.class));

        assertThatThrownBy(expression::compute)
                .isInstanceOf(ExpressionExecutionException.class)
                .satisfies(exception -> assertThat(((ExpressionExecutionException) exception).diagnostic().code())
                        .isEqualTo("RUNTIME_RETURN_INVALID_CONTAINER"));
    }

    @Test
    void rejectsANonStringMapKeyInAProviderMapReturnAsADistinctReturnContractCode() {
        CompiledExpression expression = ExpressionCompiler.compileOrThrow(
                "returnsMapWithNonStringKey(1)", environmentFrom(ReturnContractProviders.class));

        assertThatThrownBy(expression::compute)
                .isInstanceOf(ExpressionExecutionException.class)
                .satisfies(exception -> assertThat(((ExpressionExecutionException) exception).diagnostic().code())
                        .isEqualTo("RUNTIME_RETURN_INVALID_CONTAINER"));
    }

    @Test
    void rejectsAnOverLimitProviderCollectionReturnAsTheEnvironmentLimitCode() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .functionsFrom(ReturnContractProviders.class, FunctionPurity.IMPURE)
                .maxMaterializedSize(1)
                .build();
        CompiledExpression expression = ExpressionCompiler.compileOrThrow("returnsOverLimitList(1)", environment);

        assertThatThrownBy(expression::compute)
                .isInstanceOf(ExpressionExecutionException.class)
                .satisfies(exception -> assertThat(((ExpressionExecutionException) exception).diagnostic().code())
                        .isEqualTo("RUNTIME_MATERIALIZATION_LIMIT_EXCEEDED"));
    }

    @Test
    void evaluatesArgumentsLeftToRightAndStopsOnTheFirstFailure() {
        OrderRecordingProviders functions = new OrderRecordingProviders();
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .functionsFrom(functions, FunctionPurity.IMPURE)
                .build();
        CompiledExpression expression = ExpressionCompiler.compileOrThrow(
                "combine(track(1), boom(2), track(3))", environment);

        assertThatThrownBy(expression::compute).isInstanceOf(ExpressionExecutionException.class);

        assertThat(functions.order()).containsExactly(1);
    }

    @Test
    void invokesTheResolvedHandleDirectlyOnceForEachCallWithoutRediscovery() {
        CountingProvider functions = new CountingProvider();
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .functionsFrom(functions, FunctionPurity.IMPURE)
                .build();
        CompiledExpression expression = ExpressionCompiler.compileOrThrow("bump(1)", environment);

        for (int call = 1; call <= 5; call++) {
            expression.compute();
            assertThat(functions.invocations()).isEqualTo(call);
        }
    }

    private static ExpressionEnvironment environmentFrom(Class<?> providerClass) {
        return ExpressionEnvironment.builder().functionsFrom(providerClass, FunctionPurity.IMPURE).build();
    }

    @SuppressWarnings("unchecked")
    private static <T> T unsafeCast(Object value) {
        return (T) value;
    }

    public static final class FailingProviders {
        public static BigDecimal throwsChecked(BigDecimal value) throws IOException {
            throw new IOException("io failure");
        }

        public static BigDecimal throwsRuntime(BigDecimal value) {
            throw new IllegalStateException("boom");
        }

        public static BigDecimal throwsInterrupted(BigDecimal value) throws InterruptedException {
            throw new InterruptedException("interrupted");
        }

        public static BigDecimal throwsFatal(BigDecimal value) {
            throw new StackOverflowError();
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static final class ReturnContractProviders {
        public static BigDecimal returnsNull(BigDecimal value) {
            return null;
        }

        public static List<String> returnsListWithWrongElementType(BigDecimal value) {
            // Raw-typed on purpose: simulates a misbehaving provider whose declared generic element
            // type (String) does not match what it actually places in the list at runtime, which the
            // Java compiler cannot catch because generic element types are erased.
            List raw = new ArrayList();
            raw.add(BigDecimal.TEN);
            return raw;
        }

        public static List<List<BigDecimal>> returnsNestedInvalidContainer(BigDecimal value) {
            // Raw-typed on purpose: the outer list's declared element type is itself a collection,
            // but this misbehaving provider places a scalar in that slot instead of a nested list.
            List raw = new ArrayList();
            raw.add(BigDecimal.TEN);
            return raw;
        }

        public static Map<String, BigDecimal> returnsMapWithNonStringKey(BigDecimal value) {
            return unsafeCast(Map.of(1, BigDecimal.ONE));
        }

        public static List<BigDecimal> returnsOverLimitList(BigDecimal value) {
            return List.of(BigDecimal.ONE, BigDecimal.TWO);
        }
    }

    public static final class CountingProvider {
        private final AtomicInteger invocations = new AtomicInteger();

        public BigDecimal bump(BigDecimal value) {
            return BigDecimal.valueOf(invocations.incrementAndGet()).add(value);
        }

        public int invocations() {
            return invocations.get();
        }
    }

    public static final class OrderRecordingProviders {
        private final List<Integer> order = new ArrayList<>();
        private final AtomicInteger combineCalls = new AtomicInteger();

        public BigDecimal track(BigDecimal value) {
            order.add(value.intValue());
            return value;
        }

        public BigDecimal boom(BigDecimal value) {
            throw new IllegalStateException("boom");
        }

        public BigDecimal combine(BigDecimal a, BigDecimal b, BigDecimal c) {
            combineCalls.incrementAndGet();
            return a.add(b).add(c);
        }

        public List<Integer> order() {
            return order;
        }
    }
}
