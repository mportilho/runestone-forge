package com.runestone.expeval2.internal.runtime;

sealed interface ExecutableNode permits
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
