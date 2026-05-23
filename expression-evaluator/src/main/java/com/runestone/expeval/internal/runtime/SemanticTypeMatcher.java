package com.runestone.expeval.internal.runtime;

import com.runestone.expeval.types.NullType;
import com.runestone.expeval.types.ResolvedType;
import com.runestone.expeval.types.UnknownType;

import java.util.List;

final class SemanticTypeMatcher {

    private SemanticTypeMatcher() {
    }

    static boolean matchesArguments(List<ResolvedType> expectedTypes, List<ResolvedType> actualTypes) {
        if (expectedTypes.size() != actualTypes.size()) {
            return false;
        }
        for (int index = 0; index < expectedTypes.size(); index++) {
            ResolvedType expectedType = expectedTypes.get(index);
            ResolvedType actualType = actualTypes.get(index);
            if (actualType == NullType.INSTANCE || expectedType == NullType.INSTANCE) {
                continue;
            }
            if (actualType != UnknownType.INSTANCE && expectedType != UnknownType.INSTANCE && !actualType.equals(expectedType)) {
                return false;
            }
        }
        return true;
    }
}
