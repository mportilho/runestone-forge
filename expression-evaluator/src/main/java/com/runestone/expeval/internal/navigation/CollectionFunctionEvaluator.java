package com.runestone.expeval.internal.navigation;

import com.runestone.expeval.internal.execution.eval.*;
import com.runestone.expeval.internal.execution.plan.*;

import com.runestone.expeval.api.ExpressionEvaluationException;
import com.runestone.expeval.catalog.FunctionDescriptor;
import com.runestone.expeval.internal.runtime.RuntimeServices;
import com.runestone.expeval.internal.semantic.ResolvedFunctionBinding;

import java.util.List;

final class CollectionFunctionEvaluator {

    private CollectionFunctionEvaluator() {
    }

    /**
     * {@code ..funcName(args)} -- invokes a catalog function with the current
     * collection/map as the implicit first argument.
     */
    static Object evaluate(
            Object current,
            ExecutablePropertyChain.ExecutableCollectionFunction collectionFunction,
            ExecutionScope scope,
            String source,
            RuntimeServices runtimeServices,
            NodeEvaluator nodeEvaluator) {
        ResolvedFunctionBinding binding = collectionFunction.binding();
        if (binding == null || binding.descriptor() == null) {
            throw new ExpressionEvaluationException(source, "UNRESOLVED_COLLECTION_FUNCTION",
                    "collection function could not be resolved", null);
        }
        FunctionDescriptor descriptor = binding.descriptor();
        List<ExecutableNode> extraArgumentNodes = collectionFunction.arguments();
        int totalArity = 1 + extraArgumentNodes.size();
        List<Class<?>> parameterTypes = descriptor.parameterTypes();

        Object[] arguments = new Object[totalArity];
        arguments[0] = runtimeServices.coerce(current, parameterTypes.getFirst());
        for (int index = 0; index < extraArgumentNodes.size(); index++) {
            Object evaluated = nodeEvaluator.evaluate(extraArgumentNodes.get(index), scope);
            arguments[index + 1] = runtimeServices.coerce(evaluated, parameterTypes.get(index + 1));
        }
        Object result = descriptor.invoke(arguments);
        return runtimeServices.coerceToResolvedType(result, binding.returnType());
    }
}
