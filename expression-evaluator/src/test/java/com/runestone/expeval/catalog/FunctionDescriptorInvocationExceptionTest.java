package com.runestone.expeval.catalog;

import com.runestone.expeval.api.FunctionInvocationException;
import com.runestone.expeval.types.ResolvedTypes;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatNoException;

@DisplayName("FunctionDescriptor – invocation exception handling")
class FunctionDescriptorInvocationExceptionTest {

    // --- Fixtures ---

    static BigDecimal succeedingFunction(BigDecimal x) {
        return x.multiply(BigDecimal.TWO);
    }

    static BigDecimal throwingRuntimeException(BigDecimal x) {
        throw new ArithmeticException("division by zero");
    }

    static BigDecimal throwingIllegalState(BigDecimal x) {
        throw new IllegalStateException("something went wrong");
    }

    static BigDecimal throwingNullPointer(BigDecimal x) {
        throw new NullPointerException("null input");
    }

    // checked exception wrapped in a static helper via reflection trick is not needed;
    // we use a separate method that throws a checked exception declared via throws
    @SuppressWarnings("RedundantThrows")
    static BigDecimal throwingCheckedException(BigDecimal x) throws Exception {
        throw new Exception("checked failure");
    }

    // --- Helpers ---

    private static FunctionDescriptor descriptorFor(String methodName) throws NoSuchMethodException, IllegalAccessException {
        var method = FunctionDescriptorInvocationExceptionTest.class
                .getDeclaredMethod(methodName, BigDecimal.class);
        var handle = MethodHandles.lookup().unreflect(method);
        return new FunctionDescriptor(
                methodName,
                List.of(BigDecimal.class),
                List.of(ResolvedTypes.fromJavaType(BigDecimal.class)),
                ResolvedTypes.fromJavaType(BigDecimal.class),
                handle
        );
    }

    // --- Tests ---

    @Nested
    @DisplayName("Happy path")
    class HappyPath {

        @Test
        @DisplayName("Returns result when function executes successfully")
        void returnsResultOnSuccess() throws Exception {
            var descriptor = descriptorFor("succeedingFunction");
            var result = descriptor.invoke(new Object[]{new BigDecimal("3")});
            assertThat(result).isEqualTo(new BigDecimal("6"));
        }

        @Test
        @DisplayName("No exception is thrown when function executes successfully")
        void noExceptionOnSuccess() throws Exception {
            var descriptor = descriptorFor("succeedingFunction");
            assertThatNoException().isThrownBy(() -> descriptor.invoke(new Object[]{BigDecimal.ONE}));
        }
    }

    @Nested
    @DisplayName("RuntimeException thrown by function")
    class RuntimeExceptions {

        @Test
        @DisplayName("ArithmeticException is wrapped in FunctionInvocationException")
        void arithmeticExceptionIsWrapped() throws Exception {
            var descriptor = descriptorFor("throwingRuntimeException");
            assertThatThrownBy(() -> descriptor.invoke(new Object[]{BigDecimal.ONE}))
                    .isInstanceOf(FunctionInvocationException.class)
                    .hasCauseInstanceOf(ArithmeticException.class);
        }

