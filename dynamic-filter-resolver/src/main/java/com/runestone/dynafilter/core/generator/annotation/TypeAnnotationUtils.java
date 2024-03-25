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

import com.runestone.dynafilter.core.model.statement.LogicOperator;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.*;

public class TypeAnnotationUtils {

    private static final Map<AnnotationStatementInput, List<FilterAnnotationData>> cacheAnnData = new WeakHashMap<>();

    private TypeAnnotationUtils() {
    }

    public static List<Filter> retrieveFilterAnnotations(AnnotationStatementInput annotationStatementInput) {
        List<Filter> filters = new ArrayList<>(20);
        for (FilterAnnotationData data : TypeAnnotationUtils.findAnnotationData(annotationStatementInput)) {
            filters.addAll(data.filters());
            data.filterStatements().forEach(v -> filters.addAll(v.filters()));
        }
        return filters;
    }

    public static List<FilterAnnotationData> findAnnotationData(AnnotationStatementInput annotationStatementInput) {
        return cacheAnnData.computeIfAbsent(annotationStatementInput, TypeAnnotationUtils::findAnnotationDataInternal);
    }

    private static List<FilterAnnotationData> findAnnotationDataInternal(AnnotationStatementInput annotationStatementInput) {
        List<FilterAnnotationData> filterAnnotationData = new ArrayList<>();
        for (Annotation annotation : TypeAnnotationUtils.findStatementAnnotations(annotationStatementInput)) {
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

}
