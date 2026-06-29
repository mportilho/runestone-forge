package com.runestone.expeval.internal.runtime;

import com.runestone.expeval.api.AuditEvent;
import com.runestone.expeval.catalog.FunctionDescriptor;
import com.runestone.expeval.internal.semantic.ResolvedFunctionBinding;

import java.lang.invoke.MethodHandle;
import java.util.List;

final class RuntimeInvocationSupport {

    private RuntimeInvocationSupport() {
    }

    static Object invokeFunction(
            ResolvedFunctionBinding binding,
            List<ExecutableNode> argumentNodes,
            ExecutionScope scope,
            RuntimeServices runtimeServices,
            NodeEvaluator evaluator,
            AuditCollector audit) {

        FunctionDescriptor descriptor = binding.descriptor();
        List<Class<?>> parameterTypes = descriptor.parameterTypes();
        return switch (descriptor.arity()) {
            case 0 -> {
                Object result = runtimeServices.coerceToResolvedType(descriptor.invoke(), binding.returnType());
                if (audit != null) {
                    auditFunctionCall(audit, descriptor, result);
                }
                yield result;
            }
            case 1 -> {
                Object a1 = evaluateAndCoerce(argumentNodes, 0, scope, runtimeServices, evaluator, parameterTypes);
                Object result = runtimeServices.coerceToResolvedType(descriptor.invoke(a1), binding.returnType());
                if (audit != null) {
                    auditFunctionCall(audit, descriptor, result, a1);
                }
                yield result;
            }
            case 2 -> {
                Object a1 = evaluateAndCoerce(argumentNodes, 0, scope, runtimeServices, evaluator, parameterTypes);
                Object a2 = evaluateAndCoerce(argumentNodes, 1, scope, runtimeServices, evaluator, parameterTypes);
                Object result = runtimeServices.coerceToResolvedType(descriptor.invoke(a1, a2), binding.returnType());
                if (audit != null) {
                    auditFunctionCall(audit, descriptor, result, a1, a2);
                }
                yield result;
            }
            case 3 -> {
                Object a1 = evaluateAndCoerce(argumentNodes, 0, scope, runtimeServices, evaluator, parameterTypes);
                Object a2 = evaluateAndCoerce(argumentNodes, 1, scope, runtimeServices, evaluator, parameterTypes);
                Object a3 = evaluateAndCoerce(argumentNodes, 2, scope, runtimeServices, evaluator, parameterTypes);
                Object result = runtimeServices.coerceToResolvedType(descriptor.invoke(a1, a2, a3), binding.returnType());
                if (audit != null) {
                    auditFunctionCall(audit, descriptor, result, a1, a2, a3);
                }
                yield result;
            }
            case 4 -> {
                Object a1 = evaluateAndCoerce(argumentNodes, 0, scope, runtimeServices, evaluator, parameterTypes);
                Object a2 = evaluateAndCoerce(argumentNodes, 1, scope, runtimeServices, evaluator, parameterTypes);
                Object a3 = evaluateAndCoerce(argumentNodes, 2, scope, runtimeServices, evaluator, parameterTypes);
                Object a4 = evaluateAndCoerce(argumentNodes, 3, scope, runtimeServices, evaluator, parameterTypes);
                Object result = runtimeServices.coerceToResolvedType(descriptor.invoke(a1, a2, a3, a4), binding.returnType());
                if (audit != null) {
                    auditFunctionCall(audit, descriptor, result, a1, a2, a3, a4);
                }
                yield result;
            }
            case 5 -> {
                Object a1 = evaluateAndCoerce(argumentNodes, 0, scope, runtimeServices, evaluator, parameterTypes);
                Object a2 = evaluateAndCoerce(argumentNodes, 1, scope, runtimeServices, evaluator, parameterTypes);
                Object a3 = evaluateAndCoerce(argumentNodes, 2, scope, runtimeServices, evaluator, parameterTypes);
                Object a4 = evaluateAndCoerce(argumentNodes, 3, scope, runtimeServices, evaluator, parameterTypes);
                Object a5 = evaluateAndCoerce(argumentNodes, 4, scope, runtimeServices, evaluator, parameterTypes);
                Object result = runtimeServices.coerceToResolvedType(descriptor.invoke(a1, a2, a3, a4, a5), binding.returnType());
                if (audit != null) {
                    auditFunctionCall(audit, descriptor, result, a1, a2, a3, a4, a5);
                }
                yield result;
            }
            case 6 -> {
                Object a1 = evaluateAndCoerce(argumentNodes, 0, scope, runtimeServices, evaluator, parameterTypes);
                Object a2 = evaluateAndCoerce(argumentNodes, 1, scope, runtimeServices, evaluator, parameterTypes);
                Object a3 = evaluateAndCoerce(argumentNodes, 2, scope, runtimeServices, evaluator, parameterTypes);
                Object a4 = evaluateAndCoerce(argumentNodes, 3, scope, runtimeServices, evaluator, parameterTypes);
                Object a5 = evaluateAndCoerce(argumentNodes, 4, scope, runtimeServices, evaluator, parameterTypes);
                Object a6 = evaluateAndCoerce(argumentNodes, 5, scope, runtimeServices, evaluator, parameterTypes);
                Object result = runtimeServices.coerceToResolvedType(descriptor.invoke(a1, a2, a3, a4, a5, a6), binding.returnType());
                if (audit != null) {
                    auditFunctionCall(audit, descriptor, result, a1, a2, a3, a4, a5, a6);
                }
                yield result;
            }
            default -> {
                Object[] arguments = evaluateArguments(argumentNodes, scope, runtimeServices, evaluator, parameterTypes);
                Object result = runtimeServices.coerceToResolvedType(descriptor.invoke(arguments), binding.returnType());
                if (audit != null) {
                    auditFunctionCall(audit, descriptor, result, arguments);
                }
                yield result;
            }
        };
    }

