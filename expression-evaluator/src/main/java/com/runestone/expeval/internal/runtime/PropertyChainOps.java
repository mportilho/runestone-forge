package com.runestone.expeval.internal.runtime;

import com.runestone.expeval.api.ExpressionEvaluationException;
import com.runestone.expeval.internal.LanguageSymbols;
import com.runestone.expeval.internal.navigation.FilterContext;
import com.runestone.expeval.internal.navigation.TypeIntrospectionSupport;

import java.lang.invoke.MethodHandle;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

final class PropertyChainOps {

    private PropertyChainOps() {}

    static Object evaluatePropertyChain(ExecutablePropertyChain node, ExecutionScope scope,
            String source, RuntimeServices runtimeServices, MathContext mathContext, NodeEvaluator eval) {
        if (node.legacyOnly()) {
            return evaluateLegacyPropertyChain(node, scope, source, runtimeServices, eval);
        }

        Object current = eval.evaluate(node.root(), scope);
        List<ExecutablePropertyChain.ExecutableAccess> chain = node.chain();
        int chainStart = 0;
        if (isMapKeySentinel(node.root(), chain)) {
            FilterContext mapCtx = FilterContextStack.INSTANCE.get().peek();
            String sentinel = ((ExecutablePropertyChain.ReflectivePropertyAccess) chain.getFirst()).name();
            current = "key".equals(sentinel) ? mapCtx.mapKey() : mapCtx.mapValue();
            chainStart = 1;
        }
        boolean inCollection = false;
        for (int i = chainStart; i < chain.size(); i++) {
            ExecutablePropertyChain.ExecutableAccess access = chain.get(i);
            if (current == null) {
                if (isSafeAccess(access)) {
                    return null;
                }
                throw new ExpressionEvaluationException(source, "NULL_IN_CHAIN",
                        "null value encountered navigating '" + rootName(node.root()) + "'", null);
            }
            if (inCollection && current instanceof List<?> list) {
                if (access instanceof ExecutablePropertyChain.ReflectivePropertyAccess pa) {
                    current = projectPropertyOverList(source, list, pa.name());
                    continue;
                }
                if (access instanceof ExecutablePropertyChain.ExecutableFieldGet fieldGet) {
                    current = projectFieldGetOverList(list, node, fieldGet, source, runtimeServices);
                    continue;
                }
            }
            current = switch (access) {
                case ExecutablePropertyChain.ExecutableFieldGet fieldGet ->
                        invokeGetter(node, current, fieldGet, source, runtimeServices);
                case ExecutablePropertyChain.ExecutableMethodInvoke methodInvoke ->
                        invokeMethod(node, scope, current, methodInvoke, source, runtimeServices, eval);
                case ExecutablePropertyChain.ReflectivePropertyAccess propertyAccess ->
                        resolvePropertyReflective(source, current, propertyAccess.name());
                case ExecutablePropertyChain.ReflectiveMethodInvoke reflectiveMethodInvoke -> {
                    Object[] args = new Object[reflectiveMethodInvoke.arguments().size()];
                    for (int index = 0; index < reflectiveMethodInvoke.arguments().size(); index++) {
                        args[index] = eval.evaluate(reflectiveMethodInvoke.arguments().get(index), scope);
                    }
                    yield invokeMethodReflective(source, current, reflectiveMethodInvoke.name(), args);
                }
                case ExecutablePropertyChain.ExecutableIndexAccess ia ->
                        CollectionNavigationOps.applyIndex(current,
                                (int) asBigDecimalStrict(eval.evaluate(ia.index(), scope), runtimeServices).longValue(),
                                source);
                case ExecutablePropertyChain.ExecutableMapKeyAccess mka ->
                        CollectionNavigationOps.applyMapKey(current, mka.key(), source);
                case ExecutablePropertyChain.ExecutableSliceAccess sa -> {
                    Integer start = sa.start() == null ? null
                            : (int) asBigDecimalStrict(eval.evaluate(sa.start(), scope), runtimeServices).longValue();
                    Integer end = sa.end() == null ? null
                            : (int) asBigDecimalStrict(eval.evaluate(sa.end(), scope), runtimeServices).longValue();
                    yield CollectionNavigationOps.applySlice(current, start, end, source);
                }
                case ExecutablePropertyChain.ExecutableWildcard ignored ->
                        CollectionNavigationOps.applyWildcard(current);
                case ExecutablePropertyChain.ExecutableFilterPredicate fp ->
                        CollectionNavigationOps.applyFilter(current, fp.predicate(), scope, source, runtimeServices, eval);
                case ExecutablePropertyChain.ExecutableDeepScan ds ->
                        CollectionNavigationOps.applyDeepScan(current, ds.propertyName(), source);
                case ExecutablePropertyChain.ExecutableVectorAggregation va ->
                        CollectionNavigationOps.applyAggregation(current, va.kind(), va.transform(), scope,
                                source, runtimeServices, mathContext, eval);
                case ExecutablePropertyChain.ExecutableVectorMap vm ->
                        CollectionNavigationOps.applyMapTransform(current, vm.transform(), scope, source, eval);
                case ExecutablePropertyChain.ExecutableMapProjection mp ->
                        CollectionNavigationOps.applyMapProjection(current, mp.kind(), source);
                case ExecutablePropertyChain.ExecutableCollectionFunction cf ->
                        CollectionNavigationOps.applyCollectionFunction(current, cf, scope, source, runtimeServices, eval);
            };
            if (access instanceof ExecutablePropertyChain.ExecutableWildcard
                    || access instanceof ExecutablePropertyChain.ExecutableSliceAccess
                    || access instanceof ExecutablePropertyChain.ExecutableFilterPredicate
                    || access instanceof ExecutablePropertyChain.ExecutableDeepScan
                    || access instanceof ExecutablePropertyChain.ExecutableVectorMap) {
                inCollection = true;
            } else if (access instanceof ExecutablePropertyChain.ExecutableIndexAccess
                    || access instanceof ExecutablePropertyChain.ExecutableVectorAggregation) {
                inCollection = false;
            }
        }
        return current;
    }

