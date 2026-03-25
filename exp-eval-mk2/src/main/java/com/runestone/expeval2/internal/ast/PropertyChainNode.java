package com.runestone.expeval2.internal.ast;

import java.util.List;
import java.util.Objects;

public record PropertyChainNode(
        NodeId nodeId,
        SourceSpan sourceSpan,
        String rootIdentifier,
        List<MemberAccess> chain
) implements ExpressionNode {

    public PropertyChainNode {
        Objects.requireNonNull(nodeId, "nodeId must not be null");
        Objects.requireNonNull(sourceSpan, "sourceSpan must not be null");
        Objects.requireNonNull(rootIdentifier, "rootIdentifier must not be null");
        chain = List.copyOf(Objects.requireNonNull(chain, "chain must not be null"));
    }

    public sealed interface MemberAccess permits PropertyAccess, SafePropertyAccess, MethodCallAccess, SafeMethodCallAccess {}

    public record PropertyAccess(String name) implements MemberAccess {
        public PropertyAccess {
            Objects.requireNonNull(name, "name must not be null");
        }
    }

    public record SafePropertyAccess(String name) implements MemberAccess {
        public SafePropertyAccess {
            Objects.requireNonNull(name, "name must not be null");
        }
    }

    public record MethodCallAccess(String name, List<ExpressionNode> arguments) implements MemberAccess {
        public MethodCallAccess {
            Objects.requireNonNull(name, "name must not be null");
            arguments = List.copyOf(Objects.requireNonNull(arguments, "arguments must not be null"));
        }
    }

    public record SafeMethodCallAccess(String name, List<ExpressionNode> arguments) implements MemberAccess {
        public SafeMethodCallAccess {
            Objects.requireNonNull(name, "name must not be null");
            arguments = List.copyOf(Objects.requireNonNull(arguments, "arguments must not be null"));
        }
    }
}
