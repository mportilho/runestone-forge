package com.runestone.expeval_mk3.internal.runtime;

import com.runestone.expeval_mk3.api.CollectionType;
import com.runestone.expeval_mk3.api.ExpressionDiagnostic;
import com.runestone.expeval_mk3.api.ExpressionExecutionException;
import com.runestone.expeval_mk3.api.FunctionDescriptor;
import com.runestone.expeval_mk3.api.FunctionPurity;
import com.runestone.expeval_mk3.api.ScalarType;
import com.runestone.expeval_mk3.internal.ast.NodeId;
import com.runestone.expeval_mk3.internal.semantics.ContextualMemberNavigationBinding;
import com.runestone.expeval_mk3.api.SourceSpan;
import java.util.function.Function;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Clock;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExpressionRuntimeTest {

    private static final SourceSpan SPAN = new SourceSpan(2, 13, 1, 3);
    private static final int CURRENT_ITEM_SLOT = 0;
    private static final CollectionType NUMBER_COLLECTION = new CollectionType(ScalarType.NUMBER);

    private static ExecutionScope newScope() {
        return new ExecutionScope(ExecutionScope.blankFrame(1), ZoneId.of("UTC"), Clock.systemUTC());
    }

    @Test
    void invokeFunctionWrapsProviderRuntimeExceptionPreservingCause() throws Exception {
        FunctionDescriptor descriptor = functionDescriptor("throwsRuntime");

        assertThatThrownBy(() -> ExpressionRuntime.invokeFunction(
                descriptor, List.of(constant(BigDecimal.ONE)), newScope(), SPAN))
                .isInstanceOf(ExpressionExecutionException.class)
                .hasCauseInstanceOf(IllegalStateException.class)
                .satisfies(exception -> assertThat(exception.getCause()).hasMessage("boom"))
                .satisfies(exception -> assertThat(((ExpressionExecutionException) exception).diagnostic().code())
                        .isEqualTo("RUNTIME_PROVIDER_FAILURE"));
    }

    @Test
    void invokeFunctionWrapsDeclaredCheckedExceptionPreservingCause() throws Exception {
        FunctionDescriptor descriptor = functionDescriptor("throwsChecked");

        assertThatThrownBy(() -> ExpressionRuntime.invokeFunction(
                descriptor, List.of(constant(BigDecimal.ONE)), newScope(), SPAN))
                .isInstanceOf(ExpressionExecutionException.class)
                .hasCauseInstanceOf(IOException.class);
    }

    @Test
    void invokeFunctionRestoresInterruptStatusBeforeWrapping() throws Exception {
        FunctionDescriptor descriptor = functionDescriptor("throwsInterrupted");

        try {
            assertThatThrownBy(() -> ExpressionRuntime.invokeFunction(
                    descriptor, List.of(constant(BigDecimal.ONE)), newScope(), SPAN))
                    .isInstanceOf(ExpressionExecutionException.class)
                    .hasCauseInstanceOf(InterruptedException.class);
            assertThat(Thread.interrupted()).isTrue();
        } finally {
            Thread.interrupted();
        }
    }

    @Test
    void invokeFunctionPropagatesFatalErrorsUnchanged() throws Exception {
        FunctionDescriptor descriptor = functionDescriptor("throwsFatal");

        assertThatThrownBy(() -> ExpressionRuntime.invokeFunction(
                descriptor, List.of(constant(BigDecimal.ONE)), newScope(), SPAN))
                .isInstanceOf(StackOverflowError.class);
    }

    @Test
    void invokeFunctionRejectsNullArgumentWithForbiddenNullDiagnostic() throws Exception {
        FunctionDescriptor descriptor = functionDescriptor("throwsRuntime");

        assertThatThrownBy(() -> ExpressionRuntime.invokeFunction(
                descriptor, List.of(constant(null)), newScope(), SPAN))
                .isInstanceOf(ExpressionExecutionException.class)
                .satisfies(exception -> assertThat(((ExpressionExecutionException) exception).diagnostic().code())
                        .isEqualTo("RUNTIME_FORBIDDEN_NULL"));
    }

    @Test
    void invokeFunctionCallsArityZeroThroughGeneratedEntryPoint() throws Exception {
        Method method = FunctionUnderTest.class.getDeclaredMethod("arityZero");
        FunctionDescriptor descriptor = FunctionDescriptor.fromMethod(
                "arityZero", method, List.of(), ScalarType.NUMBER, FunctionPurity.PURE);

        Object result = ExpressionRuntime.invokeFunction(descriptor, List.of(), newScope(), SPAN);

        assertThat(result).isEqualTo(BigDecimal.valueOf(42));
    }

    @Test
    void invokeFunctionCallsArityTwoThroughGeneratedEntryPoint() throws Exception {
        Method method = FunctionUnderTest.class.getDeclaredMethod("sumTwo", BigDecimal.class, BigDecimal.class);
        FunctionDescriptor descriptor = FunctionDescriptor.fromMethod(
                "sumTwo", method, List.of(ScalarType.NUMBER, ScalarType.NUMBER), ScalarType.NUMBER,
                FunctionPurity.PURE);

        Object result = ExpressionRuntime.invokeFunction(
                descriptor, List.of(constant(BigDecimal.ONE), constant(BigDecimal.TEN)), newScope(), SPAN);

        assertThat(result).isEqualTo(BigDecimal.valueOf(11));
    }

    @Test
    void invokeFunctionCallsArityFourThroughGeneratedEntryPoint() throws Exception {
        Method method = FunctionUnderTest.class.getDeclaredMethod(
                "sumFour", BigDecimal.class, BigDecimal.class, BigDecimal.class, BigDecimal.class);
        FunctionDescriptor descriptor = FunctionDescriptor.fromMethod(
                "sumFour",
                method,
                List.of(ScalarType.NUMBER, ScalarType.NUMBER, ScalarType.NUMBER, ScalarType.NUMBER),
                ScalarType.NUMBER,
                FunctionPurity.PURE);

        Object result = ExpressionRuntime.invokeFunction(
                descriptor,
                List.of(constant(BigDecimal.ONE), constant(BigDecimal.TWO), constant(BigDecimal.valueOf(3)),
                        constant(BigDecimal.valueOf(4))),
                newScope(),
                SPAN);

        assertThat(result).isEqualTo(BigDecimal.valueOf(10));
    }

    @Test
    void invokeFunctionCallsArityAboveFourThroughArrayEntryPoint() throws Exception {
        Method method = FunctionUnderTest.class.getDeclaredMethod(
                "sumFive", BigDecimal.class, BigDecimal.class, BigDecimal.class, BigDecimal.class, BigDecimal.class);
        FunctionDescriptor descriptor = FunctionDescriptor.fromMethod(
                "sumFive",
                method,
                List.of(ScalarType.NUMBER, ScalarType.NUMBER, ScalarType.NUMBER, ScalarType.NUMBER,
                        ScalarType.NUMBER),
                ScalarType.NUMBER,
                FunctionPurity.PURE);

        Object result = ExpressionRuntime.invokeFunction(
                descriptor,
                List.of(constant(BigDecimal.ONE), constant(BigDecimal.ONE), constant(BigDecimal.ONE),
                        constant(BigDecimal.ONE), constant(BigDecimal.ONE)),
                newScope(),
                SPAN);

        assertThat(result).isEqualTo(BigDecimal.valueOf(5));
    }

    @Test
    void invokeFunctionClassifiesFailureIdenticallyAboveAndBelowTheDirectArityCutoff() throws Exception {
        Method fourArgMethod = FunctionUnderTest.class.getDeclaredMethod(
                "throwsRuntimeArityFour", BigDecimal.class, BigDecimal.class, BigDecimal.class, BigDecimal.class);
        FunctionDescriptor fourArgDescriptor = FunctionDescriptor.fromMethod(
                "throwsRuntimeArityFour",
                fourArgMethod,
                List.of(ScalarType.NUMBER, ScalarType.NUMBER, ScalarType.NUMBER, ScalarType.NUMBER),
                ScalarType.NUMBER,
                FunctionPurity.IMPURE);
        Method fiveArgMethod = FunctionUnderTest.class.getDeclaredMethod(
                "throwsRuntimeArityFive", BigDecimal.class, BigDecimal.class, BigDecimal.class, BigDecimal.class,
                BigDecimal.class);
        FunctionDescriptor fiveArgDescriptor = FunctionDescriptor.fromMethod(
                "throwsRuntimeArityFive",
                fiveArgMethod,
                List.of(ScalarType.NUMBER, ScalarType.NUMBER, ScalarType.NUMBER, ScalarType.NUMBER,
                        ScalarType.NUMBER),
                ScalarType.NUMBER,
                FunctionPurity.IMPURE);
        List<ExecutableNode> four = List.of(
                constant(BigDecimal.ONE), constant(BigDecimal.ONE), constant(BigDecimal.ONE), constant(BigDecimal.ONE));
        List<ExecutableNode> five = List.of(
                constant(BigDecimal.ONE), constant(BigDecimal.ONE), constant(BigDecimal.ONE), constant(BigDecimal.ONE),
                constant(BigDecimal.ONE));

        assertThatThrownBy(() -> ExpressionRuntime.invokeFunction(fourArgDescriptor, four, newScope(), SPAN))
                .isInstanceOf(ExpressionExecutionException.class)
                .hasCauseInstanceOf(IllegalStateException.class)
                .satisfies(exception -> assertThat(((ExpressionExecutionException) exception).diagnostic().code())
                        .isEqualTo("RUNTIME_PROVIDER_FAILURE"));
        assertThatThrownBy(() -> ExpressionRuntime.invokeFunction(fiveArgDescriptor, five, newScope(), SPAN))
                .isInstanceOf(ExpressionExecutionException.class)
                .hasCauseInstanceOf(IllegalStateException.class)
                .satisfies(exception -> assertThat(((ExpressionExecutionException) exception).diagnostic().code())
                        .isEqualTo("RUNTIME_PROVIDER_FAILURE"));
    }

    private static ExecutableNode constant(Object value) {
        return node(scope -> value);
    }

    private static ExecutableNode node(Function<ExecutionScope, Object> delegate) {
        return new DelegatingTestNode(new NodeId(1), SPAN, delegate);
    }

    /** Minimal test-only {@link ExecutableNode}: production code names a real class per node shape instead. */
    private record DelegatingTestNode(NodeId id, SourceSpan sourceSpan, Function<ExecutionScope, Object> delegate)
            implements ExecutableNode {

        @Override
        public Object execute(ExecutionScope scope) {
            return delegate.apply(scope);
        }
    }

    private static FunctionDescriptor functionDescriptor(String methodName) throws NoSuchMethodException {
        Method method = FunctionUnderTest.class.getDeclaredMethod(methodName, BigDecimal.class);
        return FunctionDescriptor.fromMethod(
                methodName, method, List.of(ScalarType.NUMBER), ScalarType.NUMBER, FunctionPurity.IMPURE);
    }

    public static final class FunctionUnderTest {
        public static BigDecimal throwsRuntime(BigDecimal value) {
            throw new IllegalStateException("boom");
        }

        public static BigDecimal throwsChecked(BigDecimal value) throws IOException {
            throw new IOException("io failure");
        }

        public static BigDecimal throwsInterrupted(BigDecimal value) throws InterruptedException {
            throw new InterruptedException("interrupted");
        }

        public static BigDecimal throwsFatal(BigDecimal value) {
            throw new StackOverflowError();
        }

        public static BigDecimal arityZero() {
            return BigDecimal.valueOf(42);
        }

        public static BigDecimal sumTwo(BigDecimal a, BigDecimal b) {
            return a.add(b);
        }

        public static BigDecimal sumFour(BigDecimal a, BigDecimal b, BigDecimal c, BigDecimal d) {
            return a.add(b).add(c).add(d);
        }

        public static BigDecimal sumFive(BigDecimal a, BigDecimal b, BigDecimal c, BigDecimal d, BigDecimal e) {
            return a.add(b).add(c).add(d).add(e);
        }

        public static BigDecimal throwsRuntimeArityFour(BigDecimal a, BigDecimal b, BigDecimal c, BigDecimal d) {
            throw new IllegalStateException("boom");
        }

        public static BigDecimal throwsRuntimeArityFive(
                BigDecimal a, BigDecimal b, BigDecimal c, BigDecimal d, BigDecimal e) {
            throw new IllegalStateException("boom");
        }
    }

    @Test
    void mapStringKeySubscriptRejectsNullValuesAtRuntime() {
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("present", null);

        assertThatThrownBy(() -> ExpressionRuntime.mapKeyValue(values, "present", false, SPAN))
                .isInstanceOf(ExpressionExecutionException.class)
                .satisfies(exception -> assertThat(((ExpressionExecutionException) exception).diagnostic().code())
                        .isEqualTo("RUNTIME_FORBIDDEN_NULL"));
    }

    @Test
    void strictMapStringKeySubscriptFailsWithMapKeyNotFoundDiagnostic() {
        assertThatThrownBy(() -> ExpressionRuntime.mapKeyValue(
                Map.of("present", BigDecimal.ONE), "missing", false, SPAN))
                .isInstanceOf(ExpressionExecutionException.class)
                .satisfies(exception -> {
                    ExpressionDiagnostic diagnostic = ((ExpressionExecutionException) exception).diagnostic();
                    assertThat(diagnostic.code()).isEqualTo("RUNTIME_MAP_KEY_NOT_FOUND");
                    assertThat(diagnostic.primarySpan()).contains(SPAN);
                });
    }

    @Test
    void safeMapStringKeySubscriptToleratesAbsentKeyAndNullReceiver() {
        assertThat(ExpressionRuntime.mapKeyValue(null, "present", true, SPAN)).isNull();
        assertThat(ExpressionRuntime.mapKeyValue(Map.of("present", BigDecimal.ONE), "missing", true, SPAN)).isNull();
    }

    @Test
    void strictIndexSubscriptFailsWithSubscriptOutOfBoundsDiagnostic() {
        assertThatThrownBy(() -> ExpressionRuntime.indexedValue(
                List.of(BigDecimal.ONE), BigInteger.valueOf(7), false, SPAN))
                .isInstanceOf(ExpressionExecutionException.class)
                .satisfies(exception -> {
                    ExpressionDiagnostic diagnostic = ((ExpressionExecutionException) exception).diagnostic();
                    assertThat(diagnostic.code()).isEqualTo("RUNTIME_SUBSCRIPT_OUT_OF_BOUNDS");
                    assertThat(diagnostic.primarySpan()).contains(SPAN);
                });
    }

    @Test
    void safeIndexSubscriptToleratesOutOfRangeIndex() {
        assertThat(ExpressionRuntime.indexedValue(List.of(BigDecimal.ONE), BigInteger.valueOf(7), true, SPAN)).isNull();
        assertThat(ExpressionRuntime.indexedValue(List.of(BigDecimal.ONE), BigInteger.valueOf(-7), true, SPAN)).isNull();
        assertThat(ExpressionRuntime.indexedValue(List.of(BigDecimal.ONE), BigInteger.valueOf(-1), true, SPAN))
                .isEqualTo(BigDecimal.ONE);
    }

    @Test
    void sliceSubscriptClampsBoundsIdenticallyOnBothLinkForms() {
        List<Object> values = List.of(BigDecimal.ONE, BigDecimal.TWO, BigDecimal.TEN);

        for (boolean safe : new boolean[] {false, true}) {
            assertThat(slice(values, BigInteger.valueOf(-2), null, safe))
                    .containsExactly(BigDecimal.TWO, BigDecimal.TEN);
            assertThat(slice(values, BigInteger.valueOf(2), BigInteger.ONE, safe)).isEmpty();
            assertThat(slice(values, BigInteger.valueOf(9), BigInteger.valueOf(99), safe)).isEmpty();
            assertThat(slice(values, null, null, safe)).isEqualTo(values);
        }
    }

    @SuppressWarnings("unchecked")
    private static List<Object> slice(List<Object> values, BigInteger start, BigInteger end, boolean safe) {
        return (List<Object>) ExpressionRuntime.slicedValues(values, start, end, safe, 10, SPAN);
    }

    @Test
    void sortByRejectsComputedDirectionOutsideAscendingAndDescending() {
        ExecutionScope scope = newScope();
        ExecutableLambda selector = new ExecutableLambda(
                node(executionScope -> executionScope.read(CURRENT_ITEM_SLOT)), CURRENT_ITEM_SLOT);

        assertThatThrownBy(() -> ExpressionRuntime.sortBy(
                List.of(BigDecimal.ONE), "cima", selector, scope, 10, ScalarType.NUMBER, SPAN))
                .isInstanceOf(ExpressionExecutionException.class)
                .satisfies(exception -> {
                    ExpressionDiagnostic diagnostic = ((ExpressionExecutionException) exception).diagnostic();
                    assertThat(diagnostic.code()).isEqualTo("RUNTIME_INVALID_OPERATION_ARGUMENT");
                    assertThat(diagnostic.primarySpan()).contains(SPAN);
                });
    }

    @Test
    void materializationLimitFailureCarriesTheLinkSourceSpan() {
        ExecutionScope scope = newScope();
        ExecutableLambda identity = new ExecutableLambda(
                node(executionScope -> executionScope.read(CURRENT_ITEM_SLOT)), CURRENT_ITEM_SLOT);

        assertThatThrownBy(() -> ExpressionRuntime.map(
                List.of(BigDecimal.ONE, BigDecimal.TWO), identity, scope, NUMBER_COLLECTION, 1, SPAN))
                .isInstanceOf(ExpressionExecutionException.class)
                .satisfies(exception -> {
                    ExpressionDiagnostic diagnostic = ((ExpressionExecutionException) exception).diagnostic();
                    assertThat(diagnostic.code()).isEqualTo("RUNTIME_MATERIALIZATION_LIMIT_EXCEEDED");
                    assertThat(diagnostic.primarySpan()).contains(SPAN);
                });
    }

    @Test
    void allReturnsTrueWhenEveryElementSatisfiesLambda() {
        ExecutionScope scope = newScope();
        ExecutableLambda isPositive = new ExecutableLambda(
                positiveCheckNode(), CURRENT_ITEM_SLOT);

        boolean result = ExpressionRuntime.all(
                List.of(BigDecimal.ONE, BigDecimal.TEN), isPositive, scope, NUMBER_COLLECTION, SPAN);

        assertThat(result).isTrue();
    }

    @Test
    void allReturnsFalseWhenAnyElementFailsLambda() {
        ExecutionScope scope = newScope();
        ExecutableLambda isPositive = new ExecutableLambda(
                positiveCheckNode(), CURRENT_ITEM_SLOT);

        boolean result = ExpressionRuntime.all(
                List.of(BigDecimal.ONE, BigDecimal.valueOf(-1)), isPositive, scope, NUMBER_COLLECTION, SPAN);

        assertThat(result).isFalse();
    }

    @Test
    void anyReturnsTrueWhenSomeElementSatisfiesLambda() {
        ExecutionScope scope = newScope();
        ExecutableLambda isPositive = new ExecutableLambda(
                positiveCheckNode(), CURRENT_ITEM_SLOT);

        boolean result = ExpressionRuntime.any(
                List.of(BigDecimal.valueOf(-1), BigDecimal.TEN), isPositive, scope, NUMBER_COLLECTION, SPAN);

        assertThat(result).isTrue();
    }

    @Test
    void anyReturnsFalseWhenNoElementSatisfiesLambda() {
        ExecutionScope scope = newScope();
        ExecutableLambda isPositive = new ExecutableLambda(
                positiveCheckNode(), CURRENT_ITEM_SLOT);

        boolean result = ExpressionRuntime.any(
                List.of(BigDecimal.valueOf(-1), BigDecimal.valueOf(-2)), isPositive, scope, NUMBER_COLLECTION, SPAN);

        assertThat(result).isFalse();
    }

    @Test
    void mapAppliesLambdaToEveryElement() {
        ExecutionScope scope = newScope();
        ExecutableNode doubleValue = node(executionScope ->
                ExpressionRuntime.number(executionScope.read(CURRENT_ITEM_SLOT)).multiply(BigDecimal.TWO));
        ExecutableLambda lambda = new ExecutableLambda(doubleValue, CURRENT_ITEM_SLOT);

        List<Object> result = ExpressionRuntime.map(
                List.of(BigDecimal.ONE, BigDecimal.TEN), lambda, scope, NUMBER_COLLECTION, 10, SPAN);

        assertThat(result).containsExactly(BigDecimal.valueOf(2), BigDecimal.valueOf(20));
    }

    @Test
    void reduceAccumulatesAcrossElementsUsingLambda() {
        ExecutionScope scope = newScope();
        ExecutableNode sumStep = node(executionScope -> {
            Object reductionItem = executionScope.read(CURRENT_ITEM_SLOT);
            BigDecimal accumulator = ExpressionRuntime.number(ExpressionRuntime.contextualMemberValue(
                    reductionItem, ContextualMemberNavigationBinding.Member.REDUCTION_ACCUMULATOR, false, SPAN));
            BigDecimal item = ExpressionRuntime.number(ExpressionRuntime.contextualMemberValue(
                    reductionItem, ContextualMemberNavigationBinding.Member.REDUCTION_ITEM, false, SPAN));
            return accumulator.add(item);
        });
        ExecutableLambda lambda = new ExecutableLambda(sumStep, CURRENT_ITEM_SLOT);

        Object result = ExpressionRuntime.reduce(
                List.of(BigDecimal.ONE, BigDecimal.TEN), BigDecimal.ZERO, lambda, scope, SPAN);

        assertThat(result).isEqualTo(BigDecimal.valueOf(11));
    }

    @Test
    void sortByOrdersAscendingAndDescendingByLambdaKey() {
        ExecutionScope scope = newScope();
        ExecutableNode identity = node(executionScope -> executionScope.read(CURRENT_ITEM_SLOT));
        ExecutableLambda selector = new ExecutableLambda(identity, CURRENT_ITEM_SLOT);
        List<Object> values = List.of(BigDecimal.TEN, BigDecimal.ONE, BigDecimal.valueOf(5));

        List<Object> ascending = ExpressionRuntime.sortBy(values, "asc", selector, scope, 10, ScalarType.NUMBER, SPAN);
        List<Object> descending = ExpressionRuntime.sortBy(
                values, "desc", selector, scope, 10, ScalarType.NUMBER, SPAN);

        assertThat(ascending).containsExactly(BigDecimal.ONE, BigDecimal.valueOf(5), BigDecimal.TEN);
        assertThat(descending).containsExactly(BigDecimal.TEN, BigDecimal.valueOf(5), BigDecimal.ONE);
    }

    private static ExecutableNode positiveCheckNode() {
        return node(executionScope -> ExpressionRuntime.number(executionScope.read(CURRENT_ITEM_SLOT))
                .compareTo(BigDecimal.ZERO) > 0);
    }
}
