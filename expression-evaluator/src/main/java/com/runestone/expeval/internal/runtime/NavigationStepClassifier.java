package com.runestone.expeval.internal.runtime;

import com.runestone.expeval.internal.ast.PropertyChainNode;

import java.util.List;

final class NavigationStepClassifier {

    private NavigationStepClassifier() {
    }

    static boolean isLegacyAccessChain(List<PropertyChainNode.MemberAccess> chain) {
        for (PropertyChainNode.MemberAccess access : chain) {
            if (!isLegacyAccess(access)) {
                return false;
            }
        }
        return true;
    }

    private static boolean isLegacyAccess(PropertyChainNode.MemberAccess access) {
        return access instanceof PropertyChainNode.PropertyAccess
                || access instanceof PropertyChainNode.SafePropertyAccess
                || access instanceof PropertyChainNode.MethodCallAccess
                || access instanceof PropertyChainNode.SafeMethodCallAccess;
    }
}
