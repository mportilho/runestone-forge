package com.runestone.expeval2.api;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Discriminated union of events recorded during a single expression evaluation.
 *
 * <p>Events are emitted in evaluation order and collected into an {@link ExpressionAuditTrace}
 * when calling {@code computeWithAudit()} on {@link MathExpression} or {@link LogicalExpression}.
 */
public sealed interface AuditEvent permits
        AuditEvent.VariableRead,
        AuditEvent.FunctionCall,
        AuditEvent.AssignmentEvent {

    /**
     * Recorded each time an identifier is resolved during evaluation.
     *
     * <p>This covers both user-supplied variables ({@code systemProvided = false}) and
     * built-in temporal identifiers ({@code systemProvided = true}):
     * {@code currDate}, {@code currTime}, and {@code currDateTime}.
     *
     * @param name           symbol name as written in the expression
     * @param systemProvided {@code true} for built-in temporal identifiers, {@code false} for user variables
     * @param value          the resolved value at evaluation time
     */
    record VariableRead(String name, boolean systemProvided, Object value) implements AuditEvent {
        public VariableRead {
            Objects.requireNonNull(name, "name must not be null");
        }
    }

    /**
     * Recorded after each function invocation, capturing the coerced input arguments and result.
     *
     * <p>{@code callDepth} reflects the nesting level at the moment of the call: 0 means a
     * top-level call, 1 means called from within another function, and so on. This allows
     * reconstructing call hierarchies without a tree structure.
     *
     * <p>Internally stores args as {@code Object[]} to avoid the {@code List.of(args)} allocation
     * on the evaluation hot path. The immutable-list contract is fulfilled lazily in
     * {@link #inputArgs()}, which is called only when the audit trace is read.
     */
    final class FunctionCall implements AuditEvent {

        private final String functionName;
        private final Object[] inputArgs;
        private final Object result;

        /**
         * @param functionName name of the invoked function
         * @param inputArgs    owned array of coerced argument values; caller must not mutate after passing
         * @param result       return value of the function
         */
        public FunctionCall(String functionName, Object[] inputArgs, Object result) {
            this.functionName = Objects.requireNonNull(functionName, "functionName must not be null");
            this.inputArgs = Objects.requireNonNull(inputArgs, "inputArgs must not be null");
            this.result = result;
        }

        public String functionName() {
            return functionName;
        }

        /**
         * Returns an immutable snapshot of the coerced argument values.
         *
         * <p>Allocation is deferred to read time (off the evaluation hot path).
         */
        public List<Object> inputArgs() {
            return List.copyOf(Arrays.asList(inputArgs));
        }

        public Object result() {
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof FunctionCall other)) return false;
            return functionName.equals(other.functionName)
                   && Arrays.equals(inputArgs, other.inputArgs)
                   && Objects.equals(result, other.result);
        }

        @Override
        public int hashCode() {
            return Objects.hash(functionName, Arrays.hashCode(inputArgs), result);
        }

        @Override
        public String toString() {
            return "FunctionCall[functionName=%s, inputArgs=%s, result=%s]"
                    .formatted(functionName, Arrays.toString(inputArgs), result);
        }
    }

    /**
     * Recorded after each variable assignment ({@code :=}) is executed.
     *
     * <p>For destructuring assignments, one event is emitted per target variable.
     *
     * @param targetName name of the variable receiving the value
     * @param newValue   the assigned value
     */
    record AssignmentEvent(String targetName, Object newValue) implements AuditEvent {
        public AssignmentEvent {
            Objects.requireNonNull(targetName, "targetName must not be null");
        }
    }
}
