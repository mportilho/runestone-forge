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
import com.runestone.dynafilter.core.exceptions.DynamicFilterConfigurationException;
import com.runestone.dynafilter.core.generator.annotation.*;
import com.runestone.dynafilter.core.model.FilterRequestData;
import com.runestone.dynafilter.core.operation.*;
import com.runestone.dynafilter.core.operation.types.Decorated;
import com.runestone.dynafilter.core.operation.types.Dynamic;
import com.runestone.dynafilter.helpers.StringHelper;
import io.swagger.v3.core.util.AnnotationsUtils;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.BooleanSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springdoc.core.customizers.ParameterCustomizer;
import org.springdoc.core.extractor.DelegatingMethodParameter;
import org.springframework.core.MethodParameter;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.web.method.HandlerMethod;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.data.web.SortHandlerMethodArgumentResolver;
import org.springframework.web.bind.annotation.RequestParam;
import com.runestone.dynafilter.modules.jpa.api.JpaSortPolicy;
import com.runestone.dynafilter.modules.jpa.spring.JpaSortContext;
import com.runestone.dynafilter.modules.jpa.spring.SortContractAssociation;
import com.runestone.dynafilter.modules.jpa.spring.ResolvedJpaSortContract;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

public class DynaFilterOperationCustomizer implements OperationCustomizer, ParameterCustomizer, BeanFactoryAware {

    private static final String GENERATED_SORT = "x-runestone-generated-sort";

    private final ParameterNameDiscoverer parameterNameDiscoverer;
    private final FilterOperationService<?> filterOperationService;
    private BeanFactory beanFactory;

