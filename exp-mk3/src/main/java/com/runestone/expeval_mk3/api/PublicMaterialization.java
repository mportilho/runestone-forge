package com.runestone.expeval_mk3.api;

import com.runestone.expeval_mk3.internal.diagnostics.DiagnosticCode;
import com.runestone.expeval_mk3.internal.diagnostics.RuntimeFailures;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * The single type-directed boundary that turns a computed internal value into its canonical public
 * representation: scalars as-is, collections and maps as recursive, immutable, size-checked snapshots
 * detached from any internal or provider-owned structure. Shared by every public expression view,
 * including the future assignments view.
 */
final class PublicMaterialization {

    private PublicMaterialization() {
    }

    static boolean isPubliclyExposable(ExpressionType type) {
        return switch (type) {
            case ScalarType ignored -> true;
            case CollectionType collectionType -> isPubliclyExposable(collectionType.elementType());
            case MapType mapType -> isPubliclyExposable(mapType.valueType());
            case ObjectType ignored -> false;
        };
    }

    static Object materialize(Object value, ExpressionType type, int maxMaterializedSize, SourceSpan span) {
        if (value == null) {
            throw RuntimeFailures.forbiddenNull("public expression result must not be null", span);
        }
        return switch (type) {
            case ScalarType scalarType -> materializeScalar(value, scalarType);
            case CollectionType collectionType -> materializeCollection(value, collectionType, maxMaterializedSize, span);
            case MapType mapType -> materializeMap(value, mapType, maxMaterializedSize, span);
            case ObjectType ignored -> throw new IllegalStateException(
                    "ObjectType must not cross the public materialization boundary");
        };
    }

    private static Object materializeScalar(Object value, ScalarType scalarType) {
        Class<?> expectedType = ExpressionJavaTypes.scalarValueType(scalarType);
        if (!expectedType.isInstance(value)) {
            throw new IllegalStateException(
                    "expected " + expectedType.getName() + " for " + scalarType + " but found " + value.getClass());
        }
        return value;
    }

    private static List<Object> materializeCollection(
            Object value, CollectionType type, int maxMaterializedSize, SourceSpan span) {
        if (!(value instanceof List<?> elements)) {
            throw new IllegalStateException("expected a List for CollectionType but found " + value.getClass());
        }
        requireWithinLimit(elements.size(), maxMaterializedSize, span);
        List<Object> snapshot = new ArrayList<>(elements.size());
        for (Object element : elements) {
            if (element == null) {
                throw RuntimeFailures.forbiddenNull("public collection element must not be null", span);
            }
            snapshot.add(materialize(element, type.elementType(), maxMaterializedSize, span));
        }
        return List.copyOf(snapshot);
    }

    private static Map<String, Object> materializeMap(
            Object value, MapType type, int maxMaterializedSize, SourceSpan span) {
        if (!(value instanceof Map<?, ?> entries)) {
            throw new IllegalStateException("expected a Map for MapType but found " + value.getClass());
        }
        requireWithinLimit(entries.size(), maxMaterializedSize, span);
        TreeMap<String, Object> canonicalOrder = new TreeMap<>();
        for (Map.Entry<?, ?> entry : entries.entrySet()) {
            if (!(entry.getKey() instanceof String key)) {
                throw new IllegalStateException("expected text-keyed entries for MapType but found " + entry.getKey());
            }
            Object entryValue = entry.getValue();
            if (entryValue == null) {
                throw RuntimeFailures.forbiddenNull("public map value must not be null", span);
            }
            canonicalOrder.put(key, materialize(entryValue, type.valueType(), maxMaterializedSize, span));
        }
        return Collections.unmodifiableMap(canonicalOrder);
    }

    private static void requireWithinLimit(int size, int maxMaterializedSize, SourceSpan span) {
        if (size > maxMaterializedSize) {
            throw RuntimeFailures.domainViolation(
                    DiagnosticCode.RUNTIME_MATERIALIZATION_LIMIT_EXCEEDED,
                    "public expression result exceeds maxMaterializedSize " + maxMaterializedSize,
                    span);
        }
    }
}
