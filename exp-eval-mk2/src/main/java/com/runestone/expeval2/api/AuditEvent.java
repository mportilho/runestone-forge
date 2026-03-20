package com.runestone.expeval2.api;

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
     * @param value          the resolved raw value at evaluation time
     */
    record VariableRead(String name, boolean systemProvided, Object value) implements AuditEvent {
        public VariableRead {
            Objects.requireNonNull(name, "name must not be null");
        }
    }

    /**
     * Recorded after each function invocation, capturing the coerced input arguments and raw result.
     *
     * <p>{@code callDepth} reflects the nesting level at the moment of the call: 0 means a
     * top-level call, 1 means called from within another function, and so on. This allows
     * reconstructing call hierarchies without a tree structure.
     *
     * @param functionName name of the invoked function
     * @param inputArgs    immutable snapshot of the coerced argument values
     * @param result       raw return value of the function
     * @param callDepth    nesting depth at call time (0 = top-level call)
     */
    record FunctionCall(String functionName, List<Object> inputArgs, Object result, int callDepth)
            implements AuditEvent {
        public FunctionCall {
            Objects.requireNonNull(functionName, "functionName must not be null");
            Objects.requireNonNull(inputArgs, "inputArgs must not be null");
        }
    }

    /**
     * Recorded after each variable assignment ({@code :=}) is executed.
     *
     * <p>For destructuring assignments, one event is emitted per target variable.
     *
     * @param targetName name of the variable receiving the value
     * @param newValue   the assigned raw value
     */
    record AssignmentEvent(String targetName, Object newValue) implements AuditEvent {
        public AssignmentEvent {
            Objects.requireNonNull(targetName, "targetName must not be null");
        }
    }
}
