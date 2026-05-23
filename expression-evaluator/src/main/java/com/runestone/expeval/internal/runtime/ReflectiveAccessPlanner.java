package com.runestone.expeval.internal.runtime;

import com.runestone.expeval.internal.execution.plan.*;

import com.runestone.expeval.internal.ast.PropertyChainNode;

import java.util.List;

final class ReflectiveAccessPlanner {

    private ReflectiveAccessPlanner() {
    }

    static ExecutablePropertyChain.ExecutableAccess build(
            PropertyChainNode.MemberAccess access,
            List<ExecutableNode> argumentNodes,
            boolean safe) {
        return switch (access) {
            case PropertyChainNode.PropertyAccess propertyAccess ->
                    new ExecutablePropertyChain.ReflectivePropertyAccess(propertyAccess.name(), safe);
            case PropertyChainNode.SafePropertyAccess safePropertyAccess ->
                    new ExecutablePropertyChain.ReflectivePropertyAccess(safePropertyAccess.name(), true);
            case PropertyChainNode.MethodCallAccess methodCall ->
                    new ExecutablePropertyChain.ReflectiveMethodInvoke(methodCall.name(), argumentNodes, safe);
            case PropertyChainNode.SafeMethodCallAccess safeMethodCall ->
                    new ExecutablePropertyChain.ReflectiveMethodInvoke(safeMethodCall.name(), argumentNodes, true);
            default -> throw new IllegalStateException("Unexpected access type in reflective access planner: " + access);
        };
    }
}
