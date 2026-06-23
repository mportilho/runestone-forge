package com.runestone.expeval.internal.runtime;

import java.util.regex.Pattern;

/**
 * Pre-compiled regex match node. The {@link Pattern} is compiled exactly once
 * during execution-plan construction and reused across all evaluations of this
 * expression, making repeated evaluation allocation-free and thread-safe.
 */
record ExecutableRegexOp(
        ExecutableNode subject,
        Pattern pattern,
        boolean negate
) implements ExecutableNode {
}
