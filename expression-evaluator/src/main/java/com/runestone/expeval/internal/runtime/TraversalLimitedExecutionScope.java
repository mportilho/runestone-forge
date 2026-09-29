package com.runestone.expeval.internal.runtime;

import com.runestone.expeval.api.ExpressionResourceLimits;
import com.runestone.expeval.api.SourceSpan;
import com.runestone.expeval.internal.diagnostics.RuntimeFailures;
import com.runestone.expeval.internal.diagnostics.DiagnosticCode;
import com.runestone.expeval.internal.memory.CalculationRecorder;
import com.runestone.expeval.internal.regex.LinearRegex;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.ZoneId;
import java.util.List;

/** SAFE scope variant used only by executions that can visit collection or map entries. */
public final class TraversalLimitedExecutionScope extends ExecutionScope {
    private final ExpressionResourceLimits limits;
    private final int maximum;
    private int remaining;

    public TraversalLimitedExecutionScope(Object[] frame, ZoneId zoneId, Clock clock,
                                          CalculationRecorder recorder, ExpressionResourceLimits limits) {
        super(frame, zoneId, clock, recorder);
        this.limits = limits;
        maximum = limits.maxTraversalSteps();
        remaining = maximum;
    }

    void visit(SourceSpan span) {
        if (remaining == 0) {
            throw RuntimeFailures.traversalStepLimitExceeded(maximum, span);
        }
        remaining--;
    }

    @Override
    public void validateValue(Object value, SourceSpan span) {
        ValueShapeValidator.Violation violation = ValueShapeValidator.check(value, limits, this, span);
        if (violation != null) {
            if (violation.kind() == ValueShapeValidator.Kind.FORBIDDEN_NULL) {
                throw RuntimeFailures.forbiddenNull("value container must not contain null", span);
            }
            throw RuntimeFailures.domainViolation(
                    violation.kind() == ValueShapeValidator.Kind.MATERIALIZATION
                            ? DiagnosticCode.RUNTIME_MATERIALIZATION_LIMIT_EXCEEDED
                            : DiagnosticCode.RUNTIME_VALUE_SHAPE_EXCEEDED,
                    violation.message(), span);
        }
    }

    @Override
    public boolean enforcesResourceLimits() {
        return true;
    }

    @Override
    public void validateRegexPattern(String pattern, SourceSpan span) {
        if (pattern.length() > limits.maxRegexPatternLength()) {
            throw RuntimeFailures.domainViolation(DiagnosticCode.RUNTIME_VALUE_SHAPE_EXCEEDED,
                    "maxRegexPatternLength " + limits.maxRegexPatternLength() + " exceeded", span);
        }
    }

    @Override
    public void validateRepeat(String text, BigDecimal times, SourceSpan span) {
        ValueShapeValidator.Violation violation = ValueShapeValidator.repeatedText(text, times, limits);
        if (violation != null) {
            throw RuntimeFailures.domainViolation(DiagnosticCode.RUNTIME_VALUE_SHAPE_EXCEEDED,
                    violation.message(), span);
        }
    }

    @Override
    public void validateTextLength(long length, SourceSpan span) {
        if (length > limits.maxTextLength()) {
            throw RuntimeFailures.domainViolation(DiagnosticCode.RUNTIME_VALUE_SHAPE_EXCEEDED,
                    "maxTextLength " + limits.maxTextLength() + " exceeded", span);
        }
    }

    @Override
    public String concatenate(String left, String right, SourceSpan span) {
        validateTextLength((long) left.length() + right.length(), span);
        return left + right;
    }

    @Override
    public String replaceAll(LinearRegex regex, String text, String replacement, SourceSpan span) {
        return regex.replaceAllBounded(text, replacement, limits.maxTextLength(),
                ignored -> validateTextLength((long) limits.maxTextLength() + 1, span));
    }

    @Override
    public List<String> split(LinearRegex regex, String text, SourceSpan span) {
        return regex.splitBounded(text, limits.maxMaterializedSize(), limits.maxTextLength(),
                () -> { throw RuntimeFailures.domainViolation(DiagnosticCode.RUNTIME_MATERIALIZATION_LIMIT_EXCEEDED,
                        "maxMaterializedSize " + limits.maxMaterializedSize() + " exceeded", span); },
                () -> validateTextLength((long) limits.maxTextLength() + 1, span));
    }
}
