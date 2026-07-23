package com.runestone.expeval_mk3.internal.plan;

import com.runestone.expeval_mk3.internal.runtime.ExecutionScope;

@FunctionalInterface
interface ExecutableNode {

    Object execute(ExecutionScope scope);
}