    private static Object evaluateLegacyPropertyChain(ExecutablePropertyChain node, ExecutionScope scope,
            String source, RuntimeServices runtimeServices, NodeEvaluator eval) {
        Object current = eval.evaluate(node.root(), scope);
        List<ExecutablePropertyChain.ExecutableAccess> chain = node.chain();
        int chainStart = 0;
        if (isMapKeySentinel(node.root(), chain)) {
            FilterContext mapCtx = FilterContextStack.INSTANCE.get().peek();
            String sentinel = ((ExecutablePropertyChain.ReflectivePropertyAccess) chain.getFirst()).name();
            current = "key".equals(sentinel) ? mapCtx.mapKey() : mapCtx.mapValue();
            chainStart = 1;
        }
        for (int i = chainStart; i < chain.size(); i++) {
            ExecutablePropertyChain.ExecutableAccess access = chain.get(i);
            if (current == null) {
                if (isSafeAccess(access)) {
                    return null;
                }
                throw new ExpressionEvaluationException(source, "NULL_IN_CHAIN",
                        "null value encountered navigating '" + rootName(node.root()) + "'", null);
            }
            current = switch (access) {
                case ExecutablePropertyChain.ExecutableFieldGet fieldGet ->
                        invokeGetter(node, current, fieldGet, source, runtimeServices);
                case ExecutablePropertyChain.ExecutableMethodInvoke methodInvoke ->
                        invokeMethod(node, scope, current, methodInvoke, source, runtimeServices, eval);
                case ExecutablePropertyChain.ReflectivePropertyAccess propertyAccess ->
                        resolvePropertyReflective(source, current, propertyAccess.name());
                case ExecutablePropertyChain.ReflectiveMethodInvoke reflectiveMethodInvoke -> {
                    Object[] args = new Object[reflectiveMethodInvoke.arguments().size()];
                    for (int index = 0; index < reflectiveMethodInvoke.arguments().size(); index++) {
                        args[index] = eval.evaluate(reflectiveMethodInvoke.arguments().get(index), scope);
                    }
                    yield invokeMethodReflective(source, current, reflectiveMethodInvoke.name(), args);
                }
                default -> throw new IllegalStateException("legacy property chain contains unsupported access: " + access);
            };
        }
        return current;
    }

    private static Object invokeGetter(ExecutablePropertyChain node, Object current,
            ExecutablePropertyChain.ExecutableFieldGet fieldGet, String source, RuntimeServices runtimeServices) {
        try {
            Object result = fieldGet.getter().invoke(current);
            return runtimeServices.coerceToResolvedType(result, fieldGet.resolvedType());
        } catch (Error error) {
            throw error;
        } catch (Throwable throwable) {
            ExpressionEvaluationException exception = new ExpressionEvaluationException(
                    source, "PROPERTY_ACCESS_ERROR",
                    "error accessing '" + fieldGet.name() + "' while navigating '" + rootName(node.root())
                    + "': " + throwable.getMessage(), null);
            exception.initCause(throwable);
            throw exception;
        }
    }

