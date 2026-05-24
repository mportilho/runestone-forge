package com.runestone.expeval.internal.navigation;

import com.runestone.expeval.internal.execution.eval.*;
import com.runestone.expeval.internal.execution.plan.*;

import com.runestone.expeval.api.ExpressionEvaluationException;
import com.runestone.expeval.internal.runtime.RuntimeServices;

import java.lang.invoke.MethodHandle;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

final class PropertyAccessEvaluator {

    private final String source;
    private final RuntimeServices runtimeServices;
    private final NodeEvaluator nodeEvaluator;

    PropertyAccessEvaluator(String source, RuntimeServices runtimeServices, NodeEvaluator nodeEvaluator) {
        this.source = Objects.requireNonNull(source, "source");
        this.runtimeServices = Objects.requireNonNull(runtimeServices, "runtimeServices");
        this.nodeEvaluator = Objects.requireNonNull(nodeEvaluator, "nodeEvaluator");
    }

    Object evaluateFieldGet(
            ExecutablePropertyChain node,
            Object current,
            ExecutablePropertyChain.ExecutableFieldGet fieldGet) {
        try {
            Object result = fieldGet.getter().invoke(current);
            return runtimeServices.coerceToResolvedType(result, fieldGet.resolvedType());
        } catch (Error error) {
            throw error;
        } catch (Throwable throwable) {
            ExpressionEvaluationException exception = new ExpressionEvaluationException(
                    source,
                    "PROPERTY_ACCESS_ERROR",
                    "error accessing '" + fieldGet.name() + "' while navigating '" + rootName(node.root())
                            + "': " + throwable.getMessage(),
                    null);
            exception.initCause(throwable);
            throw exception;
        }
    }

    Object evaluateMethod(
            ExecutablePropertyChain node,
            ExecutionScope scope,
            Object current,
            ExecutablePropertyChain.ExecutableMethodInvoke methodInvoke) {
        int arity = methodInvoke.arguments().size();
        List<ExecutableNode> arguments = methodInvoke.arguments();
        List<Class<?>> parameterTypes = methodInvoke.parameterTypes();
        try {
            Object result = switch (arity) {
                case 0 -> methodInvoke.handle().invoke(current);
                case 1 -> {
                    Object argument1 = evaluateArgument(arguments, parameterTypes, scope, 0);
                    yield methodInvoke.handle().invoke(current, argument1);
                }
                case 2 -> {
                    Object argument1 = evaluateArgument(arguments, parameterTypes, scope, 0);
                    Object argument2 = evaluateArgument(arguments, parameterTypes, scope, 1);
                    yield methodInvoke.handle().invoke(current, argument1, argument2);
                }
                case 3 -> {
                    Object argument1 = evaluateArgument(arguments, parameterTypes, scope, 0);
                    Object argument2 = evaluateArgument(arguments, parameterTypes, scope, 1);
                    Object argument3 = evaluateArgument(arguments, parameterTypes, scope, 2);
                    yield methodInvoke.handle().invoke(current, argument1, argument2, argument3);
                }
                case 4 -> {
                    Object argument1 = evaluateArgument(arguments, parameterTypes, scope, 0);
                    Object argument2 = evaluateArgument(arguments, parameterTypes, scope, 1);
                    Object argument3 = evaluateArgument(arguments, parameterTypes, scope, 2);
                    Object argument4 = evaluateArgument(arguments, parameterTypes, scope, 3);
                    yield methodInvoke.handle().invoke(current, argument1, argument2, argument3, argument4);
                }
                case 5 -> {
                    Object argument1 = evaluateArgument(arguments, parameterTypes, scope, 0);
                    Object argument2 = evaluateArgument(arguments, parameterTypes, scope, 1);
                    Object argument3 = evaluateArgument(arguments, parameterTypes, scope, 2);
                    Object argument4 = evaluateArgument(arguments, parameterTypes, scope, 3);
                    Object argument5 = evaluateArgument(arguments, parameterTypes, scope, 4);
                    yield methodInvoke.handle().invoke(
                            current, argument1, argument2, argument3, argument4, argument5);
                }
                case 6 -> {
                    Object argument1 = evaluateArgument(arguments, parameterTypes, scope, 0);
                    Object argument2 = evaluateArgument(arguments, parameterTypes, scope, 1);
                    Object argument3 = evaluateArgument(arguments, parameterTypes, scope, 2);
                    Object argument4 = evaluateArgument(arguments, parameterTypes, scope, 3);
                    Object argument5 = evaluateArgument(arguments, parameterTypes, scope, 4);
                    Object argument6 = evaluateArgument(arguments, parameterTypes, scope, 5);
                    yield methodInvoke.handle().invoke(
                            current, argument1, argument2, argument3, argument4, argument5, argument6);
                }
                default -> {
                    Object[] args = new Object[arity + 1];
                    args[0] = current;
                    for (int index = 0; index < arity; index++) {
                        args[index + 1] = evaluateArgument(arguments, parameterTypes, scope, index);
                    }
                    yield methodInvoke.handle().invokeWithArguments(args);
                }
            };
            return runtimeServices.coerceToResolvedType(result, methodInvoke.returnType());
        } catch (Error error) {
            throw error;
        } catch (Throwable throwable) {
            ExpressionEvaluationException exception = new ExpressionEvaluationException(
                    source,
                    "METHOD_INVOKE_ERROR",
                    "error invoking '" + methodInvoke.name() + "' while navigating '" + rootName(node.root())
                            + "': " + throwable.getMessage(),
                    null);
            exception.initCause(throwable);
            throw exception;
        }
    }

