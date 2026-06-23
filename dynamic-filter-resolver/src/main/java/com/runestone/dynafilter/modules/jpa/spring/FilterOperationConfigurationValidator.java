package com.runestone.dynafilter.modules.jpa.spring;

import com.runestone.dynafilter.core.exceptions.DynamicFilterConfigurationException;
import com.runestone.dynafilter.core.model.FilterRequestData;
import com.runestone.dynafilter.core.model.modifiers.ModIgnoreCase;
import com.runestone.dynafilter.core.model.modifiers.ModIgnorePath;
import com.runestone.dynafilter.core.operation.FilterArity;
import com.runestone.dynafilter.core.operation.FilterOperationMetadata;
import com.runestone.dynafilter.core.operation.types.Between;
import com.runestone.dynafilter.core.operation.types.EndsWith;
import com.runestone.dynafilter.core.operation.types.Equals;
import com.runestone.dynafilter.core.operation.types.Greater;
import com.runestone.dynafilter.core.operation.types.GreaterOrEquals;
import com.runestone.dynafilter.core.operation.types.IsIn;
import com.runestone.dynafilter.core.operation.types.Less;
import com.runestone.dynafilter.core.operation.types.LessOrEquals;
import com.runestone.dynafilter.core.operation.types.Like;
import com.runestone.dynafilter.core.operation.types.StartsWith;
import com.runestone.dynafilter.core.operation.types.extensions.AnyFieldLike;
import com.runestone.dynafilter.core.operation.types.extensions.CollectionSize;
import com.runestone.dynafilter.core.operation.types.extensions.ContainsAll;
import com.runestone.dynafilter.core.operation.types.extensions.EffectiveAtClosed;
import com.runestone.dynafilter.core.operation.types.extensions.EffectiveAtHalfOpen;
import com.runestone.dynafilter.core.operation.types.extensions.EffectiveAtOpen;
import com.runestone.dynafilter.core.operation.types.extensions.IsBlank;
import com.runestone.dynafilter.core.operation.types.extensions.IsEmptyCollection;
import com.runestone.dynafilter.core.operation.types.extensions.NullOrGreater;
import com.runestone.dynafilter.core.operation.types.extensions.NullOrGreaterOrEquals;
import com.runestone.dynafilter.core.operation.types.extensions.NullOrLess;
import com.runestone.dynafilter.core.operation.types.extensions.NullOrLessOrEquals;
import com.runestone.dynafilter.core.operation.types.extensions.OnDate;
import com.runestone.dynafilter.core.operation.types.extensions.PeriodOverlapsClosed;
import com.runestone.dynafilter.core.operation.types.extensions.PeriodOverlapsHalfOpen;
import com.runestone.dynafilter.core.operation.types.extensions.PeriodOverlapsOpen;
import com.runestone.dynafilter.core.operation.types.extensions.SizeBetween;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;

import static com.runestone.dynafilter.core.generator.annotation.TypeAnnotationUtils.findFilterField;
import static com.runestone.dynafilter.helpers.StringHelper.formatPath;

final class FilterOperationConfigurationValidator {

    private FilterOperationConfigurationValidator() {
    }

    static void validateMetadata(FilterRequestData filter, FilterOperationMetadata metadata) {
        validatePathArity(filter, metadata);
        validateValueArity(filter, metadata);
    }

    private static void validatePathArity(FilterRequestData filter, FilterOperationMetadata metadata) {
        if (!metadata.pathArity().accepts(filter.path().length)) {
            throw new DynamicFilterConfigurationException(
                    "Filter operation '%s' used on path '%s' requires %s path(s), but configured count is %d"
                            .formatted(filter.operation().getCanonicalName(), formatPath(filter.path()), formatArity(metadata.pathArity()), filter.path().length)
            );
        }
    }

    private static void validateValueArity(FilterRequestData filter, FilterOperationMetadata metadata) {
        if (!metadata.valueArity().accepts(filter.parameters().length)) {
            throw new DynamicFilterConfigurationException(
                    "Filter operation '%s' used on path '%s' requires %s parameter(s), but configured count is %d"
                            .formatted(filter.operation().getCanonicalName(), formatPath(filter.path()), formatArity(metadata.valueArity()), filter.parameters().length)
            );
        }
    }

    static void validateOperationSpecificConfiguration(FilterRequestData filter) {
        validatePathSyntax(filter);
        if (AnyFieldLike.class.equals(filter.operation())) {
            validateAnyFieldLike(filter);
        }
    }

