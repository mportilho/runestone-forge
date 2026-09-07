package com.runestone.expeval_mk3.internal.runtime;

import com.runestone.expeval_mk3.api.ExpressionType;
import com.runestone.expeval_mk3.api.SourceSpan;
import com.runestone.expeval_mk3.internal.ast.BinaryOperator;
import com.runestone.expeval_mk3.internal.ast.NodeId;
import com.runestone.expeval_mk3.internal.diagnostics.RuntimeFailures;
import com.runestone.expeval_mk3.internal.regex.LinearRegex;
import com.runestone.expeval_mk3.internal.semantics.DeferredCheck;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.List;
import java.util.Objects;

/**
 * Every binary construct: arithmetic, logical, ordering comparison, equality, and regex match. Each
 * shape switches on its own {@link BinaryOperator} subset and carries only the extra resolved data
 * that subset needs (a {@link MathContext} for arithmetic, an operand {@link ExpressionType} for
 * comparison/equality, or a prepared {@link LinearRegex} for regex).
 */
public final class BinaryExecutableNode implements ExecutableNode {

    private final NodeId id;
    private final SourceSpan sourceSpan;
    private final SourceSpan operatorSpan;
    private final BinaryOperator operator;
    private final ExecutableNode left;
    private final ExecutableNode right;
    private final MathContext mathContext;
    private final ExpressionType operandType;
    private final boolean negated;
    private final LinearRegex regexPattern;
    private final List<DeferredCheck> deferredChecks;
    private final boolean domainProven;

    private BinaryExecutableNode(
            NodeId id,
            SourceSpan sourceSpan,
            SourceSpan operatorSpan,
            BinaryOperator operator,
            ExecutableNode left,
            ExecutableNode right,
            MathContext mathContext,
            ExpressionType operandType,
            boolean negated,
            LinearRegex regexPattern,
            List<DeferredCheck> deferredChecks,
            boolean domainProven) {
        this.id = Objects.requireNonNull(id, "id");
        this.sourceSpan = Objects.requireNonNull(sourceSpan, "sourceSpan");
        this.operatorSpan = Objects.requireNonNull(operatorSpan, "operatorSpan");
        this.operator = Objects.requireNonNull(operator, "operator");
        this.left = Objects.requireNonNull(left, "left");
        this.right = right;
        this.mathContext = mathContext;
        this.operandType = operandType;
        this.negated = negated;
        this.regexPattern = regexPattern;
        this.deferredChecks = List.copyOf(Objects.requireNonNull(deferredChecks, "deferredChecks"));
        this.domainProven = domainProven;
    }

    /**
     * {@code domainProven} is {@code true} only for {@code EXPONENTIATE} where the plan builder has
     * already consumed the resolver's own proof (issue #124): the base's {@code NumericFact} carries a
     * known strictly positive sign and no {@code PowerRealDomainDeferredCheck} was emitted for this node.
     * Any other case — negative or unknown-sign base, or a check the resolver did emit — leaves
     * {@code domainProven} false and takes the classifying path in {@link RealDomainArithmetic}.
     */
    public static BinaryExecutableNode arithmetic(
            NodeId id, SourceSpan sourceSpan, SourceSpan operatorSpan, BinaryOperator operator,
            ExecutableNode left, ExecutableNode right, MathContext mathContext,
            List<DeferredCheck> deferredChecks, boolean domainProven) {
        return new BinaryExecutableNode(
                id, sourceSpan, operatorSpan, operator, left,
                Objects.requireNonNull(right, "right"),
                Objects.requireNonNull(mathContext, "mathContext"),
                null, false, null, deferredChecks, domainProven);
    }

    public static BinaryExecutableNode logical(
            NodeId id, SourceSpan sourceSpan, SourceSpan operatorSpan, BinaryOperator operator,
            ExecutableNode left, ExecutableNode right) {
        return new BinaryExecutableNode(
                id, sourceSpan, operatorSpan, operator, left, Objects.requireNonNull(right, "right"),
                null, null, false, null, List.of(), false);
    }

    public static BinaryExecutableNode comparison(
            NodeId id, SourceSpan sourceSpan, SourceSpan operatorSpan, BinaryOperator operator,
            ExecutableNode left, ExecutableNode right, ExpressionType operandType) {
        return new BinaryExecutableNode(
                id, sourceSpan, operatorSpan, operator, left,
                Objects.requireNonNull(right, "right"),
                null, Objects.requireNonNull(operandType, "operandType"), false, null, List.of(), false);
    }

    public static BinaryExecutableNode equality(
            NodeId id, SourceSpan sourceSpan, SourceSpan operatorSpan, BinaryOperator operator,
            ExecutableNode left, ExecutableNode right, ExpressionType operandType) {
        return new BinaryExecutableNode(
                id, sourceSpan, operatorSpan, operator, left,
                Objects.requireNonNull(right, "right"),
                null, Objects.requireNonNull(operandType, "operandType"),
                operator == BinaryOperator.NOT_EQUAL, null, List.of(), false);
    }

