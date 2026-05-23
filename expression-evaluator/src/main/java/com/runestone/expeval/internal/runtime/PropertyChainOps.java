package com.runestone.expeval.internal.runtime;

import com.runestone.expeval.api.ExpressionEvaluationException;
import com.runestone.expeval.internal.LanguageSymbols;
import com.runestone.expeval.internal.navigation.FilterContext;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.List;

final class PropertyChainOps {

    private PropertyChainOps() {}

    static Object evaluatePropertyChain(ExecutablePropertyChain node, ExecutionScope scope,
            String source, RuntimeServices runtimeServices, MathContext mathContext, NodeEvaluator eval) {
        if (node.legacyOnly()) {
            return evaluateLegacyPropertyChain(
                    node,
                    scope,
                    source,
                    eval,
                    new PropertyAccessEvaluator(source, runtimeServices, eval));
        }

        PropertyAccessEvaluator accessEvaluator = new PropertyAccessEvaluator(source, runtimeServices, eval);
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
                    current = accessEvaluator.projectReflectivePropertyOverList(list, pa.name());
                    continue;
                }
                if (access instanceof ExecutablePropertyChain.ExecutableFieldGet fieldGet) {
                    current = accessEvaluator.projectFieldGetOverList(list, node, fieldGet);
                    continue;
                }
            }
            current = switch (access) {
                case ExecutablePropertyChain.ExecutableFieldGet fieldGet ->
                        accessEvaluator.evaluateFieldGet(node, current, fieldGet);
                case ExecutablePropertyChain.ExecutableMethodInvoke methodInvoke ->
                        accessEvaluator.evaluateMethod(node, scope, current, methodInvoke);
                case ExecutablePropertyChain.ReflectivePropertyAccess propertyAccess ->
                        accessEvaluator.resolveReflectiveProperty(current, propertyAccess.name());
                case ExecutablePropertyChain.ReflectiveMethodInvoke reflectiveMethodInvoke ->
                        accessEvaluator.invokeReflectiveMethod(scope, current, reflectiveMethodInvoke);
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
            String source, NodeEvaluator eval, PropertyAccessEvaluator accessEvaluator) {
        Object current = eval.evaluate(node.root(), scope);
        List<ExecutablePropertyChain.ExecutableAccess> chain = node.chain();
        int chainStart = 0;
        if (isMapKeySentinel(node.root(), chain)) {
            FilterContext mapCtx = FilterContextStack.INSTANCE.get().peek();
            String sentinel = ((ExecutablePropertyChain.ReflectivePropertyAccess) chain.getFirst()).name();
            current = "key".equals(sentinel) ? mapCtx.mapKey() : mapCtx.mapValue();
            chainStart = 1;
        }
        for (ExecutablePropertyChain.ExecutableAccess access : chain.subList(chainStart, chain.size())) {
            if (current == null) {
                if (isSafeAccess(access)) {
                    return null;
                }
                throw new ExpressionEvaluationException(source, "NULL_IN_CHAIN",
                        "null value encountered navigating '" + rootName(node.root()) + "'", null);
            }
            current = switch (access) {
                case ExecutablePropertyChain.ExecutableFieldGet fieldGet ->
                        accessEvaluator.evaluateFieldGet(node, current, fieldGet);
                case ExecutablePropertyChain.ExecutableMethodInvoke methodInvoke ->
                        accessEvaluator.evaluateMethod(node, scope, current, methodInvoke);
                case ExecutablePropertyChain.ReflectivePropertyAccess propertyAccess ->
                        accessEvaluator.resolveReflectiveProperty(current, propertyAccess.name());
                case ExecutablePropertyChain.ReflectiveMethodInvoke reflectiveMethodInvoke ->
                        accessEvaluator.invokeReflectiveMethod(scope, current, reflectiveMethodInvoke);
                default -> throw new IllegalStateException("legacy property chain contains unsupported access: " + access);
            };
        }
        return current;
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