    static void validateEntityConfiguration(FilterRequestData filter, Class<?> entityClass) {
        if (filter.modifiers() != null && filter.modifiers().contains(ModIgnorePath.class)) {
            return;
        }

        Field[] fields = resolveFields(filter, entityClass);
        validateTextPaths(filter, fields);
        validateCollectionPaths(filter, fields);
        validateComparablePaths(filter, fields);
        validateOnDatePath(filter, fields);
        validateIgnoreCasePaths(filter, fields);
    }

    private static void validatePathSyntax(FilterRequestData filter) {
        for (String path : filter.path()) {
            if (path == null || path.trim().isEmpty()) {
                throw new DynamicFilterConfigurationException("Path cannot be empty");
            }
            String trimmed = path.trim();
            if (trimmed.charAt(0) == '.' || trimmed.charAt(trimmed.length() - 1) == '.' || trimmed.contains("..")) {
                throw new DynamicFilterConfigurationException("Invalid path segment on path '%s'".formatted(path));
            }
        }
    }

    private static Field[] resolveFields(FilterRequestData filter, Class<?> entityClass) {
        String[] paths = filter.path();
        Field[] fields = new Field[paths.length];
        for (int i = 0; i < paths.length; i++) {
            fields[i] = findFilterField(entityClass, paths[i]);
        }
        return fields;
    }

    private static void validateTextPaths(FilterRequestData filter, Field[] fields) {
        if (!isTextOperation(filter)) {
            return;
        }

        for (int i = 0; i < fields.length; i++) {
            Class<?> fieldType = fields[i].getType();
            if (!String.class.equals(fieldType)) {
                throw new DynamicFilterConfigurationException(
                        "Filter operation '%s' used on path '%s' requires a String path, but found %s"
                                .formatted(filter.operation().getCanonicalName(), filter.path()[i], fieldType.getCanonicalName())
                );
            }
        }
    }

    private static void validateCollectionPaths(FilterRequestData filter, Field[] fields) {
        if (!isCollectionOperation(filter)) {
            return;
        }

        for (int i = 0; i < fields.length; i++) {
            Class<?> fieldType = fields[i].getType();
            if (!Collection.class.isAssignableFrom(fieldType)) {
                throw new DynamicFilterConfigurationException(
                        "Filter operation '%s' used on path '%s' requires a collection path, but found %s"
                                .formatted(filter.operation().getCanonicalName(), filter.path()[i], fieldType.getCanonicalName())
                );
            }
        }
    }

    private static void validateComparablePaths(FilterRequestData filter, Field[] fields) {
        if (!isComparableOperation(filter) && !isIntervalOperation(filter)) {
            return;
        }

        for (int i = 0; i < fields.length; i++) {
            Class<?> fieldType = fields[i].getType();
            if (!isComparableType(fieldType)) {
                String requirement = isIntervalOperation(filter) ? "requires comparable path(s)" : "requires a comparable path";
                throw new DynamicFilterConfigurationException(
                        "Filter operation '%s' used on path '%s' %s, but found %s"
                                .formatted(filter.operation().getCanonicalName(), filter.path()[i], requirement, fieldType.getCanonicalName())
                );
            }
        }
    }

    private static void validateOnDatePath(FilterRequestData filter, Field[] fields) {
        if (!OnDate.class.equals(filter.operation())) {
            return;
        }

        for (int i = 0; i < fields.length; i++) {
            Class<?> fieldType = fields[i].getType();
            if (!isDateLikeType(fieldType)) {
                throw new DynamicFilterConfigurationException(
                        "Filter operation '%s' used on path '%s' supports LocalDate, LocalDateTime, Instant and Date paths, but found %s"
                                .formatted(filter.operation().getCanonicalName(), filter.path()[i], fieldType.getCanonicalName())
                );
            }
        }
    }

