package com.runestone.expeval_mk3.internal.runtime;

import com.runestone.expeval_mk3.api.SourceSpan;
import com.runestone.expeval_mk3.internal.ast.NodeId;
import com.runestone.expeval_mk3.internal.ast.PostfixOperator;
import com.runestone.expeval_mk3.internal.semantics.DeferredCheck;

import java.util.List;
import java.util.Objects;

public final class PostfixExecutableNode implements ExecutableNode {

    private final NodeId id;
    private final SourceSpan sourceSpan;
    private final ExecutableNode operand;
    private final List<PostfixOperator> operators;
    private final int maxFactorialInput;
    private final List<DeferredCheck> deferredChecks;

    public PostfixExecutableNode(
            NodeId id,
            SourceSpan sourceSpan,
            ExecutableNode operand,
            List<PostfixOperator> operators,
            int maxFactorialInput,
            List<DeferredCheck> deferredChecks) {
        this.id = Objects.requireNonNull(id, "id");
        this.sourceSpan = Objects.requireNonNull(sourceSpan, "sourceSpan");
        this.operand = Objects.requireNonNull(operand, "operand");
        this.operators = List.copyOf(Objects.requireNonNull(operators, "operators"));
        this.maxFactorialInput = maxFactorialInput;
        this.deferredChecks = List.copyOf(Objects.requireNonNull(deferredChecks, "deferredChecks"));
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
        return ExpressionRuntime.executePostfix(
                ExpressionRuntime.number(operand.execute(scope)), operators, maxFactorialInput);
    }
}