    Object resolveReflectiveProperty(Object target, String name) {
        if (target instanceof Map<?, ?> map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> typed = (Map<String, Object>) map;
            return typed.get(name);
        }
        Class<?> targetClass = target.getClass();
        MethodHandle handle = ReflectiveAccessCache.property(targetClass, name);
        if (handle == null) {
            throw new ExpressionEvaluationException(source, "UNKNOWN_PROPERTY",
                    "property '" + name + "' not found on " + targetClass.getSimpleName(), null);
        }
        try {
            return handle.invoke(target);
        } catch (Error error) {
            throw error;
        } catch (Throwable throwable) {
            ExpressionEvaluationException exception = new ExpressionEvaluationException(
                    source, "PROPERTY_ACCESS_ERROR",
                    "error accessing '" + name + "': " + throwable.getMessage(), null);
            exception.initCause(throwable);
            throw exception;
        }
    }

    Object invokeReflectiveMethod(
            ExecutionScope scope,
            Object target,
            ExecutablePropertyChain.ReflectiveMethodInvoke reflectiveMethodInvoke) {
        Object[] arguments = new Object[reflectiveMethodInvoke.arguments().size()];
        for (int index = 0; index < reflectiveMethodInvoke.arguments().size(); index++) {
            arguments[index] = nodeEvaluator.evaluate(reflectiveMethodInvoke.arguments().get(index), scope);
        }
        return invokeMethodReflective(target, reflectiveMethodInvoke.name(), arguments);
    }

    List<Object> projectReflectivePropertyOverList(List<?> list, String propertyName) {
        List<Object> result = new ArrayList<>(list.size());
        for (Object element : list) {
            if (element != null) {
                result.add(resolveReflectiveProperty(element, propertyName));
            }
        }
        return result;
    }

    List<Object> projectFieldGetOverList(
            List<?> list,
            ExecutablePropertyChain node,
            ExecutablePropertyChain.ExecutableFieldGet fieldGet) {
        List<Object> result = new ArrayList<>(list.size());
        for (Object element : list) {
            if (element != null) {
                result.add(evaluateFieldGet(node, element, fieldGet));
            }
        }
        return result;
    }

    private Object evaluateArgument(
            List<ExecutableNode> arguments,
            List<Class<?>> parameterTypes,
            ExecutionScope scope,
            int index) {
        Object evaluated = nodeEvaluator.evaluate(arguments.get(index), scope);
        return runtimeServices.coerce(evaluated, parameterTypes.get(index));
    }

    private Object invokeMethodReflective(Object target, String name, Object[] arguments) {
        Class<?> targetClass = target.getClass();
        MethodHandle handle = ReflectiveAccessCache.method(targetClass, name, arguments.length);
        if (handle == null) {
            throw new ExpressionEvaluationException(source, "UNKNOWN_METHOD",
                    "method '" + name + "' with " + arguments.length + " argument(s) not found on "
                            + targetClass.getSimpleName(), null);
        }
        try {
            return switch (arguments.length) {
                case 0 -> handle.invoke(target);
                case 1 -> handle.invoke(target, arguments[0]);
                case 2 -> handle.invoke(target, arguments[0], arguments[1]);
                case 3 -> handle.invoke(target, arguments[0], arguments[1], arguments[2]);
                case 4 -> handle.invoke(target, arguments[0], arguments[1], arguments[2], arguments[3]);
                case 5 -> handle.invoke(target, arguments[0], arguments[1], arguments[2], arguments[3], arguments[4]);
                case 6 -> handle.invoke(
                        target, arguments[0], arguments[1], arguments[2], arguments[3], arguments[4], arguments[5]);
                default -> {
                    Object[] fullArguments = new Object[arguments.length + 1];
                    fullArguments[0] = target;
                    System.arraycopy(arguments, 0, fullArguments, 1, arguments.length);
                    yield handle.invokeWithArguments(fullArguments);
                }
            };
        } catch (Error error) {
            throw error;
        } catch (Throwable throwable) {
            ExpressionEvaluationException exception = new ExpressionEvaluationException(
                    source, "METHOD_INVOKE_ERROR",
                    "error invoking '" + name + "': " + throwable.getMessage(), null);
            exception.initCause(throwable);
            throw exception;
        }
    }

    private static String rootName(ExecutableNode root) {
        if (root instanceof ExecutableIdentifier id) {
            return id.ref().name();
        }
        return "[constant]";
    }
}
