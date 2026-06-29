package com.runestone.expeval.internal.runtime;

import com.runestone.expeval.internal.navigation.VectorAggregationKind;

import java.util.List;

final class CollectionScalarAggregationPlanner {

    private CollectionScalarAggregationPlanner() {
    }

    static CollectionScalarAggregationProgram planOrNull(
            List<ExecutablePropertyChain.ExecutableAccess> chain) {
        if (chain.isEmpty() || !(chain.getLast() instanceof ExecutablePropertyChain.ExecutableVectorAggregation aggregation)) {
            return null;
        }
        if (!isScalarAggregation(aggregation.kind())) {
            return null;
        }

        int aggregationIndex = chain.size() - 1;
        int startIndex = aggregationIndex;
        for (int index = 0; index < aggregationIndex; index++) {
            if (chain.get(index) instanceof ExecutablePropertyChain.ExecutableDeepScan
                    || chain.get(index) instanceof ExecutablePropertyChain.ExecutableCollectionFunction) {
                return null;
            }
            if (isCollectionNavigationStarter(chain.get(index))) {
                startIndex = index;
                break;
            }
        }
        for (int index = startIndex; index < aggregationIndex; index++) {
            if (!isEligibleSuffixStep(chain.get(index))) {
                return null;
            }
        }
        return new DefaultCollectionScalarAggregationProgram(chain, startIndex, aggregationIndex, aggregation);
    }

    private static boolean isScalarAggregation(VectorAggregationKind kind) {
        return switch (kind) {
            case COUNT, SUM, AVG, MIN, MAX, PROD -> true;
        };
    }

    private static boolean isCollectionNavigationStarter(ExecutablePropertyChain.ExecutableAccess access) {
        return access instanceof ExecutablePropertyChain.ExecutableIndexAccess
                || access instanceof ExecutablePropertyChain.ExecutableSliceAccess
                || access instanceof ExecutablePropertyChain.ExecutableWildcard
                || access instanceof ExecutablePropertyChain.ExecutableFilterPredicate
                || access instanceof ExecutablePropertyChain.ExecutableMapProjection
                || access instanceof ExecutablePropertyChain.ExecutableVectorMap;
    }

    private static boolean isEligibleSuffixStep(ExecutablePropertyChain.ExecutableAccess access) {
        return access instanceof ExecutablePropertyChain.ExecutableIndexAccess
                || access instanceof ExecutablePropertyChain.ExecutableSliceAccess
                || access instanceof ExecutablePropertyChain.ExecutableWildcard
                || access instanceof ExecutablePropertyChain.ExecutableFilterPredicate
                || access instanceof ExecutablePropertyChain.ExecutableMapProjection
                || access instanceof ExecutablePropertyChain.ExecutableVectorMap
                || access instanceof ExecutablePropertyChain.ExecutableFieldGet
                || access instanceof ExecutablePropertyChain.ReflectivePropertyAccess;
    }
}
