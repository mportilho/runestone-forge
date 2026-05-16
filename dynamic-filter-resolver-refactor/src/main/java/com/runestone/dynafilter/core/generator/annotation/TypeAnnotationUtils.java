package com.runestone.dynafilter.core.generator.annotation;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.runestone.dynafilter.core.annotation.Conjunction;
import com.runestone.dynafilter.core.annotation.ConjunctionFrom;
import com.runestone.dynafilter.core.annotation.Disjunction;
import com.runestone.dynafilter.core.annotation.DisjunctionFrom;
import com.runestone.dynafilter.core.annotation.Filter;
import com.runestone.dynafilter.core.annotation.FilterTarget;
import com.runestone.dynafilter.core.annotation.Statement;
import com.runestone.dynafilter.core.annotation.StatementFrom;
import com.runestone.dynafilter.core.exception.DynamicFilterConfigurationException;
import com.runestone.dynafilter.core.model.FilterRequestData;
import com.runestone.dynafilter.core.security.FilterPathPolicy;
import com.runestone.dynafilter.core.statement.LogicOperator;
import org.springframework.data.jpa.domain.Specification;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public final class TypeAnnotationUtils {

    private static final long DEFAULT_CACHE_MAX_SIZE = 4096L;
    private static final String CACHE_MAX_SIZE_PROPERTY = "runestone.dynafilter.annotation.cache.max-size";
    private static final Cache<AnnotationStatementInput, AnnotationMetadata> METADATA_CACHE = Caffeine.newBuilder()
            .maximumSize(cacheMaxSize())
            .build();

    private TypeAnnotationUtils() {
    }

    public static List<FilterAnnotationData> findAnnotationData(AnnotationStatementInput annotationStatementInput) {
        return metadata(annotationStatementInput).annotationData();
    }

    public static List<FilterRequestData> listAllFilterRequestData(AnnotationStatementInput annotationStatementInput) {
        return metadata(annotationStatementInput).allFilters();
    }

    public static Class<?> findFilterTargetClass(AnnotationStatementInput annotationStatementInput) {
        return metadata(annotationStatementInput).targetClass();
    }

    public static Field findFilterField(Class<?> clazz, String fieldName) {
        return findFilterField(clazz, fieldName, FilterPathPolicy.PERMISSIVE);
    }

    public static Field findFilterField(Class<?> clazz, String fieldName, FilterPathPolicy filterPathPolicy) {
        Objects.requireNonNull(clazz, "clazz must not be null");
        Objects.requireNonNull(fieldName, "fieldName must not be null");
        Objects.requireNonNull(filterPathPolicy, "filterPathPolicy must not be null").validate(fieldName);
        Class<?> currentType = clazz;
        Field currentField = null;
        for (String segment : fieldName.split("\\.")) {
            if (segment.isBlank()) {
                throw new DynamicFilterConfigurationException("Filter path contains a blank segment: " + fieldName);
            }
            currentField = findField(currentType, segment);
            if (currentField == null) {
                throw new DynamicFilterConfigurationException(
                        "Filter field not found: " + segment + " in " + currentType.getName()
                );
            }
            currentType = nextPathType(currentField);
        }
        return currentField;
    }

    public static void clearCache() {
        METADATA_CACHE.invalidateAll();
    }

    private static AnnotationMetadata metadata(AnnotationStatementInput annotationStatementInput) {
        Objects.requireNonNull(annotationStatementInput, "annotationStatementInput must not be null");
        return METADATA_CACHE.get(annotationStatementInput, TypeAnnotationUtils::buildMetadata);
    }

    private static AnnotationMetadata buildMetadata(AnnotationStatementInput input) {
        List<Annotation> annotations = collectAnnotations(input);
        Class<?> targetClass = resolveTargetClass(input, annotations);
        List<FilterAnnotationData> annotationData = new ArrayList<>();
        List<FilterRequestData> allFilters = new ArrayList<>();
        for (Annotation annotation : annotations) {
            FilterAnnotationData data = toAnnotationData(annotation);
            if (data != null) {
                validateFilters(data.filters(), targetClass);
                for (FilterAnnotationStatement statement : data.filterStatements()) {
                    validateFilters(statement.filters(), targetClass);
                }
                annotationData.add(data);
                allFilters.addAll(data.filters().stream().map(FilterRequestData::from).toList());
                for (FilterAnnotationStatement statement : data.filterStatements()) {
                    allFilters.addAll(statement.filters().stream().map(FilterRequestData::from).toList());
                }
            }
        }
        return new AnnotationMetadata(annotationData, allFilters, targetClass);
    }

    private static List<Annotation> collectAnnotations(AnnotationStatementInput input) {
        List<Annotation> annotations = new ArrayList<>();
        Set<Class<?>> visitedTypes = new HashSet<>();
        Set<Class<? extends Annotation>> visitedAnnotations = new HashSet<>();
        Set<Annotation> collectedAnnotations = new HashSet<>();
        for (Annotation annotation : input.annotations()) {
            collectAnnotation(annotation, annotations, collectedAnnotations, visitedAnnotations);
        }
        collectTypeAnnotations(input.type(), annotations, collectedAnnotations, visitedTypes, visitedAnnotations);
        return annotations;
    }

    private static void collectTypeAnnotations(
            Class<?> type,
            List<Annotation> annotations,
            Set<Annotation> collectedAnnotations,
            Set<Class<?>> visitedTypes,
            Set<Class<? extends Annotation>> visitedAnnotations
    ) {
        if (type == null || !visitedTypes.add(type) || isJavaType(type)) {
            return;
        }
        for (Annotation annotation : type.getAnnotations()) {
            collectAnnotation(annotation, annotations, collectedAnnotations, visitedAnnotations);
        }
        for (Class<?> interfaceType : type.getInterfaces()) {
            collectTypeAnnotations(interfaceType, annotations, collectedAnnotations, visitedTypes, visitedAnnotations);
        }
        collectTypeAnnotations(type.getSuperclass(), annotations, collectedAnnotations, visitedTypes, visitedAnnotations);
    }

    private static void collectAnnotation(
            Annotation annotation,
            List<Annotation> annotations,
            Set<Annotation> collectedAnnotations,
            Set<Class<? extends Annotation>> visitedAnnotations
    ) {
        if (annotation == null) {
            return;
        }
        if (!collectedAnnotations.add(annotation)) {
            return;
        }
        annotations.add(annotation);
        Class<? extends Annotation> annotationType = annotation.annotationType();
        if (!visitedAnnotations.add(annotationType) || annotationType.getPackageName().startsWith("java.lang.annotation")) {
            return;
        }
        for (Annotation metaAnnotation : annotationType.getAnnotations()) {
            collectAnnotation(metaAnnotation, annotations, collectedAnnotations, visitedAnnotations);
        }
    }

    private static FilterAnnotationData toAnnotationData(Annotation annotation) {
        return switch (annotation) {
            case Conjunction conjunction -> new FilterAnnotationData(
                    LogicOperator.CONJUNCTION,
                    List.of(conjunction.value()),
                    inlineStatements(conjunction.disjunctions()),
                    conjunction.negate()
            );
            case Disjunction disjunction -> new FilterAnnotationData(
                    LogicOperator.DISJUNCTION,
                    List.of(disjunction.value()),
                    inlineStatements(disjunction.conjunctions()),
                    disjunction.negate()
            );
            case ConjunctionFrom conjunctionFrom -> new FilterAnnotationData(
                    LogicOperator.CONJUNCTION,
                    filtersFromClass(conjunctionFrom.value()),
                    externalStatements(conjunctionFrom.disjunctions()),
                    conjunctionFrom.negate()
            );
            case DisjunctionFrom disjunctionFrom -> new FilterAnnotationData(
                    LogicOperator.DISJUNCTION,
                    filtersFromClass(disjunctionFrom.value()),
                    externalStatements(disjunctionFrom.conjunctions()),
                    disjunctionFrom.negate()
            );
            default -> null;
        };
    }

    private static List<FilterAnnotationStatement> inlineStatements(Statement[] statements) {
        List<FilterAnnotationStatement> result = new ArrayList<>();
        for (Statement statement : statements) {
            result.add(new FilterAnnotationStatement(List.of(statement.value()), statement.negate()));
        }
        return result;
    }

    private static List<FilterAnnotationStatement> externalStatements(StatementFrom[] statements) {
        List<FilterAnnotationStatement> result = new ArrayList<>();
        for (StatementFrom statement : statements) {
            result.add(new FilterAnnotationStatement(filtersFromClass(statement.value()), statement.negate()));
        }
        return result;
    }

    private static List<Filter> filtersFromClass(Class<?> type) {
        List<Filter> filters = new ArrayList<>();
        Class<?> currentType = type;
        while (currentType != null && !isJavaType(currentType)) {
            for (Field field : currentType.getDeclaredFields()) {
                Filter filter = field.getAnnotation(Filter.class);
                if (filter != null) {
                    filters.add(filter);
                }
            }
            currentType = currentType.getSuperclass();
        }
        return filters;
    }

    private static void validateFilters(List<Filter> filters, Class<?> targetClass) {
        for (Filter filter : filters) {
            validateFilter(filter, targetClass);
        }
    }

    private static void validateFilter(Filter filter, Class<?> targetClass) {
        if (filter.parameters().length == 0) {
            throw new DynamicFilterConfigurationException("Filter must declare at least one parameter");
        }
        for (String parameter : filter.parameters()) {
            if (parameter == null || parameter.isBlank()) {
                throw new DynamicFilterConfigurationException("Filter parameters must not be blank");
            }
        }
        if (filter.defaultValues().length > 0 && filter.defaultValues().length != filter.parameters().length) {
            throw new DynamicFilterConfigurationException("defaultValues must have the same size as parameters");
        }
        if (filter.constantValues().length > 0 && filter.constantValues().length != filter.parameters().length) {
            throw new DynamicFilterConfigurationException("constantValues must have the same size as parameters");
        }
        if (targetClass != Object.class) {
            findFilterField(targetClass, filter.path());
        }
    }

    private static Class<?> resolveTargetClass(AnnotationStatementInput input, List<Annotation> annotations) {
        for (Annotation annotation : annotations) {
            Class<?> target = targetFromAnnotation(annotation);
            if (target != null && target != Object.class) {
                return target;
            }
        }
        Class<?> targetFromType = targetFromType(input.type());
        return targetFromType == null ? Object.class : targetFromType;
    }

    private static Class<?> targetFromAnnotation(Annotation annotation) {
        return switch (annotation) {
            case FilterTarget filterTarget -> filterTarget.value();
            case Conjunction conjunction -> conjunction.target();
            case Disjunction disjunction -> disjunction.target();
            case ConjunctionFrom conjunctionFrom -> targetFromType(conjunctionFrom.value());
            case DisjunctionFrom disjunctionFrom -> targetFromType(disjunctionFrom.value());
            default -> {
                Class<?> target = targetFromType(annotation.annotationType());
                yield target == null ? Object.class : target;
            }
        };
    }

    private static Class<?> targetFromType(Class<?> type) {
        if (type == null) {
            return null;
        }
        FilterTarget filterTarget = type.getAnnotation(FilterTarget.class);
        if (filterTarget != null) {
            return filterTarget.value();
        }
        if (type.getGenericInterfaces().length > 0) {
            for (Type genericInterface : type.getGenericInterfaces()) {
                Class<?> specificationTarget = targetFromSpecification(genericInterface);
                if (specificationTarget != null) {
                    return specificationTarget;
                }
            }
        }
        return null;
    }

    private static Class<?> targetFromSpecification(Type type) {
        if (!(type instanceof ParameterizedType parameterizedType)) {
            return null;
        }
        if (!parameterizedType.getRawType().equals(Specification.class)) {
            return null;
        }
        Type argument = parameterizedType.getActualTypeArguments()[0];
        return argument instanceof Class<?> targetClass ? targetClass : null;
    }

    private static Field findField(Class<?> type, String fieldName) {
        Class<?> currentType = type;
        while (currentType != null && !isJavaType(currentType)) {
            try {
                return currentType.getDeclaredField(fieldName);
            } catch (NoSuchFieldException ignored) {
                currentType = currentType.getSuperclass();
            }
        }
        return null;
    }

    private static Class<?> nextPathType(Field field) {
        if (Iterable.class.isAssignableFrom(field.getType())) {
            return collectionElementType(field);
        }
        return field.getType();
    }

    private static Class<?> collectionElementType(Field field) {
        Type genericType = field.getGenericType();
        if (!(genericType instanceof ParameterizedType parameterizedType)) {
            throw new DynamicFilterConfigurationException("Collection path segment must declare a concrete generic type: " + field.getName());
        }
        Type argument = parameterizedType.getActualTypeArguments()[0];
        if (argument instanceof Class<?> argumentClass) {
            return argumentClass;
        }
        if (argument instanceof ParameterizedType parameterizedArgument && parameterizedArgument.getRawType() instanceof Class<?> rawClass) {
            return rawClass;
        }
        if (argument instanceof WildcardType || argument instanceof TypeVariable<?>) {
            throw new DynamicFilterConfigurationException("Collection generic type must be materialized: " + field.getName());
        }
        throw new DynamicFilterConfigurationException("Unsupported collection generic type: " + field.getName());
    }

    private static boolean isJavaType(Class<?> type) {
        return type.getPackageName().startsWith("java.");
    }

    private static long cacheMaxSize() {
        return Long.getLong(CACHE_MAX_SIZE_PROPERTY, DEFAULT_CACHE_MAX_SIZE);
    }

    private record AnnotationMetadata(
            List<FilterAnnotationData> annotationData,
            List<FilterRequestData> allFilters,
            Class<?> targetClass
    ) {
        private AnnotationMetadata {
            annotationData = List.copyOf(annotationData);
            allFilters = List.copyOf(allFilters);
        }
    }
}
