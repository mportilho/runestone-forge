package com.runestone.dynafilter.modules.openapi;

import com.fasterxml.jackson.annotation.JsonView;
import com.runestone.assertions.Asserts;
import com.runestone.dynafilter.core.generator.annotation.AnnotationStatementInput;
import com.runestone.dynafilter.core.generator.annotation.Filter;
import com.runestone.dynafilter.core.generator.annotation.TypeAnnotationUtils;
import com.runestone.dynafilter.core.operation.types.Decorated;
import com.runestone.dynafilter.core.operation.types.Dynamic;
import com.runestone.dynafilter.core.operation.types.IsIn;
import com.runestone.dynafilter.core.operation.types.IsNull;
import io.swagger.v3.core.util.AnnotationsUtils;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.BooleanSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import org.apache.commons.lang3.StringUtils;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.core.MethodParameter;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.web.method.HandlerMethod;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

public class DynaFilterOperationCustomizer implements OperationCustomizer {

    private final ParameterNameDiscoverer parameterNameDiscoverer;

    public DynaFilterOperationCustomizer() {
        this(null);
    }

    public DynaFilterOperationCustomizer(ParameterNameDiscoverer parameterNameDiscoverer) {
        this.parameterNameDiscoverer = parameterNameDiscoverer;
    }

    @Override
    public Operation customize(Operation operation, HandlerMethod handlerMethod) {
        for (MethodParameter methodParameter : handlerMethod.getMethodParameters()) {
            String parameterName = getParameterName(methodParameter);

            List<Filter> parameterAnnotations = TypeAnnotationUtils
                    .retrieveFilterAnnotations(new AnnotationStatementInput(methodParameter.getParameterType(), methodParameter.getParameterAnnotations()));
            parameterAnnotations.removeIf(filter -> !Decorated.class.equals(filter.operation()));

            if (!parameterAnnotations.isEmpty()) {
                operation.getParameters().removeIf(p -> p.getName().equals(parameterName));
                for (Filter spec : parameterAnnotations) {
                    try {
                        customizeParameter(operation, methodParameter, spec);
                    } catch (Exception e) {
                        throw new IllegalStateException("Cannot build custom OpenAPI parameters from specification-args-resolver", e);
                    }
                }
            }
        }
        return operation;
    }

