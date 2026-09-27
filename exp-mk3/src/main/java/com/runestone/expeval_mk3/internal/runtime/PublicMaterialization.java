package com.runestone.expeval_mk3.internal.runtime;

import com.runestone.expeval_mk3.api.CollectionType;
import com.runestone.expeval_mk3.api.ExpressionType;
import com.runestone.expeval_mk3.api.ExpressionResourceLimits;
import com.runestone.expeval_mk3.api.MapType;
import com.runestone.expeval_mk3.api.ObjectType;
import com.runestone.expeval_mk3.api.ScalarType;
import com.runestone.expeval_mk3.api.SourceSpan;
import com.runestone.expeval_mk3.internal.diagnostics.DiagnosticCode;
import com.runestone.expeval_mk3.internal.diagnostics.RuntimeFailures;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/** Creates bounded immutable values at the public execution boundary. */
public final class PublicMaterialization {

    private PublicMaterialization() {
    }

    public static boolean isPubliclyExposable(ExpressionType type) {
        return switch (type) {
            case ScalarType ignored -> true;
            case CollectionType collectionType -> isPubliclyExposable(collectionType.elementType());
            case MapType mapType -> isPubliclyExposable(mapType.valueType());
            case ObjectType ignored -> false;
        };
    }

    public static Object materialize(Object value, ExpressionType type, int maxMaterializedSize, SourceSpan span) {
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

    public static Object materialize(Object value, ExpressionType type, int maxMaterializedSize,
                                     SourceSpan span, ExpressionResourceLimits limits) {
        return materialize(value, type, maxMaterializedSize, span, limits, null);
    }

    public static Object materialize(Object value, ExpressionType type, int maxMaterializedSize,
                                     SourceSpan span, ExpressionResourceLimits limits, ExecutionScope scope) {
        if (limits != null) {
            ValueShapeValidator.Violation violation = ValueShapeValidator.check(value, limits, scope, span);
            if (violation != null) {
                if (violation.kind() == ValueShapeValidator.Kind.FORBIDDEN_NULL) {
                    throw RuntimeFailures.forbiddenNull("public value container must not contain null", span);
                }
                throw RuntimeFailures.domainViolation(
                        violation.kind() == ValueShapeValidator.Kind.MATERIALIZATION
                                ? DiagnosticCode.RUNTIME_MATERIALIZATION_LIMIT_EXCEEDED
                                : DiagnosticCode.RUNTIME_VALUE_SHAPE_EXCEEDED,
                        violation.message(), span);
            }
        }
        return materialize(value, type, maxMaterializedSize, span, scope);
    }

    private static Object materialize(Object value, ExpressionType type, int maxMaterializedSize,
                                      SourceSpan span, ExecutionScope scope) {
        if (value == null) {
            throw RuntimeFailures.forbiddenNull("public expression result must not be null", span);
        }
        return switch (type) {
            case ScalarType scalarType -> materializeScalar(value, scalarType);
            case CollectionType collectionType -> materializeCollection(
                    value, collectionType, maxMaterializedSize, span, scope);
            case MapType mapType -> materializeMap(value, mapType, maxMaterializedSize, span, scope);
            case ObjectType ignored -> throw new IllegalStateException(
                    "ObjectType must not cross the public materialization boundary");
        };
    }

    private static Object materializeScalar(Object value, ScalarType scalarType) {
        Class<?> expectedType = switch (scalarType) {
            case NUMBER -> BigDecimal.class;
            case BOOLEAN -> Boolean.class;
            case STRING -> String.class;
            case DATE -> LocalDate.class;
            case TIME -> LocalTime.class;
            case DATETIME -> LocalDateTime.class;
        };
        if (!expectedType.isInstance(value)) {
            throw new IllegalStateException(
                    "expected " + expectedType.getName() + " for " + scalarType + " but found " + value.getClass());
        }
        return value;
    }

    private static List<Object> materializeCollection(
            Object value, CollectionType type, int maxMaterializedSize, SourceSpan span) {
        return materializeCollection(value, type, maxMaterializedSize, span, null);
    }

    private static List<Object> materializeCollection(
            Object value, CollectionType type, int maxMaterializedSize, SourceSpan span, ExecutionScope scope) {
        if (!(value instanceof List<?> elements)) {
            throw new IllegalStateException("expected a List for CollectionType but found " + value.getClass());
        }
        requireWithinLimit(elements.size(), maxMaterializedSize, span);
        List<Object> snapshot = new ArrayList<>(elements.size());
        for (int index = 0; index < elements.size(); index++) {
            visit(scope, span);
            Object element = elements.get(index);
            if (element == null) {
                throw RuntimeFailures.forbiddenNull("public collection element must not be null", span);
            }
            snapshot.add(materialize(element, type.elementType(), maxMaterializedSize, span, scope));
        }
        return List.copyOf(snapshot);
    }

    private static Map<String, Object> materializeMap(
            Object value, MapType type, int maxMaterializedSize, SourceSpan span) {
        return materializeMap(value, type, maxMaterializedSize, span, null);
    }

    private static Map<String, Object> materializeMap(
            Object value, MapType type, int maxMaterializedSize, SourceSpan span, ExecutionScope scope) {
        if (!(value instanceof Map<?, ?> entries)) {
            throw new IllegalStateException("expected a Map for MapType but found " + value.getClass());
        }
        requireWithinLimit(entries.size(), maxMaterializedSize, span);
        TreeMap<String, Object> canonicalOrder = new TreeMap<>();
        Iterator<? extends Map.Entry<?, ?>> iterator = entries.entrySet().iterator();
        while (iterator.hasNext()) {
            visit(scope, span);
            Map.Entry<?, ?> entry = iterator.next();
            if (!(entry.getKey() instanceof String key)) {
                throw new IllegalStateException("expected text-keyed entries for MapType but found " + entry.getKey());
            }
            Object entryValue = entry.getValue();
            if (entryValue == null) {
                throw RuntimeFailures.forbiddenNull("public map value must not be null", span);
            }
            canonicalOrder.put(key, materialize(entryValue, type.valueType(), maxMaterializedSize, span, scope));
        }
        return Collections.unmodifiableMap(canonicalOrder);
    }

    private static void visit(ExecutionScope scope, SourceSpan span) {
        if (scope != null) {
            scope.visitTraversalStep(span);
        }
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
