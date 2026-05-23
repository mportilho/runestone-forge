package com.runestone.expeval.internal.runtime;

import com.runestone.expeval.catalog.FunctionDescriptor;

import java.util.List;

final class FunctionCallPlanner {

    private FunctionCallPlanner() {
    }

    static ExecutableNode build(
            ResolvedFunctionBinding binding,
            List<ExecutableNode> arguments,
            RuntimeServices runtimeServices) {
        FunctionDescriptor descriptor = binding.descriptor();
        if (!descriptor.isFoldable() || arguments.stream().anyMatch(argument -> !ConstantNodeValues.isConstant(argument))) {
            return ExecutableFunctionCall.of(binding, arguments);
        }

        int arity = descriptor.arity();
        Object[] args = new Object[arity];
        for (int index = 0; index < arity; index++) {
            args[index] = runtimeServices.coerce(
                    ConstantNodeValues.value(arguments.get(index)),
                    descriptor.parameterTypes().get(index));
        }
        Object result = descriptor.invoke(args);
        return ExecutableFunctionCall.folded(binding, arguments, args, result);
    }
}
