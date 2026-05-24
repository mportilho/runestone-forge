package com.runestone.expeval.internal.execution.plan;

import com.runestone.expeval.api.ExpressionEvaluationException;
import com.runestone.expeval.api.FunctionInvocationException;
import com.runestone.expeval.internal.navigation.PropertyChainOps;
import com.runestone.expeval.internal.runtime.RuntimeServices;

import java.math.MathContext;
import java.util.List;

final class PropertyChainPrefixFolder {

    private PropertyChainPrefixFolder() {
    }

    static ExecutableNode fold(
            ExecutableNode root,
            List<ExecutablePropertyChain.ExecutableAccess> steps,
            RuntimeServices runtimeServices,
            MathContext mathContext) {
        if (!ConstantNodeValues.isConstant(root) || steps.isEmpty()) {
            return new ExecutablePropertyChain(root, steps);
        }

        int foldedSteps = 0;
        Object foldedValue = ConstantNodeValues.value(root);
        ConstantNodeEvaluator evaluator = new ConstantNodeEvaluator(runtimeServices, mathContext);
        for (int index = 0; index < steps.size(); index++) {
            ExecutablePropertyChain.ExecutableAccess access = steps.get(index);
            // Semantic barriers stay in the runtime suffix so folding never captures per-evaluation state.
            if (!FoldabilityAnalyzer.isFoldableAccess(access)) {
                break;
            }
            ExecutablePropertyChain prefix = new ExecutablePropertyChain(root, steps.subList(0, index + 1));
            try {
                foldedValue = PropertyChainOps.evaluatePropertyChain(
                        prefix,
                        null,
                        "<constant-folding>",
                        runtimeServices,
                        mathContext,
                        evaluator);
            } catch (ExpressionEvaluationException | FunctionInvocationException | IllegalStateException exception) {
                // Preserve runtime failure timing: invalid constant navigation still fails during compute().
                break;
            }
            foldedSteps = index + 1;
        }

        if (foldedSteps == 0) {
            return new ExecutablePropertyChain(root, steps);
        }
        if (foldedSteps == steps.size()) {
            return new ExecutableLiteral(foldedValue);
        }
        return new ExecutablePropertyChain(new ExecutableLiteral(foldedValue), steps.subList(foldedSteps, steps.size()));
    }

}