    @Override
    public void setBeanFactory(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    public DynaFilterOperationCustomizer(ParameterNameDiscoverer parameterNameDiscoverer, FilterOperationService<?> filterOperationService) {
        this.parameterNameDiscoverer = parameterNameDiscoverer;
        this.filterOperationService = Objects.requireNonNull(filterOperationService, "filterOperationService cannot be null");
    }

    @Override
    public io.swagger.v3.oas.models.parameters.Parameter customize(io.swagger.v3.oas.models.parameters.Parameter parameter,
                                                                   MethodParameter methodParameter) {
        if (declaredSortIntegration() && methodParameter instanceof DelegatingMethodParameter flattened) {
            Field field = flattened.getField();
            if (field != null && field.getName().equals("sort")
                    && (field.getDeclaringClass() == org.springdoc.core.converters.models.Pageable.class
                        || field.getDeclaringClass() == org.springdoc.core.converters.models.Sort.class)) {
                // Preserve provenance through Springdoc's merge step instead of guessing from a parameter name or description.
                parameter.addExtension(GENERATED_SORT, true);
            }
        }
        return parameter;
    }

    @Override
    public Operation customize(Operation operation, HandlerMethod handlerMethod) {
        if (operation.getParameters() == null) {
            operation.setParameters(new ArrayList<>());
        }
        var associations = declaredSortIntegration() ? SortContractAssociation.forMethod(handlerMethod.getMethod()) : List.<SortContractAssociation>of();
        Map<Parameter, ResolvedJpaSortContract> sortContexts = new HashMap<>();
        for (var association : associations) {
            var parameter = association.contract().getParameter();
            sortContexts.put(parameter, beanFactory.getBean(JpaSortContext.class).resolve(parameter));
        }
        for (MethodParameter methodParameter : handlerMethod.getMethodParameters()) {
            if (!methodParameter.hasParameterAnnotation(Conjunction.class) && !methodParameter.hasParameterAnnotation(ConjunctionFrom.class)
                && !methodParameter.hasParameterAnnotation(Disjunction.class) && !methodParameter.hasParameterAnnotation(DisjunctionFrom.class)) {
                continue;
            }

            String parameterName = getParameterName(methodParameter);
            List<FilterRequestData> parameterAnnotations = TypeAnnotationUtils
                    .listAllFilterRequestData(new AnnotationStatementInput(methodParameter.getParameterType(), methodParameter.getParameterAnnotations()));

            if (!parameterAnnotations.isEmpty()) {
                operation.getParameters().removeIf(p -> p.getName().equals(parameterName));
                for (FilterRequestData spec : parameterAnnotations) {
                    if (spec.hidden()) {
                        continue;
                    }
                    try {
                        customizeParameter(operation, methodParameter, spec, sortContexts.get(methodParameter.getParameter()));
                    } catch (Exception e) {
                        throw new IllegalStateException("Cannot build custom OpenAPI parameters from dynamic filter parameter on method: %s"
                                .formatted(handlerMethod.getMethod().toString()), e);
                    }
                }
            }
        }
        customizeSort(operation, handlerMethod, associations, sortContexts);
        return operation;
    }

    private void customizeSort(Operation operation, HandlerMethod handler, List<SortContractAssociation> associations,
                               Map<Parameter, ResolvedJpaSortContract> sortContexts) {
        if (!declaredSortIntegration()) {
            return;
        }
        var names = new HashSet<String>();
        for (var association : associations) {
            var resolver = SpringDataSortDocumentation.resolver(beanFactory, association.ordering());
            String name = SpringDataSortDocumentation.parameterName(resolver, association.ordering());
            if (!names.add(name)) {
                throw new DynamicFilterConfigurationException("Sort arguments resolve to the same request parameter at " + handler.getMethod());
            }
            customizeSortParameter(operation, sortContexts.get(association.contract().getParameter()), resolver, name);
        }
        operation.getParameters().removeIf(parameter -> {
            boolean generated = parameter.getExtensions() != null && parameter.getExtensions().remove(GENERATED_SORT) != null;
            return generated && !associations.isEmpty() && !names.contains(parameter.getName())
                    && !explicitQueryParameter(handler, parameter.getName());
        });
    }

    private boolean declaredSortIntegration() {
        return beanFactory != null && beanFactory.getBeanProvider(JpaSortPolicy.class)
                .getIfAvailable(() -> JpaSortPolicy.DECLARED) == JpaSortPolicy.DECLARED;
    }

    private static boolean explicitQueryParameter(HandlerMethod handler, String name) {
        for (MethodParameter argument : handler.getMethodParameters()) {
            RequestParam request = argument.getParameterAnnotation(RequestParam.class);
            if (request != null) {
                String requestName = !request.name().isEmpty() ? request.name()
                        : !request.value().isEmpty() ? request.value() : argument.getParameter().getName();
                if (name.equals(requestName)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void customizeSortParameter(Operation operation, ResolvedJpaSortContract context,
                                        SortHandlerMethodArgumentResolver resolver, String name) {
        var catalog = context.catalog();
        var parameter = operation.getParameters().stream()
                .filter(value -> name.equals(value.getName()) && (value.getIn() == null || "query".equals(value.getIn()))).findFirst()
                .orElseGet(() -> {
                    var value = new io.swagger.v3.oas.models.parameters.Parameter().name(name).in("query");
                    operation.addParametersItem(value);
                    return value;
                });
        List<String> names = catalog.keySet().stream().sorted().toList();
        String delimiter = resolver.getPropertyDelimiter();
        parameter.setDescription("Repeat this parameter to preserve sort precedence. Use property" + delimiter
                + "asc or property" + delimiter + "desc; multiple properties may precede the direction, separated by '"
                + delimiter + "'. Direction is optional and defaults to asc; optional " + delimiter + "ignorecase. Available names: "
                + String.join(", ", names) + ".");
        parameter.setSchema(new ArraySchema().items(new StringSchema()));
        parameter.setIn("query");
        parameter.setStyle(io.swagger.v3.oas.models.parameters.Parameter.StyleEnum.FORM);
        parameter.setExplode(true);
        parameter.addExtension("x-sort-names", names);
        parameter.setExample(names.isEmpty() ? null : List.of(names.getFirst() + delimiter + "asc"));
    }

    /**
     * Applies necessary customization on the OpenAPI 3 {@link Operation}
     * representation
     */
    private void customizeParameter(Operation operation, MethodParameter methodParameter, FilterRequestData filter,
                                    ResolvedJpaSortContract sortContext) {
        if (filter.hidden()) {
            return;
        }
        if (filter.constantValues() != null && filter.constantValues().length > 0) {
            return;
        }

        if (Dynamic.class.equals(filter.operation()) && filter.parameters().length > 1) {
            throw new IllegalStateException("Dynamic filter operation cannot have two parameters");
        }

        FilterOperationMetadata metadata = filterOperationService.findMetadata(filter.operation());

        for (String parameterName : filter.parameters()) {
            Optional<io.swagger.v3.oas.models.parameters.Parameter> optParameter = operation.getParameters().stream()
                    .filter(p -> p.getName().equals(parameterName)).findFirst();

            boolean parameterExists = optParameter.isPresent();
            io.swagger.v3.oas.models.parameters.Parameter parameter = optParameter.orElse(new io.swagger.v3.oas.models.parameters.Parameter());
            parameter.setName(parameterName);
            if (parameter.getExtensions() != null) {
                // A reused parameter now describes a public filter, not generated-only ordering metadata.
                parameter.getExtensions().remove(GENERATED_SORT);
            }

            if (FilterValueShape.DYNAMIC.equals(metadata.valueShape())) {
                parameter.setSchema(createArraySchema(new StringSchema(), 2, null));
            } else {
                SchemaAttribute attribute = findFilterAttribute(methodParameter, filter, sortContext);
                createSchema(filter, attribute, methodParameter, parameter, metadata);
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

    private SchemaAttribute findFilterAttribute(MethodParameter methodParameter, FilterRequestData filter,
                                               ResolvedJpaSortContract sortContext) {
        try {
            if (sortContext != null) {
                var attribute = sortContext.findAttribute(filter.path()[0]);
                if (attribute != null) {
                    return new SchemaAttribute(attribute.getJavaType(),
                            attribute.getJavaMember() instanceof AnnotatedElement member ? member : null);
                }
            }
            // Nonpersistent filters keep their independent schema rules, never sort publication.
            Class<?> filterTargetClass = sortContext != null ? sortContext.entity().getJavaType()
                    : TypeAnnotationUtils.findFilterTargetClass(methodParameter.getParameter());
            if (filterTargetClass == null) {
                if (isCustomOperation(filter.operation())) {
                    return null;
                }
                throw new DynamicFilterConfigurationException("Cannot resolve target type for filter path(s) '%s'"
                        .formatted(StringHelper.formatPath(filter.path())));
            }
            var field = TypeAnnotationUtils.findFilterField(filterTargetClass, filter.path()[0]);
            return new SchemaAttribute(field.getType(), field);
        } catch (DynamicFilterConfigurationException e) {
            if (isCustomOperation(filter.operation())) {
                return null;
            }
            throw e;
        }
    }

    private record SchemaAttribute(Class<?> type, AnnotatedElement member) {
    }

    private static boolean isCustomOperation(@SuppressWarnings("rawtypes") Class<? extends FilterOperation> operationType) {
        if (Decorated.class.equals(operationType) || Dynamic.class.equals(operationType)) {
            return false;
        }
        for (ComparisonOperation comparisonOperation : ComparisonOperation.values()) {
            if (comparisonOperation.getOperation().equals(operationType)) {
                return false;
            }
        }
        return true;
    }

    @SuppressWarnings({"rawtypes"})
    private void createSchema(FilterRequestData filter, SchemaAttribute attribute, MethodParameter methodParameter,
                              io.swagger.v3.oas.models.parameters.Parameter parameter, FilterOperationMetadata metadata) {
        Schema schemaFromType = resolveSchemaFromAttribute(attribute, methodParameter);

        Schema newSchema = switch (metadata.valueShape()) {
            case BOOLEAN -> new BooleanSchema();
            case ARRAY ->
                    createArraySchema(parameter.getSchema() != null ? parameter.getSchema() : new StringSchema(), null, null);
            case STRING -> new StringSchema();
            case TARGET_FIELD -> createTargetFieldSchema(parameter.getSchema(), schemaFromType);
            case DYNAMIC ->
                    throw new IllegalStateException("Unsupported schema shape in common schema creation: " + metadata.valueShape());
        };

        parameter.setSchema(newSchema);
        parameter.setDescription(filter.description());

        if (filter.defaultValues() != null && filter.defaultValues().length == 1) {
            newSchema.setDefault(filter.defaultValues()[0]);
        }
        if (!metadata.valueShape().equals(FilterValueShape.ARRAY)) {
            SchemaValidationUtils.applyValidations(newSchema, attribute != null ? attribute.member() : null);
        }
    }

    @SuppressWarnings({"rawtypes"})
    private Schema resolveSchemaFromAttribute(SchemaAttribute attribute, MethodParameter methodParameter) {
        if (attribute == null) {
            return new StringSchema();
        }
        return AnnotationsUtils.resolveSchemaFromType(attribute.type(), null, getJsonViewFromMethod(methodParameter));
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static Schema createTargetFieldSchema(Schema currentSchema, Schema schemaFromType) {
        if (currentSchema == null) {
            return schemaFromType;
        }
        Schema newSchema = new Schema();
        newSchema.setType(schemaFromType.getType());
        newSchema.setEnum(schemaFromType.getEnum());
        return newSchema;
    }

    @SuppressWarnings({"rawtypes"})
    private static ArraySchema createArraySchema(Schema itemSchema, Integer minItems, Integer maxItems) {
        ArraySchema arraySchema = new ArraySchema();
        arraySchema.type("array");
        arraySchema.items(itemSchema);
        if (minItems != null) {
            arraySchema.minItems(minItems);
        }
        if (maxItems != null) {
            arraySchema.maxItems(maxItems);
        }
        return arraySchema;
    }

    /**
     * Extracts a {@link JsonView} configuration from a {@link MethodParameter} for additional customization
     */
    private static JsonView getJsonViewFromMethod(MethodParameter methodParameter) {
        JsonView[] jsonViews = requireNonNull(methodParameter.getMethod()).getAnnotationsByType(JsonView.class);
        JsonView jsonView = null;
        if (jsonViews.length > 0) {
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