    static Object invokeCollectionFunction(
            ResolvedFunctionBinding binding,
            Object current,
            List<ExecutableNode> extraArgumentNodes,
            ExecutionScope scope,
            RuntimeServices runtimeServices,
            NodeEvaluator evaluator,
            AuditCollector audit) {

        FunctionDescriptor descriptor = binding.descriptor();
        List<Class<?>> parameterTypes = descriptor.parameterTypes();
        int totalArity = 1 + extraArgumentNodes.size();
        return switch (totalArity) {
            case 1 -> {
                Object a1 = runtimeServices.coerce(current, parameterTypes.getFirst());
                Object result = runtimeServices.coerceToResolvedType(descriptor.invoke(a1), binding.returnType());
                if (audit != null) {
                    auditFunctionCall(audit, descriptor, result, a1);
                }
                yield result;
            }
            case 2 -> {
                Object a1 = runtimeServices.coerce(current, parameterTypes.getFirst());
                Object a2 = evaluateAndCoerce(extraArgumentNodes, 0, scope, runtimeServices, evaluator, parameterTypes, 1);
                Object result = runtimeServices.coerceToResolvedType(descriptor.invoke(a1, a2), binding.returnType());
                if (audit != null) {
                    auditFunctionCall(audit, descriptor, result, a1, a2);
                }
                yield result;
            }
            case 3 -> {
                Object a1 = runtimeServices.coerce(current, parameterTypes.getFirst());
                Object a2 = evaluateAndCoerce(extraArgumentNodes, 0, scope, runtimeServices, evaluator, parameterTypes, 1);
                Object a3 = evaluateAndCoerce(extraArgumentNodes, 1, scope, runtimeServices, evaluator, parameterTypes, 1);
                Object result = runtimeServices.coerceToResolvedType(descriptor.invoke(a1, a2, a3), binding.returnType());
                if (audit != null) {
                    auditFunctionCall(audit, descriptor, result, a1, a2, a3);
                }
                yield result;
            }
            case 4 -> {
                Object a1 = runtimeServices.coerce(current, parameterTypes.getFirst());
                Object a2 = evaluateAndCoerce(extraArgumentNodes, 0, scope, runtimeServices, evaluator, parameterTypes, 1);
                Object a3 = evaluateAndCoerce(extraArgumentNodes, 1, scope, runtimeServices, evaluator, parameterTypes, 1);
                Object a4 = evaluateAndCoerce(extraArgumentNodes, 2, scope, runtimeServices, evaluator, parameterTypes, 1);
                Object result = runtimeServices.coerceToResolvedType(descriptor.invoke(a1, a2, a3, a4), binding.returnType());
                if (audit != null) {
                    auditFunctionCall(audit, descriptor, result, a1, a2, a3, a4);
                }
                yield result;
            }
            case 5 -> {
                Object a1 = runtimeServices.coerce(current, parameterTypes.getFirst());
                Object a2 = evaluateAndCoerce(extraArgumentNodes, 0, scope, runtimeServices, evaluator, parameterTypes, 1);
                Object a3 = evaluateAndCoerce(extraArgumentNodes, 1, scope, runtimeServices, evaluator, parameterTypes, 1);
                Object a4 = evaluateAndCoerce(extraArgumentNodes, 2, scope, runtimeServices, evaluator, parameterTypes, 1);
                Object a5 = evaluateAndCoerce(extraArgumentNodes, 3, scope, runtimeServices, evaluator, parameterTypes, 1);
                Object result = runtimeServices.coerceToResolvedType(descriptor.invoke(a1, a2, a3, a4, a5), binding.returnType());
                if (audit != null) {
                    auditFunctionCall(audit, descriptor, result, a1, a2, a3, a4, a5);
                }
                yield result;
            }
            case 6 -> {
                Object a1 = runtimeServices.coerce(current, parameterTypes.getFirst());
                Object a2 = evaluateAndCoerce(extraArgumentNodes, 0, scope, runtimeServices, evaluator, parameterTypes, 1);
                Object a3 = evaluateAndCoerce(extraArgumentNodes, 1, scope, runtimeServices, evaluator, parameterTypes, 1);
                Object a4 = evaluateAndCoerce(extraArgumentNodes, 2, scope, runtimeServices, evaluator, parameterTypes, 1);
                Object a5 = evaluateAndCoerce(extraArgumentNodes, 3, scope, runtimeServices, evaluator, parameterTypes, 1);
                Object a6 = evaluateAndCoerce(extraArgumentNodes, 4, scope, runtimeServices, evaluator, parameterTypes, 1);
                Object result = runtimeServices.coerceToResolvedType(descriptor.invoke(a1, a2, a3, a4, a5, a6), binding.returnType());
                if (audit != null) {
                    auditFunctionCall(audit, descriptor, result, a1, a2, a3, a4, a5, a6);
                }
                yield result;
            }
            default -> {
                Object[] arguments = new Object[totalArity];
                arguments[0] = runtimeServices.coerce(current, parameterTypes.getFirst());
                for (int index = 0; index < extraArgumentNodes.size(); index++) {
                    arguments[index + 1] = evaluateAndCoerce(
                            extraArgumentNodes,
                            index,
                            scope,
                            runtimeServices,
                            evaluator,
                            parameterTypes,
                            1);
                }
                Object result = runtimeServices.coerceToResolvedType(descriptor.invoke(arguments), binding.returnType());
                if (audit != null) {
                    auditFunctionCall(audit, descriptor, result, arguments);
                }
                yield result;
            }
        };
    }

