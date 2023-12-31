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

import java.lang.annotation.Annotation;
import java.util.*;

public class TypeAnnotationUtils {

    private static final Map<AnnotationStatementInput, List<Annotation>> cache = new WeakHashMap<>();

    private TypeAnnotationUtils() {
    }

    /**
     *
     */
    public static List<Annotation> findStatementAnnotations(AnnotationStatementInput annotationStatementInput) {
        return cache.computeIfAbsent(annotationStatementInput, TypeAnnotationUtils::findStatementAnnotationsInternal);
    }

    public static List<Filter> retrieveFilterAnnotations(AnnotationStatementInput annotationStatementInput) {
        List<Filter> filters = new ArrayList<>(20);
        for (Annotation annotation : TypeAnnotationUtils.findStatementAnnotations(annotationStatementInput)) {
            filters.addAll(TypeAnnotationUtils.flattenFilterAnnotations(annotation));
        }
        return filters;
    }

    /**
     * Creates a list of parameter's filters from the annotation
     */
    private static List<Filter> flattenFilterAnnotations(Annotation annotation) {
        List<Filter> specsList = new ArrayList<>();
        if (annotation instanceof Conjunction conjunction) {
            specsList.addAll(Arrays.asList(conjunction.value()));
            for (Statement v : conjunction.disjunctions()) {
                specsList.addAll(Arrays.asList(v.value()));
            }
        } else if (annotation instanceof Disjunction disjunction) {
            specsList.addAll(Arrays.asList(disjunction.value()));
            for (Statement v : disjunction.conjunctions()) {
                specsList.addAll(Arrays.asList(v.value()));
            }
        } else if (annotation instanceof Statement statement) {
            specsList.addAll(Arrays.asList(statement.value()));
        }
        return specsList;
    }

    /**
     *
     */
    private static List<Annotation> findStatementAnnotationsInternal(AnnotationStatementInput annotationStatementInput) {
        List<Annotation> statementAnnotations = new ArrayList<>();
        for (Class<?> anInterface : extractProcessableInterfaces(annotationStatementInput.type())) {
            statementAnnotations.addAll(extractFilterAnnotations(anInterface));
        }
        statementAnnotations.addAll(TypeAnnotationUtils.extractFilterAnnotations(annotationStatementInput.annotations()));
        return statementAnnotations;
    }


    /**
     *
     */
    protected static List<Class<?>> extractProcessableInterfaces(Class<?> clazz) {
        if (clazz == null) {
            return Collections.emptyList();
        }
        List<Class<?>> interfacesFound = new ArrayList<>();
        interfacesFound.add(clazz);
        getAllInterfaces(clazz, interfacesFound);
        return interfacesFound;
    }

    /**
     *
     */
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

    /**
     *
     */
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

    /**
     *
     */
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

    /**
     *
     */
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
        return annotation.annotationType() == Conjunction.class || annotation.annotationType() == Disjunction.class;
    }

}
