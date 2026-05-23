package com.runestone.expeval.internal.execution.plan;

public sealed interface ExecutableNode permits
        ExecutableLiteral,
        ExecutableDynamicLiteral,
        ExecutableIdentifier,
        ExecutableBinaryOp,
        ExecutableTernaryOp,
        ExecutableUnaryOp,
        ExecutablePostfixOp,
        ExecutableFunctionCall,
        ExecutableConditional,
        ExecutableSimpleConditional,
        ExecutableVectorLiteral,
        ExecutablePropertyChain,
        ExecutableNullCoalesce,
        ExecutableRegexOp {
}
