package com.runestone.expeval.internal.semantic;

import com.runestone.expeval.internal.ast.PropertyChainNode;

import java.util.List;

public final class NavigationStepClassifier {

    private NavigationStepClassifier() {
    }

    public static boolean isLegacyAccessChain(List<PropertyChainNode.MemberAccess> chain) {
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
