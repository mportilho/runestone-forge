package com.runestone.expeval.internal.runtime;

import com.runestone.expeval.internal.execution.plan.*;

import com.runestone.expeval.api.AuditEvent;
import com.runestone.expeval.catalog.FunctionDescriptor;
import com.runestone.expeval.internal.audit.AuditCollector;

import java.util.List;
import java.util.Objects;

final class FunctionCallEvaluator {

    private final RuntimeServices runtimeServices;
    private final NodeEvaluator nodeEvaluator;

    FunctionCallEvaluator(RuntimeServices runtimeServices, NodeEvaluator nodeEvaluator) {
        this.runtimeServices = Objects.requireNonNull(runtimeServices, "runtimeServices");
        this.nodeEvaluator = Objects.requireNonNull(nodeEvaluator, "nodeEvaluator");
    }

    Object evaluate(ExecutableFunctionCall node, ExecutionScope scope) {
        if (node.isFolded()) {
            Object result = runtimeServices.coerceToResolvedType(node.foldedResult(), node.binding().returnType());
            AuditCollector audit = scope.audit();
            if (audit != null) {
                audit.record(new AuditEvent.FunctionCall(
                        node.binding().descriptor().name(),
                        node.foldedArgs(),
                        result));
            }
            return result;
        }

        FunctionDescriptor descriptor = node.binding().descriptor();
        int arity = descriptor.arity();
        AuditCollector audit = scope.audit();
        List<ExecutableNode> argumentNodes = node.arguments();
        List<Class<?>> parameterTypes = descriptor.parameterTypes();

        return switch (arity) {
            case 0 -> runtimeServices.coerceToResolvedType(descriptor.invoke(), node.binding().returnType());
            case 1 -> {
                Object argument1 = evaluateArgument(argumentNodes, parameterTypes, scope, 0);
                Object result = runtimeServices.coerceToResolvedType(
                        descriptor.invoke(argument1), node.binding().returnType());
                if (audit != null) {
                    auditFunctionCall(audit, descriptor, result, argument1);
                }
                yield result;
            }
            case 2 -> {
                Object argument1 = evaluateArgument(argumentNodes, parameterTypes, scope, 0);
                Object argument2 = evaluateArgument(argumentNodes, parameterTypes, scope, 1);
                Object result = runtimeServices.coerceToResolvedType(
                        descriptor.invoke(argument1, argument2), node.binding().returnType());
                if (audit != null) {
                    auditFunctionCall(audit, descriptor, result, argument1, argument2);
                }
                yield result;
            }
            case 3 -> {
                Object argument1 = evaluateArgument(argumentNodes, parameterTypes, scope, 0);
                Object argument2 = evaluateArgument(argumentNodes, parameterTypes, scope, 1);
                Object argument3 = evaluateArgument(argumentNodes, parameterTypes, scope, 2);
                Object result = runtimeServices.coerceToResolvedType(
                        descriptor.invoke(argument1, argument2, argument3), node.binding().returnType());
                if (audit != null) {
                    auditFunctionCall(audit, descriptor, result, argument1, argument2, argument3);
                }
                yield result;
            }
            case 4 -> {
                Object argument1 = evaluateArgument(argumentNodes, parameterTypes, scope, 0);
                Object argument2 = evaluateArgument(argumentNodes, parameterTypes, scope, 1);
                Object argument3 = evaluateArgument(argumentNodes, parameterTypes, scope, 2);
                Object argument4 = evaluateArgument(argumentNodes, parameterTypes, scope, 3);
                Object result = runtimeServices.coerceToResolvedType(
                        descriptor.invoke(argument1, argument2, argument3, argument4), node.binding().returnType());
                if (audit != null) {
                    auditFunctionCall(audit, descriptor, result, argument1, argument2, argument3, argument4);
                }
                yield result;
            }
            case 5 -> {
                Object argument1 = evaluateArgument(argumentNodes, parameterTypes, scope, 0);
                Object argument2 = evaluateArgument(argumentNodes, parameterTypes, scope, 1);
                Object argument3 = evaluateArgument(argumentNodes, parameterTypes, scope, 2);
                Object argument4 = evaluateArgument(argumentNodes, parameterTypes, scope, 3);
                Object argument5 = evaluateArgument(argumentNodes, parameterTypes, scope, 4);
                Object result = runtimeServices.coerceToResolvedType(
                        descriptor.invoke(argument1, argument2, argument3, argument4, argument5),
                        node.binding().returnType());
                if (audit != null) {
                    auditFunctionCall(audit, descriptor, result, argument1, argument2, argument3, argument4, argument5);
                }
                yield result;
            }
            case 6 -> {
                Object argument1 = evaluateArgument(argumentNodes, parameterTypes, scope, 0);
                Object argument2 = evaluateArgument(argumentNodes, parameterTypes, scope, 1);
                Object argument3 = evaluateArgument(argumentNodes, parameterTypes, scope, 2);
                Object argument4 = evaluateArgument(argumentNodes, parameterTypes, scope, 3);
                Object argument5 = evaluateArgument(argumentNodes, parameterTypes, scope, 4);
                Object argument6 = evaluateArgument(argumentNodes, parameterTypes, scope, 5);
                Object result = runtimeServices.coerceToResolvedType(
                        descriptor.invoke(argument1, argument2, argument3, argument4, argument5, argument6),
                        node.binding().returnType());
                if (audit != null) {
                    auditFunctionCall(audit, descriptor, result,
                            argument1, argument2, argument3, argument4, argument5, argument6);
                }
                yield result;
            }
            default -> {
                Object[] arguments = new Object[arity];
                for (int index = 0; index < arity; index++) {
                    arguments[index] = evaluateArgument(argumentNodes, parameterTypes, scope, index);
                }
                Object result = runtimeServices.coerceToResolvedType(
                        descriptor.invoke(arguments), node.binding().returnType());
                if (audit != null) {
                    auditFunctionCall(audit, descriptor, result, arguments);
                }
                yield result;
            }
        };
    }

    private Object evaluateArgument(
            List<ExecutableNode> argumentNodes,
            List<Class<?>> parameterTypes,
            ExecutionScope scope,
            int index) {
        Object evaluated = nodeEvaluator.evaluate(argumentNodes.get(index), scope);
        return runtimeServices.coerce(evaluated, parameterTypes.get(index));
    }

    private static void auditFunctionCall(
            AuditCollector audit,
            FunctionDescriptor descriptor,
            Object result,
            Object... arguments) {
        audit.record(new AuditEvent.FunctionCall(descriptor.name(), arguments, result));
    }
}
