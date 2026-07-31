package com.runestone.expeval_mk3.internal.semantics;

import com.runestone.expeval_mk3.api.CollectionOperationCatalog;
import com.runestone.expeval_mk3.api.ExpressionType;
import com.runestone.expeval_mk3.api.JavaWildcardChildDescriptor;
import com.runestone.expeval_mk3.api.RuntimeNullability;

import java.util.List;
import java.util.Objects;

public record WildcardNavigationBinding(
        ReceiverKind receiverKind,
        ExpressionType receiverType,
        ExpressionType elementType,
        RuntimeNullability resultNullability,
        CollectionShape resultShape,
        CollectionOperationCatalog.MaterializationPolicy materializationPolicy,
        boolean pure,
        List<JavaWildcardChildDescriptor> objectChildren) implements NavigationBinding {

    public WildcardNavigationBinding {
        Objects.requireNonNull(receiverKind, "receiverKind");
        Objects.requireNonNull(receiverType, "receiverType");
        Objects.requireNonNull(elementType, "elementType");
        Objects.requireNonNull(resultNullability, "resultNullability");
        Objects.requireNonNull(resultShape, "resultShape");
        Objects.requireNonNull(materializationPolicy, "materializationPolicy");
        objectChildren = List.copyOf(Objects.requireNonNull(objectChildren, "objectChildren"));
    }

    public enum ReceiverKind {
        COLLECTION,
        MAP,
        OBJECT
    }
}
