package com.runestone.expeval.internal.runtime;

import com.runestone.expeval.internal.execution.plan.*;

import com.runestone.expeval.internal.LanguageSymbols;

final class FoldabilityAnalyzer {

    private FoldabilityAnalyzer() {
    }

    static boolean isFoldableAccess(ExecutablePropertyChain.ExecutableAccess access) {
        return switch (access) {
            case ExecutablePropertyChain.ExecutableFieldGet ignored -> true;
            case ExecutablePropertyChain.ReflectivePropertyAccess ignored -> true;
            case ExecutablePropertyChain.ExecutableMethodInvoke methodInvoke ->
                    methodInvoke.arguments().stream().allMatch(argument -> isFoldableNode(argument, false));
            case ExecutablePropertyChain.ReflectiveMethodInvoke ignored -> false;
            case ExecutablePropertyChain.ExecutableIndexAccess indexAccess ->
                    isFoldableNode(indexAccess.index(), false);
            case ExecutablePropertyChain.ExecutableMapKeyAccess ignored -> true;
            case ExecutablePropertyChain.ExecutableSliceAccess sliceAccess ->
                    (sliceAccess.start() == null || isFoldableNode(sliceAccess.start(), false))
                            && (sliceAccess.end() == null || isFoldableNode(sliceAccess.end(), false));
            case ExecutablePropertyChain.ExecutableWildcard ignored -> true;
            case ExecutablePropertyChain.ExecutableFilterPredicate filterPredicate ->
                    isFoldableNode(filterPredicate.predicate(), true);
            case ExecutablePropertyChain.ExecutableDeepScan ignored -> false;
            case ExecutablePropertyChain.ExecutableCollectionFunction collectionFunction ->
                    collectionFunction.binding().descriptor() != null
                            && collectionFunction.binding().descriptor().isFoldable()
                            && collectionFunction.arguments().stream().allMatch(argument -> isFoldableNode(argument, false));
            case ExecutablePropertyChain.ExecutableMapProjection ignored -> true;
            case ExecutablePropertyChain.ExecutableVectorAggregation aggregation ->
                    aggregation.transform() == null || isFoldableNode(aggregation.transform(), true);
            case ExecutablePropertyChain.ExecutableVectorMap vectorMap ->
                    isFoldableNode(vectorMap.transform(), true);
        };
    }

    static boolean isFoldableNode(ExecutableNode node, boolean allowFilterContext) {
        return switch (node) {
            case ExecutableLiteral ignored -> true;
            case ExecutableDynamicLiteral ignored -> false;
            case ExecutableIdentifier identifier ->
                    allowFilterContext && LanguageSymbols.CURRENT_ELEMENT.equals(identifier.ref().name());
            case ExecutablePropertyChain chain ->
                    isFoldablePropertyChainNode(chain, allowFilterContext);
            case ExecutableFunctionCall functionCall ->
                    isFoldableFunctionCall(functionCall, allowFilterContext);
            case ExecutableBinaryOp binaryOp ->
                    isFoldableNode(binaryOp.left(), allowFilterContext)
                            && isFoldableNode(binaryOp.right(), allowFilterContext);
            case ExecutableTernaryOp ternaryOp ->
                    isFoldableNode(ternaryOp.first(), allowFilterContext)
                            && isFoldableNode(ternaryOp.second(), allowFilterContext)
                            && isFoldableNode(ternaryOp.third(), allowFilterContext);
            case ExecutableUnaryOp unaryOp -> isFoldableNode(unaryOp.operand(), allowFilterContext);
            case ExecutablePostfixOp postfixOp -> isFoldableNode(postfixOp.operand(), allowFilterContext);
            case ExecutableConditional conditional ->
                    conditional.conditions().stream().allMatch(condition -> isFoldableNode(condition, allowFilterContext))
                            && conditional.results().stream().allMatch(result -> isFoldableNode(result, allowFilterContext))
                            && isFoldableNode(conditional.elseExpression(), allowFilterContext);
            case ExecutableSimpleConditional conditional ->
                    isFoldableNode(conditional.condition(), allowFilterContext)
                            && isFoldableNode(conditional.thenExpression(), allowFilterContext)
                            && isFoldableNode(conditional.elseExpression(), allowFilterContext);
            case ExecutableVectorLiteral vectorLiteral ->
                    vectorLiteral.isFolded()
                            || vectorLiteral.elements().stream().allMatch(element -> isFoldableNode(element, allowFilterContext));
            case ExecutableNullCoalesce nullCoalesce ->
                    isFoldableNode(nullCoalesce.left(), allowFilterContext)
                            && isFoldableNode(nullCoalesce.right(), allowFilterContext);
            case ExecutableRegexOp regexOp -> isFoldableNode(regexOp.subject(), allowFilterContext);
        };
    }

    private static boolean isFoldablePropertyChainNode(ExecutablePropertyChain chain, boolean allowFilterContext) {
        boolean rootIsFoldable = isFoldableNode(chain.root(), allowFilterContext);
        if (!rootIsFoldable) {
            return false;
        }
        for (ExecutablePropertyChain.ExecutableAccess access : chain.chain()) {
            if (!isFoldableAccess(access)) {
                return false;
            }
        }
        return true;
    }

    private static boolean isFoldableFunctionCall(ExecutableFunctionCall functionCall, boolean allowFilterContext) {
        if (functionCall.isFolded()) {
            return true;
        }
        return functionCall.binding().descriptor().isFoldable()
                && functionCall.arguments().stream().allMatch(argument -> isFoldableNode(argument, allowFilterContext));
    }
}
