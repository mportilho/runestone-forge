/*
 * MIT License
 * <p>
 * Copyright (c) 2023-2023 Marcelo Silva Portilho
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.runestone.dynafilter.core.generator.annotation;

import com.runestone.dynafilter.core.exceptions.DynamicFilterConfigurationException;
import com.runestone.dynafilter.core.generator.ConditionalStatement;
import com.runestone.dynafilter.core.model.FilterRequestData;
import com.runestone.dynafilter.core.model.statement.LogicOperator;
import com.runestone.dynafilter.core.resolver.FilterDecorator;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.util.*;

public class TypeAnnotationUtils {

    private static final int DEFAULT_CACHE_MAX_SIZE = 4096;
    private static final String CACHE_MAX_SIZE_PROPERTY = "runestone.dynafilter.annotation.cache.max-size";
    private static final int CACHE_MAX_SIZE = resolveCacheMaxSize();
    private static final Map<AnnotationStatementInput, AnnotationMetadata> CACHE_METADATA = Caffeine.newBuilder()
            .maximumSize(CACHE_MAX_SIZE)
            .executor(Runnable::run)
            .<AnnotationStatementInput, AnnotationMetadata>build().asMap();

    private TypeAnnotationUtils() {
    }

    public static List<Class<? extends FilterDecorator<?>>> findFilterDecorators(AnnotationStatementInput annotationStatementInput) {
        return findCachedMetadata(annotationStatementInput).decorators();
    }

    public static List<Class<? extends FilterDecorator<?>>> findFilterDecoratorsInternal(AnnotationStatementInput annotationStatementInput) {
        List<Annotation> statementAnnotations = TypeAnnotationUtils.findStatementAnnotations(annotationStatementInput);
        return findFilterDecoratorsInternal(annotationStatementInput, statementAnnotations);
    }

    private static List<Class<? extends FilterDecorator<?>>> findFilterDecoratorsInternal(AnnotationStatementInput annotationStatementInput,
                                                                                           List<Annotation> statementAnnotations) {
        List<Class<? extends FilterDecorator<?>>> decorators = new ArrayList<>();

        if (annotationStatementInput.type() != null) {
            FilterDecorators decors = annotationStatementInput.type().getAnnotation(FilterDecorators.class);
            if (decors != null) {
                decorators.addAll(Arrays.asList(decors.value()));
            }
        }

        if (annotationStatementInput.annotations() != null) {
            for (Annotation annotation : annotationStatementInput.annotations()) {
                if (annotation.annotationType().equals(FilterDecorators.class)) {
                    FilterDecorators filterDecorators = (FilterDecorators) annotation;
                    decorators.addAll(Arrays.asList(filterDecorators.value()));
                }
            }
        }

        for (Annotation annotation : statementAnnotations) {
            if (annotation.annotationType() == ConjunctionFrom.class) {
                ConjunctionFrom ann = (ConjunctionFrom) annotation;
                FilterDecorators filterDecorators = ann.value().getAnnotation(FilterDecorators.class);
                if (filterDecorators != null) {
                    decorators.addAll(Arrays.asList(filterDecorators.value()));
                }
            } else if (annotation.annotationType() == DisjunctionFrom.class) {
                DisjunctionFrom ann = (DisjunctionFrom) annotation;
                FilterDecorators filterDecorators = ann.value().getAnnotation(FilterDecorators.class);
                if (filterDecorators != null) {
                    decorators.addAll(Arrays.asList(filterDecorators.value()));
                }
            }
        }

        return decorators.isEmpty() ? Collections.emptyList() : decorators;
    }

    /**
     * List all filter request data from the annotation statement input
     *
     * @param annotationStatementInput the annotation statement input
     * @return a list of filter request data
     */
    public static List<FilterRequestData> listAllFilterRequestData(AnnotationStatementInput annotationStatementInput) {
        return findCachedMetadata(annotationStatementInput).requestFilters();
    }

    private static List<FilterRequestData> listAllFilterRequestDataInternal(List<FilterAnnotationData> annotationData) {
        List<FilterRequestData> filters = new ArrayList<>(20);
        for (FilterAnnotationData data : annotationData) {
            filters.addAll(data.filters().stream().map(FilterRequestData::of).toList());
            data.filterStatements().forEach(v -> filters.addAll(v.filters().stream().map(FilterRequestData::of).toList()));
        }
        return filters;
    }

    public static List<FilterAnnotationData> findAnnotationData(AnnotationStatementInput annotationStatementInput) {
        return findCachedMetadata(annotationStatementInput).statementData();
    }

    public static void clearCaches() {
        CACHE_METADATA.clear();
    }

    private static AnnotationMetadata findCachedMetadata(AnnotationStatementInput annotationStatementInput) {
        Objects.requireNonNull(annotationStatementInput, "annotationStatementInput is required");
        AnnotationMetadata cachedMetadata = CACHE_METADATA.get(annotationStatementInput);
        if (cachedMetadata != null) {
            return cachedMetadata;
        }

        AnnotationMetadata builtMetadata = buildMetadata(annotationStatementInput);
        return CACHE_METADATA.computeIfAbsent(annotationStatementInput, k -> builtMetadata);
    }

    private static AnnotationMetadata buildMetadata(AnnotationStatementInput annotationStatementInput) {
        List<Annotation> statementAnnotations = TypeAnnotationUtils.findStatementAnnotations(annotationStatementInput);
        List<FilterAnnotationData> annotationData = findAnnotationDataInternal(statementAnnotations);
        validateAnnotationData(annotationData);
        List<Class<? extends FilterDecorator<?>>> decorators = findFilterDecoratorsInternal(annotationStatementInput, statementAnnotations);
        List<FilterRequestData> filterRequestData = listAllFilterRequestDataInternal(annotationData);

        return new AnnotationMetadata(
                annotationData.isEmpty() ? List.of() : List.copyOf(annotationData),
                decorators.isEmpty() ? List.of() : List.copyOf(decorators),
                filterRequestData.isEmpty() ? List.of() : List.copyOf(filterRequestData)
        );
    }

    private static void validateAnnotationData(List<FilterAnnotationData> annotationData) {
        if (annotationData == null || annotationData.isEmpty()) {
            return;
        }
        for (FilterAnnotationData data : annotationData) {
            validateFilters(data.filters());
            for (FilterAnnotationStatement filterStatement : data.filterStatements()) {
                validateFilters(filterStatement.filters());
            }
        }
    }

    private static void validateFilters(List<Filter> filters) {
        if (filters == null || filters.isEmpty()) {
            return;
        }
        for (Filter filter : filters) {
            validateFilter(filter);
        }
    }

    private static void validateFilter(Filter filter) {
        if (filter.parameters().length == 0) {
            throw new IllegalArgumentException("No parameter configured for filter of path " + filter.path());
        }
        if (filter.constantValues().length != 0 && filter.constantValues().length != filter.parameters().length) {
            throw new IllegalArgumentException(String.format("Parameters and constant values have different sizes. Parameters required: '%s'",
                    String.join(", ", Arrays.asList(filter.parameters()))));
        }
        if (filter.defaultValues().length != 0 && filter.defaultValues().length != filter.parameters().length) {
            throw new IllegalArgumentException(String.format("Parameters and default values have different sizes. Parameters required: '%s'",
                    String.join(", ", Arrays.asList(filter.parameters()))));
        }
    }

    private static List<FilterAnnotationData> findAnnotationDataInternal(List<Annotation> statementAnnotations) {
        List<FilterAnnotationData> filterAnnotationData = new ArrayList<>();
        for (Annotation annotation : statementAnnotations) {
            if (annotation.annotationType() == Conjunction.class) {
                Conjunction ann = (Conjunction) annotation;
                List<FilterAnnotationStatement> statements = getFilterAnnotationFromStatements(ann.disjunctions());
                filterAnnotationData.add(new FilterAnnotationData(LogicOperator.CONJUNCTION, Arrays.asList(ann.value()), statements, ann.negate()));
            } else if (annotation.annotationType() == Disjunction.class) {
                Disjunction ann = (Disjunction) annotation;
                List<FilterAnnotationStatement> statements = getFilterAnnotationFromStatements(ann.conjunctions());
                filterAnnotationData.add(new FilterAnnotationData(LogicOperator.DISJUNCTION, Arrays.asList(ann.value()), statements, ann.negate()));
            } else if (annotation.annotationType() == ConjunctionFrom.class) {
                ConjunctionFrom ann = (ConjunctionFrom) annotation;
                List<Filter> filters = getFiltersFromClass(ann.value());
                List<FilterAnnotationStatement> statements = Arrays.stream(ann.disjunctions())
                        .map(stmt -> new FilterAnnotationStatement(getFiltersFromClass(stmt.value()), stmt.negate()))
                        .toList();
                filterAnnotationData.add(new FilterAnnotationData(LogicOperator.CONJUNCTION, filters, statements, ann.negate()));
            } else if (annotation.annotationType() == DisjunctionFrom.class) {
                DisjunctionFrom ann = (DisjunctionFrom) annotation;
                List<Filter> filters = getFiltersFromClass(ann.value());
                List<FilterAnnotationStatement> statements = Arrays.stream(ann.conjunctions())
                        .map(stmt -> new FilterAnnotationStatement(getFiltersFromClass(stmt.value()), stmt.negate()))
                        .toList();
                filterAnnotationData.add(new FilterAnnotationData(LogicOperator.DISJUNCTION, filters, statements, ann.negate()));
            }
        }
        return filterAnnotationData;
    }

    private static List<Filter> getFiltersFromClass(Class<?> clazz) {
        List<Filter> filters = new ArrayList<>();
        for (Field declaredField : clazz.getDeclaredFields()) {
            Filter filter = declaredField.getAnnotation(Filter.class);
            if (filter != null) {
                filters.add(filter);
            }
        }
        return filters;
    }

    private static List<FilterAnnotationStatement> getFilterAnnotationFromStatements(Statement[] ann) {
        List<FilterAnnotationStatement> statements;
        if (ann.length == 0) {
            statements = Collections.emptyList();
        } else {
            statements = new ArrayList<>(ann.length);
            for (Statement statement : ann) {
                statements.add(new FilterAnnotationStatement(Arrays.asList(statement.value()), statement.negate()));
            }
        }
        return statements;
    }

    private static List<Annotation> findStatementAnnotations(AnnotationStatementInput annotationStatementInput) {
        List<Annotation> statementAnnotations = new ArrayList<>();
        for (Class<?> anInterface : extractProcessableInterfaces(annotationStatementInput.type())) {
            statementAnnotations.addAll(extractFilterAnnotations(anInterface));
        }
        statementAnnotations.addAll(TypeAnnotationUtils.extractFilterAnnotations(annotationStatementInput.annotations()));
        return statementAnnotations;
    }


    protected static List<Class<?>> extractProcessableInterfaces(Class<?> clazz) {
        if (clazz == null) {
            return Collections.emptyList();
        }
        List<Class<?>> interfacesFound = new ArrayList<>();
        interfacesFound.add(clazz);
        getAllInterfaces(clazz, interfacesFound);
        return interfacesFound;
    }

    private static void getAllInterfaces(Class<?> clazz, List<Class<?>> interfacesFound) {
        while (clazz != null) {
            Class<?>[] interfaces = clazz.getInterfaces();
            for (Class<?> i : interfaces) {
                if (!i.getPackageName().startsWith("java.")) {
                    interfacesFound.add(i);
                    getAllInterfaces(i, interfacesFound);
                }
            }
            clazz = clazz.getSuperclass();
        }
    }

    protected static List<Annotation> extractFilterAnnotations(Class<?> type) {
        if (type == null) {
            return Collections.emptyList();
        }
        List<Annotation> annotationsFound = new ArrayList<>();
        List<Annotation> annotations = new ArrayList<>(Arrays.asList(type.getAnnotations()));
        annotations.removeIf(a -> a.annotationType().getPackageName().startsWith("java.lang.annotation"));
        if (!annotations.isEmpty()) {
            VirtualAnnotationHolder virtualAnnotationHolder = new VirtualAnnotationHolder(annotations);
            getAllAnnotations(virtualAnnotationHolder, annotationsFound, new HashSet<>());
        }
        return annotationsFound;
    }

    protected static List<Annotation> extractFilterAnnotations(Annotation[] annotations) {
        if (annotations == null || annotations.length == 0) {
            return Collections.emptyList();
        }
        List<Annotation> annotationsFound = new ArrayList<>();
        List<Annotation> annotationList = new ArrayList<>(Arrays.asList(annotations));
        annotationList.removeIf(a -> a.annotationType().getPackageName().startsWith("java.lang.annotation")); // speed up things
        if (!annotationList.isEmpty()) {
            VirtualAnnotationHolder virtualAnnotationHolder = new VirtualAnnotationHolder(annotationList);
            getAllAnnotations(virtualAnnotationHolder, annotationsFound, new HashSet<>());
        }
        return annotationsFound;
    }

    private static boolean getAllAnnotations(Annotation annotation, List<Annotation> annotationsFound, Set<Annotation> seenAnnotations) {
        List<Annotation> annotations;
        if (annotation instanceof VirtualAnnotationHolder virtualAnnotationHolder) {
            annotations = virtualAnnotationHolder.getAnnotations();
        } else {
            annotations = new ArrayList<>(Arrays.asList(annotation.annotationType().getAnnotations()));
        }
        annotations.removeIf(a -> a.annotationType().getPackageName().startsWith("java.lang.annotation")); // speed up things

        for (Annotation currAnnotation : annotations) {
            if (!seenAnnotations.contains(currAnnotation)) {
                seenAnnotations.add(currAnnotation);
                boolean foundStatementAnnotation = getAllAnnotations(currAnnotation, annotationsFound, seenAnnotations);
                if (foundStatementAnnotation) {
                    annotationsFound.add(currAnnotation);
                }
            }
        }
        return annotation.annotationType() == Conjunction.class
                || annotation.annotationType() == ConjunctionFrom.class
                || annotation.annotationType() == Disjunction.class
                || annotation.annotationType() == DisjunctionFrom.class;
    }

    /**
     * Find a field in a class using dot notation to navigate through classes.
     *
     * @param clazz     Class to search for the field
     * @param fieldName Field name to search
     * @return Field object
     * @throws IllegalStateException if the field does not exist in the class
     */
    public static Field findFilterField(Class<?> clazz, String fieldName) {
        final String[] fieldNames = fieldName.split("\\.", -1);
        // if using dot notation to navigate for classes
        if (fieldNames.length > 1) {
            final String firstProperty = fieldNames[0];
            final String otherProperties = StringUtils.join(fieldNames, '.', 1, fieldNames.length);
            final Field firstPropertyType = findFilterField(clazz, firstProperty);

            Class<?> actualClass = null;
            if (!Object.class.equals(firstPropertyType.getType())) {
                if (Collection.class.isAssignableFrom(firstPropertyType.getType())) {
                    actualClass = (Class<?>) ((ParameterizedType) firstPropertyType.getGenericType()).getActualTypeArguments()[0];
                } else {
                    actualClass = firstPropertyType.getType();
                }
            }

            if (actualClass != null) {
                return findFilterField(actualClass, otherProperties);
            }
        }

        try {
            return clazz.getDeclaredField(fieldName);
        } catch (final NoSuchFieldException e) {
            if (!clazz.getSuperclass().equals(Object.class)) {
                return findFilterField(clazz.getSuperclass(), fieldName);
            }
            throw new DynamicFilterConfigurationException(String.format("Field '%s' does not exist in type '%s'", fieldName, clazz.getCanonicalName()));
        }
    }

    /**
     * Searches for the target class of a filter annotation
     *
     * @param parameter the parameter to search for the target class
     * @return the target class of the filter annotation or null if not found
     * @throws DynamicFilterConfigurationException if the target class is not found when required (e.g. when using ConjunctionFrom or DisjunctionFrom annotations or ConditionalStatement type)
     */
    public static Class<?> findFilterTargetClass(Parameter parameter) {
        Class<?> filterClass;
        Class<?> entityClass = null;

        // Find from ConjunctionFrom or DisjunctionFrom annotations

        ConjunctionFrom conjunctionFrom = parameter.getAnnotation(ConjunctionFrom.class);
        if (conjunctionFrom != null) {
            filterClass = conjunctionFrom.value();
        } else {
            DisjunctionFrom disjunctionFrom = parameter.getAnnotation(DisjunctionFrom.class);
            filterClass = disjunctionFrom != null ? disjunctionFrom.value() : null;
        }
        if (filterClass != null) {
            FilterTarget filterTargetAnnotation = filterClass.getAnnotation(FilterTarget.class);
            if (filterTargetAnnotation == null) {
                throw new DynamicFilterConfigurationException("FilterTarget annotation is required for adding filter configuration from an external class");
            }
            entityClass = filterTargetAnnotation.value();
        }

        // if not found, search on Conjunction or Disjunction configuration

        if (entityClass == null && (parameter.isAnnotationPresent(Conjunction.class) || parameter.isAnnotationPresent(Disjunction.class))) {
            if (parameter.getType().isAssignableFrom(Specification.class)) {
                entityClass = (Class<?>) ((ParameterizedType) parameter.getParameterizedType()).getActualTypeArguments()[0];
            } else if (parameter.getType().equals(ConditionalStatement.class)) {
                FilterTarget filterTargetAnnotation = parameter.getAnnotation(FilterTarget.class);
                if (filterTargetAnnotation == null) {
                    throw new DynamicFilterConfigurationException("FilterTarget annotation is required for ConditionalStatement type parameters");
                }
                entityClass = filterTargetAnnotation.value();
            }
        }

        return entityClass;
    }

    private record AnnotationMetadata(
            List<FilterAnnotationData> statementData,
            List<Class<? extends FilterDecorator<?>>> decorators,
            List<FilterRequestData> requestFilters
    ) {
    }

    static int cacheSize() {
        return CACHE_METADATA.size();
    }

    static int cacheMaxSize() {
        return CACHE_MAX_SIZE;
    }

    private static int resolveCacheMaxSize() {
        String configuredValue = System.getProperty(CACHE_MAX_SIZE_PROPERTY);
        if (configuredValue == null || configuredValue.isBlank()) {
            return DEFAULT_CACHE_MAX_SIZE;
        }
        try {
            int parsedValue = Integer.parseInt(configuredValue.trim());
            return parsedValue > 0 ? parsedValue : DEFAULT_CACHE_MAX_SIZE;
        } catch (NumberFormatException ignored) {
            return DEFAULT_CACHE_MAX_SIZE;
        }
    }

}
