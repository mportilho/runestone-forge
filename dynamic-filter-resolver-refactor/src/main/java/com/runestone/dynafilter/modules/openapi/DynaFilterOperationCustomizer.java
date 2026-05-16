package com.runestone.dynafilter.modules.openapi;

import com.fasterxml.jackson.annotation.JsonView;
import com.runestone.dynafilter.core.annotation.Conjunction;
import com.runestone.dynafilter.core.annotation.ConjunctionFrom;
import com.runestone.dynafilter.core.annotation.Disjunction;
import com.runestone.dynafilter.core.annotation.DisjunctionFrom;
import com.runestone.dynafilter.core.exception.DynamicFilterConfigurationException;
import com.runestone.dynafilter.core.generator.annotation.AnnotationStatementInput;
import com.runestone.dynafilter.core.generator.annotation.TypeAnnotationUtils;
import com.runestone.dynafilter.core.model.FilterRequestData;
import com.runestone.dynafilter.core.operation.Dynamic;
import com.runestone.dynafilter.core.operation.IsIn;
import com.runestone.dynafilter.core.operation.IsNull;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.BooleanSchema;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.NumberSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.core.MethodParameter;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.core.StandardReflectionParameterNameDiscoverer;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.web.method.HandlerMethod;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class DynaFilterOperationCustomizer implements OperationCustomizer {

    private final ParameterNameDiscoverer parameterNameDiscoverer;

    public DynaFilterOperationCustomizer() {
        this(new StandardReflectionParameterNameDiscoverer());
    }

    public DynaFilterOperationCustomizer(ParameterNameDiscoverer parameterNameDiscoverer) {
        this.parameterNameDiscoverer = Objects.requireNonNull(parameterNameDiscoverer, "parameterNameDiscoverer must not be null");
    }

    @Override
    public Operation customize(Operation operation, HandlerMethod handlerMethod) {
        Objects.requireNonNull(operation, "operation must not be null");
        Objects.requireNonNull(handlerMethod, "handlerMethod must not be null");
        for (MethodParameter methodParameter : handlerMethod.getMethodParameters()) {
            if (!hasDynamicFilterAnnotation(methodParameter)) {
                continue;
            }
            AnnotationStatementInput input = new AnnotationStatementInput(
                    methodParameter.getParameterType(),
                    methodParameter.getParameterAnnotations()
            );
            String technicalParameterName = technicalParameterName(methodParameter);
            removeParameter(operation, technicalParameterName);
            for (FilterRequestData filter : TypeAnnotationUtils.listAllFilterRequestData(input)) {
                customizeParameter(operation, methodParameter, input, filter);
            }
        }
        return operation;
    }

    void customizeParameter(
            Operation operation,
            MethodParameter methodParameter,
            AnnotationStatementInput input,
            FilterRequestData filter
    ) {
        if (filter.hasConstantValues()) {
            return;
        }
        for (String requestParameter : filter.parameters()) {
            Parameter parameter = findParameter(operation, requestParameter).orElseGet(() -> new Parameter().name(requestParameter).in("query"));
            Field field = findField(input, filter.path()).orElse(null);
            parameter.description(filter.description());
            if ("path".equals(parameter.getIn())) {
                parameter.required(true);
            } else {
                parameter.required(filter.required());
            }
            createCommonSchema(filter, field, methodParameter, parameter);
            if (findParameter(operation, requestParameter).isEmpty()) {
                operation.addParametersItem(parameter);
            }
        }
    }

    void createCommonSchema(FilterRequestData filter, Field field, MethodParameter methodParameter, Parameter parameter) {
        Schema<?> schema = schemaFor(filter, field, methodParameter, parameter);
        if (filter.defaultValues().length == 1) {
            schema.setDefault(String.valueOf(filter.defaultValues()[0]));
        }
        if (field != null) {
            SchemaValidationUtils.applyValidations(schema, field);
        }
        parameter.schema(schema);
    }

    private Schema<?> schemaFor(FilterRequestData filter, Field field, MethodParameter methodParameter, Parameter parameter) {
        if (filter.operation().equals(Dynamic.class)) {
            if (filter.parameters().length != 1) {
                throw new DynamicFilterConfigurationException("Dynamic operation must declare exactly one parameter");
            }
            return new ArraySchema().items(new StringSchema()).minItems(2);
        }
        if (filter.operation().equals(IsNull.class)) {
            return new BooleanSchema();
        }
        Schema<?> fieldSchema = field == null ? new StringSchema() : schemaFromType(field.getType(), methodParameter);
        if (filter.operation().equals(IsIn.class)) {
            Schema<?> itemSchema = parameter.getSchema() instanceof ArraySchema arraySchema && arraySchema.getItems() != null
                    ? arraySchema.getItems()
                    : fieldSchema;
            return new ArraySchema().items(itemSchema);
        }
        return fieldSchema;
    }

    private static Schema<?> schemaFromType(Class<?> type, MethodParameter methodParameter) {
        if (type == boolean.class || type == Boolean.class) {
            return new BooleanSchema();
        }
        if (type == byte.class || type == short.class || type == int.class || type == long.class
                || type == Byte.class || type == Short.class || type == Integer.class || type == Long.class) {
            return new IntegerSchema();
        }
        if (type == float.class || type == double.class || type == Float.class || type == Double.class
                || BigDecimal.class.isAssignableFrom(type)) {
            return new NumberSchema();
        }
        if (type.isArray() || Iterable.class.isAssignableFrom(type)) {
            return new ArraySchema().items(new StringSchema());
        }
        StringSchema schema = new StringSchema();
        if (type.isEnum()) {
            List<String> values = new ArrayList<>();
            for (Object constant : type.getEnumConstants()) {
                values.add(String.valueOf(constant));
            }
            schema.setEnum(values);
        } else if (Temporal.class.isAssignableFrom(type)) {
            schema.format("date-time");
        }
        JsonView jsonView = methodParameter.getMethodAnnotation(JsonView.class);
        if (jsonView != null) {
            schema.addExtension("x-json-view", List.of(jsonView.value()).toString());
        }
        return schema;
    }

    String technicalParameterName(MethodParameter methodParameter) {
        io.swagger.v3.oas.annotations.Parameter parameter = methodParameter.getParameterAnnotation(io.swagger.v3.oas.annotations.Parameter.class);
        if (parameter != null && !parameter.name().isBlank()) {
            return parameter.name();
        }
        String[] names = parameterNameDiscoverer.getParameterNames(methodParameter.getMethod());
        if (names != null && methodParameter.getParameterIndex() >= 0 && methodParameter.getParameterIndex() < names.length) {
            return names[methodParameter.getParameterIndex()];
        }
        String reflectionName = methodParameter.getParameter().getName();
        return reflectionName == null || reflectionName.isBlank() ? "arg" + methodParameter.getParameterIndex() : reflectionName;
    }

    private static void removeParameter(Operation operation, String name) {
        if (operation.getParameters() == null) {
            return;
        }
        operation.setParameters(operation.getParameters().stream()
                .filter(parameter -> !name.equals(parameter.getName()))
                .collect(java.util.stream.Collectors.toCollection(ArrayList::new)));
    }

    private static Optional<Parameter> findParameter(Operation operation, String name) {
        if (operation.getParameters() == null) {
            return Optional.empty();
        }
        return operation.getParameters().stream()
                .filter(parameter -> name.equals(parameter.getName()))
                .findFirst();
    }

    private static Optional<Field> findField(AnnotationStatementInput input, String path) {
        Class<?> targetClass = TypeAnnotationUtils.findFilterTargetClass(input);
        if (targetClass == Object.class) {
            return Optional.empty();
        }
        try {
            return Optional.of(TypeAnnotationUtils.findFilterField(targetClass, path));
        } catch (DynamicFilterConfigurationException ignored) {
            return Optional.empty();
        }
    }

    private static boolean hasDynamicFilterAnnotation(MethodParameter methodParameter) {
        for (Annotation annotation : methodParameter.getParameterAnnotations()) {
            Class<? extends Annotation> type = annotation.annotationType();
            if (type.equals(Conjunction.class)
                    || type.equals(Disjunction.class)
                    || type.equals(ConjunctionFrom.class)
                    || type.equals(DisjunctionFrom.class)
                    || AnnotatedElementUtils.hasAnnotation(type, Conjunction.class)
                    || AnnotatedElementUtils.hasAnnotation(type, Disjunction.class)
                    || AnnotatedElementUtils.hasAnnotation(type, ConjunctionFrom.class)
                    || AnnotatedElementUtils.hasAnnotation(type, DisjunctionFrom.class)) {
                return true;
            }
        }
        return false;
    }
}
