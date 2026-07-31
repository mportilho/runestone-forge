package com.runestone.expeval_mk3.internal.runtime;

import com.runestone.expeval_mk3.api.CollectionType;
import com.runestone.expeval_mk3.api.ExpressionType;
import com.runestone.expeval_mk3.api.SourceSpan;
import com.runestone.expeval_mk3.internal.ast.NodeId;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public record MembershipExecutableNode(
        NodeId id,
        SourceSpan sourceSpan,
        boolean negated,
        ExecutableNode element,
        ExecutableNode collection,
        ExpressionType collectionType) implements ExecutableNode {

    public MembershipExecutableNode {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(sourceSpan, "sourceSpan");
        Objects.requireNonNull(element, "element");
        Objects.requireNonNull(collection, "collection");
        Objects.requireNonNull(collectionType, "collectionType");
    }

    @Override
    public Object execute(ExecutionScope scope) {
        Object evaluatedElement = element.execute(scope);
        boolean contains;
        if (collectionType instanceof CollectionType type) {
            contains = false;
            for (Object value : (List<?>) collection.execute(scope)) {
                if (ExpressionRuntime.structuralEquals(evaluatedElement, value, type.elementType())) {
                    contains = true;
                    break;
                }
            }
        } else {
            contains = ((Map<?, ?>) collection.execute(scope)).containsKey(evaluatedElement);
        }
        return contains != negated;
    }
}