    /**
     * Applies necessary customization on the OpenAPI 3 {@link Operation}
     * representation
     */
    @SuppressWarnings({"rawtypes"})
    private void customizeParameter(
            Operation operation, MethodParameter methodParameter, Filter filter) throws Exception {
        ParameterizedType parameterizedType = (ParameterizedType) methodParameter.getParameter().getParameterizedType();
        Class<?> parameterizedClassType = Class.forName(parameterizedType.getActualTypeArguments()[0].getTypeName());

        if (Decorated.class.equals(filter.operation())) {
            var parameter = new io.swagger.v3.oas.models.parameters.Parameter();
            parameter.setName(filter.parameters()[0]);
            Schema schema = AnnotationsUtils.resolveSchemaFromType(filter.targetType(), null, null);
            parameter.setSchema(schema);
            parameter.setIn(ParameterIn.QUERY.toString());
            parameter.setDescription(filter.description());
            operation.getParameters().add(parameter);
            return;
        }

        Field field = findParameterField(operation, filter, parameterizedClassType);
        Class<?> fieldClass = field.getType();

        if (Dynamic.class.equals(filter.operation()) && filter.parameters().length > 1) {
            throw new IllegalStateException("Dynamic filter operation cannot have two parameters");
        }

        Schema schemaFromType = AnnotationsUtils.resolveSchemaFromType(fieldClass, null, getJsonViewFromMethod(methodParameter));
        for (String parameterName : filter.parameters()) {
            Optional<io.swagger.v3.oas.models.parameters.Parameter> optParameter = operation.getParameters().stream()
                    .filter(p -> p.getName().equals(parameterName)).findFirst();

            boolean parameterExists = optParameter.isPresent();
            io.swagger.v3.oas.models.parameters.Parameter parameter = optParameter.orElse(new io.swagger.v3.oas.models.parameters.Parameter());
            parameter.setName(parameterName);

            if (Dynamic.class.equals(filter.operation())) {
                ArraySchema arraySchema = new ArraySchema();
                arraySchema.type("array");
                arraySchema.minItems(2);
                arraySchema.items(new StringSchema());
                parameter.setSchema(arraySchema);
            } else if (IsIn.class.equals(filter.operation())) {
                ArraySchema arraySchema = new ArraySchema();
                arraySchema.type("array");
                arraySchema.items(Optional.ofNullable(parameter.getSchema()).orElse(new StringSchema()));
                parameter.setSchema(arraySchema);
            } else {
                createCommonSchema(filter, field, schemaFromType, parameter);
            }

            parameter.required(filter.required());
            if (parameterExists) {
                if (parameter.getIn() == null || ParameterIn.DEFAULT.toString().equals(parameter.getIn())) {
                    parameter.setIn(ParameterIn.QUERY.toString());
                } else if (ParameterIn.PATH.toString().equals(parameter.getIn())) {
                    parameter.required(true);
                }
            } else {
                parameter.setIn(ParameterIn.QUERY.toString());
                operation.getParameters().add(parameter);
            }
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void createCommonSchema(Filter filter, Field field, Schema schemaFromType,
                                    io.swagger.v3.oas.models.parameters.Parameter parameter) {
        Optional<Schema> optSchema = Optional.ofNullable(parameter.getSchema());
        boolean schemaExists = optSchema.isPresent();
        Schema schema = optSchema.orElse(new Schema<>());

        if (IsNull.class.equals(filter.operation())) {
            schema = new BooleanSchema();
        } else if (schemaExists) {
            schema.setType(schemaFromType.getType());
            schema.setEnum(schemaFromType.getEnum());
        } else {
            schema = schemaFromType;
        }
        parameter.setSchema(schema);
        parameter.setDescription(filter.description());

        if (filter.defaultValues() != null && filter.defaultValues().length == 1) {
            schema.setDefault(filter.defaultValues()[0]);
        }
        SchemaValidationUtils.applyValidations(schema, field);
    }

    private static Field findParameterField(Operation operation, Filter filter, Class<?> parameterizedClassType) {
        Field field;
        try {
            field = findFilterField(parameterizedClassType, filter.path());
        } catch (IllegalStateException e) {
            String location = Asserts.isNotEmpty(operation.getTags()) ? operation.getTags().get(0) + "." : "";
            location += operation.getOperationId();
            throw new IllegalStateException(String.format("Fail to get Schema data from Operation '%s'", location), e);
        }
        return field;
    }

    /**
     * Extracts a {@link JsonView} configuration from a {@link MethodParameter} for
     * additional customization
     */
    private static JsonView getJsonViewFromMethod(MethodParameter methodParameter) {
        JsonView[] jsonViews = requireNonNull(methodParameter.getMethod()).getAnnotationsByType(JsonView.class);
        JsonView jsonView = null;
        if (jsonViews != null && jsonViews.length > 0) {
            jsonView = jsonViews[0];
        }
        return jsonView;
    }

    /**
     * Defines the request parameter's name
     */
    private String getParameterName(MethodParameter methodParameter) {
        io.swagger.v3.oas.annotations.Parameter parameterAnnotation = methodParameter.getParameter().getAnnotation(io.swagger.v3.oas.annotations.Parameter.class);
        if (parameterAnnotation != null) {
            return parameterAnnotation.name();
        } else if (parameterNameDiscoverer != null) {
            methodParameter.initParameterNameDiscovery(parameterNameDiscoverer);
            return methodParameter.getParameterName();
        }
        return methodParameter.getParameter().getName();
    }

    /**
     * Gets the {@link Field}'s representation on the specific type
     */
    private static Field findFilterField(Class<?> clazz, String fieldName) {
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
            throw new IllegalStateException(String.format("Field '%s' does not exist in type '%s'", fieldName, clazz.getCanonicalName()));
        }
    }

}