    static Object invokeMethodHandleWithReceiver(
            MethodHandle handle,
            Object receiver,
            List<ExecutableNode> argumentNodes,
            List<Class<?>> parameterTypes,
            ExecutionScope scope,
            RuntimeServices runtimeServices,
            NodeEvaluator evaluator) throws Throwable {

        return switch (argumentNodes.size()) {
            case 0 -> handle.invoke(receiver);
            case 1 -> {
                Object a1 = evaluateAndCoerce(argumentNodes, 0, scope, runtimeServices, evaluator, parameterTypes);
                yield handle.invoke(receiver, a1);
            }
            case 2 -> {
                Object a1 = evaluateAndCoerce(argumentNodes, 0, scope, runtimeServices, evaluator, parameterTypes);
                Object a2 = evaluateAndCoerce(argumentNodes, 1, scope, runtimeServices, evaluator, parameterTypes);
                yield handle.invoke(receiver, a1, a2);
            }
            case 3 -> {
                Object a1 = evaluateAndCoerce(argumentNodes, 0, scope, runtimeServices, evaluator, parameterTypes);
                Object a2 = evaluateAndCoerce(argumentNodes, 1, scope, runtimeServices, evaluator, parameterTypes);
                Object a3 = evaluateAndCoerce(argumentNodes, 2, scope, runtimeServices, evaluator, parameterTypes);
                yield handle.invoke(receiver, a1, a2, a3);
            }
            case 4 -> {
                Object a1 = evaluateAndCoerce(argumentNodes, 0, scope, runtimeServices, evaluator, parameterTypes);
                Object a2 = evaluateAndCoerce(argumentNodes, 1, scope, runtimeServices, evaluator, parameterTypes);
                Object a3 = evaluateAndCoerce(argumentNodes, 2, scope, runtimeServices, evaluator, parameterTypes);
                Object a4 = evaluateAndCoerce(argumentNodes, 3, scope, runtimeServices, evaluator, parameterTypes);
                yield handle.invoke(receiver, a1, a2, a3, a4);
            }
            case 5 -> {
                Object a1 = evaluateAndCoerce(argumentNodes, 0, scope, runtimeServices, evaluator, parameterTypes);
                Object a2 = evaluateAndCoerce(argumentNodes, 1, scope, runtimeServices, evaluator, parameterTypes);
                Object a3 = evaluateAndCoerce(argumentNodes, 2, scope, runtimeServices, evaluator, parameterTypes);
                Object a4 = evaluateAndCoerce(argumentNodes, 3, scope, runtimeServices, evaluator, parameterTypes);
                Object a5 = evaluateAndCoerce(argumentNodes, 4, scope, runtimeServices, evaluator, parameterTypes);
                yield handle.invoke(receiver, a1, a2, a3, a4, a5);
            }
            case 6 -> {
                Object a1 = evaluateAndCoerce(argumentNodes, 0, scope, runtimeServices, evaluator, parameterTypes);
                Object a2 = evaluateAndCoerce(argumentNodes, 1, scope, runtimeServices, evaluator, parameterTypes);
                Object a3 = evaluateAndCoerce(argumentNodes, 2, scope, runtimeServices, evaluator, parameterTypes);
                Object a4 = evaluateAndCoerce(argumentNodes, 3, scope, runtimeServices, evaluator, parameterTypes);
                Object a5 = evaluateAndCoerce(argumentNodes, 4, scope, runtimeServices, evaluator, parameterTypes);
                Object a6 = evaluateAndCoerce(argumentNodes, 5, scope, runtimeServices, evaluator, parameterTypes);
                yield handle.invoke(receiver, a1, a2, a3, a4, a5, a6);
            }
            default -> {
                Object[] arguments = new Object[argumentNodes.size() + 1];
                arguments[0] = receiver;
                for (int index = 0; index < argumentNodes.size(); index++) {
                    arguments[index + 1] = evaluateAndCoerce(
                            argumentNodes,
                            index,
                            scope,
                            runtimeServices,
                            evaluator,
                            parameterTypes);
                }
                yield handle.invokeWithArguments(arguments);
            }
        };
    }