    private static Object invokeMethod(ExecutablePropertyChain node, ExecutionScope scope, Object current,
            ExecutablePropertyChain.ExecutableMethodInvoke methodInvoke,
            String source, RuntimeServices runtimeServices, NodeEvaluator eval) {
        try {
            Object result = RuntimeInvocationSupport.invokeMethodHandleWithReceiver(
                    methodInvoke.handle(),
                    current,
                    methodInvoke.arguments(),
                    methodInvoke.parameterTypes(),
                    scope,
                    runtimeServices,
                    eval);
            return runtimeServices.coerceToResolvedType(result, methodInvoke.returnType());
        } catch (Error error) {
            throw error;
        } catch (Throwable throwable) {
            ExpressionEvaluationException exception = new ExpressionEvaluationException(
                    source, "METHOD_INVOKE_ERROR",
                    "error invoking '" + methodInvoke.name() + "' while navigating '" + rootName(node.root())
                    + "': " + throwable.getMessage(), null);
            exception.initCause(throwable);
            throw exception;
        }
    }

    @SuppressWarnings("unchecked")
    private static Object resolvePropertyReflective(String source, Object target, String name) {
        // For Map targets, a dot-notation property access degrades to a key lookup
        if (target instanceof Map<?, ?> map) {
            return ((Map<String, Object>) map).get(name);
        }
        Class<?> cls = target.getClass();
        MethodHandle handle = TypeIntrospectionSupport.cachedProperty(cls, name);
        if (handle == null) {
            throw new ExpressionEvaluationException(source, "UNKNOWN_PROPERTY",
                    "property '" + name + "' not found on " + cls.getSimpleName(), null);
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

    private static Object invokeMethodReflective(String source, Object target, String name, Object[] args) {
        Class<?> cls = target.getClass();
        MethodHandle handle = TypeIntrospectionSupport.cachedMethod(cls, name, args.length);
        if (handle == null) {
            throw new ExpressionEvaluationException(source, "UNKNOWN_METHOD",
                    "method '" + name + "' with " + args.length + " argument(s) not found on " + cls.getSimpleName(), null);
        }
        try {
            return RuntimeInvocationSupport.invokeMethodHandleWithReceiver(handle, target, args);
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

    private static List<Object> projectPropertyOverList(String source, List<?> list, String propertyName) {
        List<Object> result = new ArrayList<>(list.size());
        for (Object element : list) {
            if (element != null) {
                result.add(resolvePropertyReflective(source, element, propertyName));
            }
        }
        return result;
    }

    private static List<Object> projectFieldGetOverList(List<?> list, ExecutablePropertyChain node,
            ExecutablePropertyChain.ExecutableFieldGet fieldGet, String source, RuntimeServices runtimeServices) {
        List<Object> result = new ArrayList<>(list.size());
        for (Object element : list) {
            if (element != null) {
                result.add(invokeGetter(node, element, fieldGet, source, runtimeServices));
            }
        }
        return result;
    }

    /**
     * Returns {@code true} when the root of a property chain is the {@code @} sentinel, the chain
     * is non-empty, the first step is a {@link ExecutablePropertyChain.ReflectivePropertyAccess}
     * named {@code "key"} or {@code "value"}, and evaluation is currently inside a map-entry filter
     * ({@link FilterContext#isMapContext()} is {@code true}).
     */
    private static boolean isMapKeySentinel(ExecutableNode root,
            List<ExecutablePropertyChain.ExecutableAccess> chain) {
        if (root instanceof ExecutableIdentifier id
                && LanguageSymbols.CURRENT_ELEMENT.equals(id.ref().name())
                && !chain.isEmpty()
                && chain.getFirst() instanceof ExecutablePropertyChain.ReflectivePropertyAccess rpa
                && ("key".equals(rpa.name()) || "value".equals(rpa.name()))) {
            FilterContext mapCtx = FilterContextStack.INSTANCE.get().peek();
            return mapCtx != null && mapCtx.isMapContext();
        }
        return false;
    }

    private static boolean isSafeAccess(ExecutablePropertyChain.ExecutableAccess access) {
        return switch (access) {
            case ExecutablePropertyChain.ExecutableFieldGet fieldGet -> fieldGet.safe();
            case ExecutablePropertyChain.ExecutableMethodInvoke methodInvoke -> methodInvoke.safe();
            case ExecutablePropertyChain.ReflectivePropertyAccess propertyAccess -> propertyAccess.safe();
            case ExecutablePropertyChain.ReflectiveMethodInvoke reflectiveMethodInvoke -> reflectiveMethodInvoke.safe();
            // Collection navigation steps — never null-safe (they propagate nulls differently)
            default -> false;
        };
    }

    private static String rootName(ExecutableNode root) {
        if (root instanceof ExecutableIdentifier id) {
            return id.ref().name();
        }
        return "[constant]";
    }

    /** Strict number coercion for index/slice operations — rejects non-numeric values early. */
    private static BigDecimal asBigDecimalStrict(Object value, RuntimeServices runtimeServices) {
        if (value instanceof BigDecimal bd) return bd;
        return runtimeServices.asNumber(value);
    }
}