        @Test
        @DisplayName("IllegalStateException is wrapped in FunctionInvocationException")
        void illegalStateExceptionIsWrapped() throws Exception {
            var descriptor = descriptorFor("throwingIllegalState");
            assertThatThrownBy(() -> descriptor.invoke(new Object[]{BigDecimal.ONE}))
                    .isInstanceOf(FunctionInvocationException.class)
                    .hasCauseInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("NullPointerException is wrapped in FunctionInvocationException")
        void nullPointerExceptionIsWrapped() throws Exception {
            var descriptor = descriptorFor("throwingNullPointer");
            assertThatThrownBy(() -> descriptor.invoke(new Object[]{BigDecimal.ONE}))
                    .isInstanceOf(FunctionInvocationException.class)
                    .hasCauseInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("Checked exception thrown by function")
    class CheckedExceptions {

        @Test
        @DisplayName("Checked exception is wrapped in FunctionInvocationException")
        void checkedExceptionIsWrapped() throws Exception {
            var descriptor = descriptorFor("throwingCheckedException");
            assertThatThrownBy(() -> descriptor.invoke(new Object[]{BigDecimal.ONE}))
                    .isInstanceOf(FunctionInvocationException.class)
                    .hasCauseInstanceOf(Exception.class)
                    .getCause().hasMessage("checked failure");
        }
    }

    @Nested
    @DisplayName("Exception message and cause")
    class ExceptionContent {

        @Test
        @DisplayName("Exception message contains function name")
        void messageContainsFunctionName() throws Exception {
            var descriptor = descriptorFor("throwingRuntimeException");
            assertThatThrownBy(() -> descriptor.invoke(new Object[]{BigDecimal.ONE}))
                    .isInstanceOf(FunctionInvocationException.class)
                    .hasMessageContaining("throwingRuntimeException");
        }

        @Test
        @DisplayName("FunctionInvocationException exposes function name via functionName()")
        void exposesFunctionNameAccessor() throws Exception {
            var descriptor = descriptorFor("throwingRuntimeException");
            assertThatThrownBy(() -> descriptor.invoke(new Object[]{BigDecimal.ONE}))
                    .isInstanceOf(FunctionInvocationException.class)
                    .satisfies(ex -> assertThat(((FunctionInvocationException) ex).functionName())
                            .isEqualTo("throwingRuntimeException"));
        }

        @Test
        @DisplayName("Cause is the original exception thrown by the function")
        void causeIsOriginalException() throws Exception {
            var descriptor = descriptorFor("throwingIllegalState");
            assertThatThrownBy(() -> descriptor.invoke(new Object[]{BigDecimal.ONE}))
                    .isInstanceOf(FunctionInvocationException.class)
                    .cause()
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("something went wrong");
        }
    }

    @Nested
    @DisplayName("Error propagation – not wrapped")
    class ErrorPropagation {

        // Errors represent catastrophic JVM conditions (OutOfMemoryError, StackOverflowError, etc.)
        // and must never be swallowed or re-wrapped — they propagate as-is.
        // This test uses a lightweight Error subclass to avoid actually crashing the JVM.

        static class TestError extends Error {
            TestError() {
                super("simulated JVM error");
            }
        }

        static BigDecimal throwingError(BigDecimal x) {
            throw new TestError();
        }

        @Test
        @DisplayName("Error propagates directly without being wrapped")
        void errorIsNotWrapped() throws Exception {
            var method = ErrorPropagation.class.getDeclaredMethod("throwingError", BigDecimal.class);
            var handle = MethodHandles.lookup().unreflect(method);
            var descriptor = new FunctionDescriptor(
                    "throwingError",
                    List.of(BigDecimal.class),
                    List.of(ResolvedTypes.fromJavaType(BigDecimal.class)),
                    ResolvedTypes.fromJavaType(BigDecimal.class),
                    handle
            );

            assertThatThrownBy(() -> descriptor.invoke(new Object[]{BigDecimal.ONE}))
                    .isInstanceOf(TestError.class)
                    .isNotInstanceOf(FunctionInvocationException.class);
        }
    }

    @Nested
    @DisplayName("Precondition validation – not wrapped")
    class PreconditionValidation {

        // Precondition violations (null args, arity mismatch) occur before invocation
        // and must remain as IllegalArgumentException, not FunctionInvocationException.

        @Test
        @DisplayName("Null arguments array throws NullPointerException")
        void nullArgumentsThrowsNullPointer() throws Exception {
            var descriptor = descriptorFor("succeedingFunction");
            assertThatThrownBy(() -> descriptor.invoke(null))
                    .isInstanceOf(NullPointerException.class)
                    .isNotInstanceOf(FunctionInvocationException.class);
        }

        @Test
        @DisplayName("Wrong argument count throws IllegalArgumentException")
        void arityMismatchThrowsIllegalArgument() throws Exception {
            var descriptor = descriptorFor("succeedingFunction");
            assertThatThrownBy(() -> descriptor.invoke(new Object[0]))
                    .isInstanceOf(IllegalArgumentException.class)
                    .isNotInstanceOf(FunctionInvocationException.class);
        }
    }
}