    static Object invokeMethodHandleWithReceiver(MethodHandle handle, Object receiver, Object[] arguments) throws Throwable {
        return switch (arguments.length) {
            case 0 -> handle.invoke(receiver);
            case 1 -> handle.invoke(receiver, arguments[0]);
            case 2 -> handle.invoke(receiver, arguments[0], arguments[1]);
            case 3 -> handle.invoke(receiver, arguments[0], arguments[1], arguments[2]);
            case 4 -> handle.invoke(receiver, arguments[0], arguments[1], arguments[2], arguments[3]);
            case 5 -> handle.invoke(receiver, arguments[0], arguments[1], arguments[2], arguments[3], arguments[4]);
            case 6 -> handle.invoke(receiver, arguments[0], arguments[1], arguments[2], arguments[3], arguments[4], arguments[5]);
            default -> {
                Object[] fullArguments = new Object[arguments.length + 1];
                fullArguments[0] = receiver;
                System.arraycopy(arguments, 0, fullArguments, 1, arguments.length);
                yield handle.invokeWithArguments(fullArguments);
            }
        };
    }

    private static Object evaluateAndCoerce(
            List<ExecutableNode> argumentNodes,
            int argumentIndex,
            ExecutionScope scope,
            RuntimeServices runtimeServices,
            NodeEvaluator evaluator,
            List<Class<?>> parameterTypes) {

        return evaluateAndCoerce(argumentNodes, argumentIndex, scope, runtimeServices, evaluator, parameterTypes, 0);
    }

    private static Object evaluateAndCoerce(
            List<ExecutableNode> argumentNodes,
            int argumentIndex,
            ExecutionScope scope,
            RuntimeServices runtimeServices,
            NodeEvaluator evaluator,
            List<Class<?>> parameterTypes,
            int parameterOffset) {

        Object value = evaluator.evaluate(argumentNodes.get(argumentIndex), scope);
        return runtimeServices.coerce(value, parameterTypes.get(argumentIndex + parameterOffset));
    }

    private static Object[] evaluateArguments(
            List<ExecutableNode> argumentNodes,
            ExecutionScope scope,
            RuntimeServices runtimeServices,
            NodeEvaluator evaluator,
            List<Class<?>> parameterTypes) {

        Object[] arguments = new Object[argumentNodes.size()];
        for (int index = 0; index < arguments.length; index++) {
            arguments[index] = evaluateAndCoerce(argumentNodes, index, scope, runtimeServices, evaluator, parameterTypes);
        }
        return arguments;
    }

    private static void auditFunctionCall(AuditCollector audit, FunctionDescriptor descriptor, Object result, Object... arguments) {
        audit.record(new AuditEvent.FunctionCall(descriptor.name(), arguments, result));
    }
}