    private static void validateIgnoreCasePaths(FilterRequestData filter, Field[] fields) {
        if (filter.modifiers() == null || !filter.modifiers().contains(ModIgnoreCase.class) || !supportsIgnoreCase(filter)) {
            return;
        }

        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            if (String.class.equals(field.getType())) {
                continue;
            }
            if (IsIn.class.equals(filter.operation()) && Collection.class.isAssignableFrom(field.getType()) && String.class.equals(findCollectionElementType(field))) {
                continue;
            }
            throw new DynamicFilterConfigurationException(
                    "Filter operation '%s' used on path '%s' supports ModIgnoreCase only for String paths, but found %s"
                            .formatted(filter.operation().getCanonicalName(), filter.path()[i], field.getType().getCanonicalName())
            );
        }
    }

    private static void validateAnyFieldLike(FilterRequestData filter) {
        HashSet<String> uniquePaths = new HashSet<>();
        for (String path : filter.path()) {
            if (!uniquePaths.add(path)) {
                throw new DynamicFilterConfigurationException(
                        "Filter operation '%s' has path value '%s' configured more than once in path '%s'"
                                .formatted(filter.operation().getCanonicalName(), path, formatPath(filter.path()))
                );
            }
        }
    }

    private static String formatArity(FilterArity arity) {
        if (arity.min() == arity.max()) {
            return "exactly " + arity.min();
        }
        if (arity.max() == FilterArity.UNBOUNDED) {
            return "at least " + arity.min();
        }
        return "between %d and %d".formatted(arity.min(), arity.max());
    }

    private static boolean isTextOperation(FilterRequestData filter) {
        Class<?> operation = filter.operation();
        return Like.class.equals(operation)
               || StartsWith.class.equals(operation)
               || EndsWith.class.equals(operation)
               || AnyFieldLike.class.equals(operation)
               || IsBlank.class.equals(operation);
    }

    private static boolean isCollectionOperation(FilterRequestData filter) {
        Class<?> operation = filter.operation();
        return ContainsAll.class.equals(operation)
               || CollectionSize.class.equals(operation)
               || IsEmptyCollection.class.equals(operation)
               || SizeBetween.class.equals(operation);
    }

    private static boolean isComparableOperation(FilterRequestData filter) {
        Class<?> operation = filter.operation();
        return Greater.class.equals(operation)
               || GreaterOrEquals.class.equals(operation)
               || Less.class.equals(operation)
               || LessOrEquals.class.equals(operation)
               || Between.class.equals(operation)
               || NullOrGreater.class.equals(operation)
               || NullOrGreaterOrEquals.class.equals(operation)
               || NullOrLess.class.equals(operation)
               || NullOrLessOrEquals.class.equals(operation);
    }

    private static boolean isIntervalOperation(FilterRequestData filter) {
        Class<?> operation = filter.operation();
        return EffectiveAtClosed.class.equals(operation)
               || EffectiveAtHalfOpen.class.equals(operation)
               || EffectiveAtOpen.class.equals(operation)
               || PeriodOverlapsClosed.class.equals(operation)
               || PeriodOverlapsHalfOpen.class.equals(operation)
               || PeriodOverlapsOpen.class.equals(operation);
    }

    private static boolean supportsIgnoreCase(FilterRequestData filter) {
        Class<?> operation = filter.operation();
        return Equals.class.equals(operation)
               || Greater.class.equals(operation)
               || GreaterOrEquals.class.equals(operation)
               || Less.class.equals(operation)
               || LessOrEquals.class.equals(operation)
               || Between.class.equals(operation)
               || IsIn.class.equals(operation)
               || Like.class.equals(operation)
               || StartsWith.class.equals(operation)
               || EndsWith.class.equals(operation)
               || AnyFieldLike.class.equals(operation);
    }

    private static boolean isComparableType(Class<?> type) {
        Class<?> wrappedType = wrapPrimitive(type);
        return Number.class.isAssignableFrom(wrappedType) || Comparable.class.isAssignableFrom(wrappedType);
    }

    private static boolean isDateLikeType(Class<?> type) {
        return LocalDate.class.equals(type)
               || LocalDateTime.class.equals(type)
               || Instant.class.equals(type)
               || Date.class.isAssignableFrom(type);
    }

    private static Class<?> findCollectionElementType(Field field) {
        Type genericType = field.getGenericType();
        if (genericType instanceof ParameterizedType parameterizedType) {
            Type actualType = parameterizedType.getActualTypeArguments()[0];
            if (actualType instanceof Class<?> actualClass) {
                return actualClass;
            }
        }
        return Object.class;
    }

    private static Class<?> wrapPrimitive(Class<?> type) {
        if (!type.isPrimitive()) {
            return type;
        }
        if (int.class.equals(type)) {
            return Integer.class;
        }
        if (long.class.equals(type)) {
            return Long.class;
        }
        if (double.class.equals(type)) {
            return Double.class;
        }
        if (float.class.equals(type)) {
            return Float.class;
        }
        if (short.class.equals(type)) {
            return Short.class;
        }
        if (byte.class.equals(type)) {
            return Byte.class;
        }
        if (char.class.equals(type)) {
            return Character.class;
        }
        if (boolean.class.equals(type)) {
            return Boolean.class;
        }
        return type;
    }
}
