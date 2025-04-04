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

package com.runestone.dynafilter.modules.openapi;

import com.fasterxml.jackson.annotation.JsonView;
import com.runestone.dynafilter.core.generator.annotation.*;
import com.runestone.dynafilter.core.model.FilterRequestData;
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
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.core.MethodParameter;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.web.method.HandlerMethod;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

public class DynaFilterOperationCustomizer implements OperationCustomizer {

    private final ParameterNameDiscoverer parameterNameDiscoverer;

    public DynaFilterOperationCustomizer(ParameterNameDiscoverer parameterNameDiscoverer) {
        this.parameterNameDiscoverer = parameterNameDiscoverer;
    }

    @Override
    public Operation customize(Operation operation, HandlerMethod handlerMethod) {
        for (MethodParameter methodParameter : handlerMethod.getMethodParameters()) {
            if (!methodParameter.hasParameterAnnotation(Conjunction.class) && !methodParameter.hasParameterAnnotation(ConjunctionFrom.class)
                && !methodParameter.hasParameterAnnotation(Disjunction.class) && !methodParameter.hasParameterAnnotation(Disjunction.class)) {
                continue;
            }

            String parameterName = getParameterName(methodParameter);
            List<FilterRequestData> parameterAnnotations = TypeAnnotationUtils
                    .listAllFilterRequestData(new AnnotationStatementInput(methodParameter.getParameterType(), methodParameter.getParameterAnnotations()));

            if (!parameterAnnotations.isEmpty()) {
                operation.getParameters().removeIf(p -> p.getName().equals(parameterName));
                for (FilterRequestData spec : parameterAnnotations) {
                    try {
                        customizeParameter(operation, methodParameter, spec);
                    } catch (Exception e) {
                        throw new IllegalStateException("Cannot build custom OpenAPI parameters from dynamic filter parameter on method: %s"
                                .formatted(handlerMethod.getMethod().toString()), e);
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
    private void customizeParameter(Operation operation, MethodParameter methodParameter, FilterRequestData filter) {
        if (filter.constantValues() != null && filter.constantValues().length > 0) {
            return;
        }

        if (Dynamic.class.equals(filter.operation()) && filter.parameters().length > 1) {
            throw new IllegalStateException("Dynamic filter operation cannot have two parameters");
        }

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
                arraySchema.items(parameter.getSchema() != null ? parameter.getSchema() : new StringSchema());
                parameter.setSchema(arraySchema);
            } else {
                Class<?> filterTargetClass = TypeAnnotationUtils.findFilterTargetClass(methodParameter.getParameter());
                Field field = TypeAnnotationUtils.findFilterField(filterTargetClass, filter.path());
                createCommonSchema(filter, field, methodParameter, parameter);
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
    private void createCommonSchema(FilterRequestData filter, Field field, MethodParameter methodParameter, io.swagger.v3.oas.models.parameters.Parameter parameter) {
        Schema schemaFromType;
        if (field != null) {
            Class<?> fieldClass = field.getType();
            schemaFromType = AnnotationsUtils.resolveSchemaFromType(fieldClass, null, getJsonViewFromMethod(methodParameter));
        } else {
            schemaFromType = new StringSchema();
        }

        Schema currentSchema = parameter.getSchema();
        Schema newSchema;
        if (IsNull.class.equals(filter.operation())) {
            newSchema = new BooleanSchema();
        } else if (currentSchema != null) {
            newSchema = new Schema();
            newSchema.setType(schemaFromType.getType());
            newSchema.setEnum(schemaFromType.getEnum());
        } else {
            newSchema = schemaFromType;
        }

        parameter.setSchema(newSchema);
        parameter.setDescription(filter.description());

        if (filter.defaultValues() != null && filter.defaultValues().length == 1) {
            newSchema.setDefault(filter.defaultValues()[0]);
        }
        SchemaValidationUtils.applyValidations(newSchema, field);
    }

    /**
     * Extracts a {@link JsonView} configuration from a {@link MethodParameter} for additional customization
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

}