    public static BinaryExecutableNode regex(
            NodeId id, SourceSpan sourceSpan, SourceSpan operatorSpan, BinaryOperator operator,
            ExecutableNode left, LinearRegex regexPattern) {
        return new BinaryExecutableNode(
                id, sourceSpan, operatorSpan, operator, left, null, null, null,
                operator == BinaryOperator.REGEX_NOT_MATCH,
                Objects.requireNonNull(regexPattern, "regexPattern"), List.of(), false);
    }

    @Override
    public NodeId id() {
        return id;
    }

    @Override
    public SourceSpan sourceSpan() {
        return sourceSpan;
    }

    @Override
    public List<DeferredCheck> deferredChecks() {
        return deferredChecks;
    }

    @Override
    public Object execute(ExecutionScope scope) {
        return switch (operator) {
            case ADD -> ExpressionRuntime.number(left.execute(scope)).add(ExpressionRuntime.number(right.execute(scope)));
            case SUBTRACT -> ExpressionRuntime.number(left.execute(scope)).subtract(ExpressionRuntime.number(right.execute(scope)));
            case MULTIPLY -> ExpressionRuntime.number(left.execute(scope)).multiply(ExpressionRuntime.number(right.execute(scope)), mathContext);
            case DIVIDE -> divide(scope);
            case MODULO -> modulo(scope);
            case ROOT -> root(scope);
            case EXPONENTIATE -> exponentiate(scope);
            case CONCATENATE -> (String) left.execute(scope) + right.execute(scope);
            case LOGICAL_AND -> ExpressionRuntime.bool(left.execute(scope)) && ExpressionRuntime.bool(right.execute(scope));
            case LOGICAL_OR -> ExpressionRuntime.bool(left.execute(scope)) || ExpressionRuntime.bool(right.execute(scope));
            case LOGICAL_NAND -> !eagerAnd(scope);
            case LOGICAL_NOR -> !eagerOr(scope);
            case LOGICAL_XOR -> ExpressionRuntime.bool(left.execute(scope)) ^ ExpressionRuntime.bool(right.execute(scope));
            case LOGICAL_XNOR -> !(ExpressionRuntime.bool(left.execute(scope)) ^ ExpressionRuntime.bool(right.execute(scope)));
            case GREATER_THAN -> compare(scope) > 0;
            case GREATER_THAN_OR_EQUAL -> compare(scope) >= 0;
            case LESS_THAN -> compare(scope) < 0;
            case LESS_THAN_OR_EQUAL -> compare(scope) <= 0;
            case EQUAL, NOT_EQUAL -> ExpressionRuntime.structuralEquals(
                    left.execute(scope), right.execute(scope), operandType) != negated;
            case REGEX_MATCH, REGEX_NOT_MATCH -> regexPattern.matches((String) left.execute(scope)) != negated;
        };
    }

    private BigDecimal divide(ExecutionScope scope) {
        BigDecimal dividend = ExpressionRuntime.number(left.execute(scope));
        BigDecimal divisor = ExpressionRuntime.number(right.execute(scope));
        if (divisor.signum() == 0) {
            throw RuntimeFailures.undefinedOperation("division by zero", operatorSpan);
        }
        try {
            return dividend.divide(divisor, mathContext);
        } catch (ArithmeticException exception) {
            throw RuntimeFailures.calculationFailure("division failed", operatorSpan, exception);
        }
    }

    private BigDecimal modulo(ExecutionScope scope) {
        BigDecimal dividend = ExpressionRuntime.number(left.execute(scope));
        BigDecimal divisor = ExpressionRuntime.number(right.execute(scope));
        if (divisor.signum() == 0) {
            throw RuntimeFailures.undefinedOperation("modulo by zero", operatorSpan);
        }
        try {
            return dividend.remainder(divisor);
        } catch (ArithmeticException exception) {
            throw RuntimeFailures.calculationFailure("modulo failed", operatorSpan, exception);
        }
    }

    private BigDecimal root(ExecutionScope scope) {
        BigDecimal degree = ExpressionRuntime.number(left.execute(scope));
        BigDecimal radicand = ExpressionRuntime.number(right.execute(scope));
        return RealDomainArithmetic.root(degree, radicand, mathContext, operatorSpan);
    }

    private BigDecimal exponentiate(ExecutionScope scope) {
        BigDecimal base = ExpressionRuntime.number(left.execute(scope));
        BigDecimal exponent = ExpressionRuntime.number(right.execute(scope));
        return domainProven
                ? RealDomainArithmetic.powWithProvenDomain(base, exponent, mathContext, operatorSpan)
                : RealDomainArithmetic.pow(base, exponent, mathContext, operatorSpan);
    }

    private int compare(ExecutionScope scope) {
        return ExpressionRuntime.compareValues(left.execute(scope), right.execute(scope), operandType);
    }

    // nand/nor evaluate both operands eagerly, unlike Java's short-circuiting &&/||
    private boolean eagerAnd(ExecutionScope scope) {
        boolean leftValue = ExpressionRuntime.bool(left.execute(scope));
        boolean rightValue = ExpressionRuntime.bool(right.execute(scope));
        return leftValue && rightValue;
    }

    private boolean eagerOr(ExecutionScope scope) {
        boolean leftValue = ExpressionRuntime.bool(left.execute(scope));
        boolean rightValue = ExpressionRuntime.bool(right.execute(scope));
        return leftValue || rightValue;
    }
}
