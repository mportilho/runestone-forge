package com.runestone.expeval2.internal.runtime;

sealed interface ExecutableNode permits
        ExecutableLiteral,
        ExecutableDynamicLiteral,
        ExecutableIdentifier,
        ExecutableBinaryOp,
        ExecutableUnaryOp,
        ExecutablePostfixOp,
        ExecutableFunctionCall,
        ExecutableConditional,
        ExecutableVectorLiteral {
}
