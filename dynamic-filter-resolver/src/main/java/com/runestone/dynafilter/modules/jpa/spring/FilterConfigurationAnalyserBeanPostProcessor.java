package com.runestone.dynafilter.modules.jpa.spring;

import com.runestone.dynafilter.core.exceptions.DynamicFilterConfigurationException;
import com.runestone.dynafilter.core.generator.ConditionalStatement;
import com.runestone.dynafilter.core.generator.annotation.AnnotationStatementInput;
import com.runestone.dynafilter.core.generator.annotation.TypeAnnotationUtils;
import com.runestone.dynafilter.core.model.FilterRequestData;
import com.runestone.dynafilter.core.model.modifiers.ModIgnorePath;
import com.runestone.dynafilter.core.operation.FilterArity;
import com.runestone.dynafilter.core.operation.FilterOperationMetadata;
import com.runestone.dynafilter.core.operation.FilterOperationService;
import com.runestone.dynafilter.core.operation.types.Decorated;
import com.runestone.dynafilter.core.operation.types.Dynamic;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.runestone.dynafilter.core.generator.annotation.TypeAnnotationUtils.findFilterField;
import static com.runestone.dynafilter.core.generator.annotation.TypeAnnotationUtils.listAllFilterRequestData;
import static com.runestone.dynafilter.helpers.StringHelper.formatPath;

public class FilterConfigurationAnalyserBeanPostProcessor implements BeanPostProcessor {

    private final FilterOperationService<Specification<?>> filterOperationService;

    public FilterConfigurationAnalyserBeanPostProcessor(FilterOperationService<Specification<?>> filterOperationService) {
        this.filterOperationService = filterOperationService;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (AnnotationUtils.findAnnotation(bean.getClass(), RestController.class) != null) {
            for (Method method : bean.getClass().getMethods()) {
                for (Parameter parameter : method.getParameters()) {
                    warmupAndCheckFilterConfiguration(parameter);
                }
            }
        }
        return bean;
    }

    private void warmupAndCheckFilterConfiguration(Parameter parameter) {
        if (!isDynamicFilterParameter(parameter)) {
            return;
        }

        AnnotationStatementInput input = new AnnotationStatementInput(parameter.getType(), parameter.getAnnotations());
        List<FilterRequestData> allFilters = listAllFilterRequestData(input);
        validateUniqueFilterParameters(allFilters, parameter);

        Class<?> entityClass = TypeAnnotationUtils.findFilterTargetClass(parameter);
        if (entityClass != null) {
            allFilters.forEach(filter -> {
                if (filter.modifiers() == null || filter.modifiers().isEmpty() || !filter.modifiers().contains(ModIgnorePath.class)) {
                    for (String path : filter.path()) {
                        findFilterField(entityClass, path);
                    }
                }
            });
        }
        allFilters.forEach(this::checkRegisteredOperation);
    }

    private static void validateUniqueFilterParameters(List<FilterRequestData> filters, Parameter parameter) {
        Map<String, FilterRequestData> filtersByParameterName = new HashMap<>();
        for (FilterRequestData filter : filters) {
            for (String parameterName : filter.parameters()) {
                FilterRequestData existingFilter = filtersByParameterName.putIfAbsent(parameterName, filter);
                if (existingFilter != null) {
                    throw new DynamicFilterConfigurationException(
                            "Filter parameter name '%s' is configured more than once across filters at %s. First filter: operation '%s' on path '%s'; duplicated filter: operation '%s' on path '%s'"
                                    .formatted(parameterName, formatParameterLocation(parameter), existingFilter.operation().getCanonicalName(), formatPath(existingFilter.path()), filter.operation().getCanonicalName(), formatPath(filter.path()))
                    );
                }
            }
        }
    }

    private static String formatParameterLocation(Parameter parameter) {
        Executable executable = parameter.getDeclaringExecutable();
        return "%s#%s, parameter %d '%s' (%s)".formatted(
                executable.getDeclaringClass().getCanonicalName(),
                executable.getName(),
                findParameterIndex(parameter, executable.getParameters()),
                parameter.getName(),
                parameter.getParameterizedType().getTypeName()
        );
    }

    private static int findParameterIndex(Parameter parameter, Parameter[] parameters) {
        for (int i = 0; i < parameters.length; i++) {
            if (parameters[i].equals(parameter)) {
                return i;
            }
        }
        return -1;
    }

    private void checkRegisteredOperation(FilterRequestData filter) {
        if (!Decorated.class.equals(filter.operation()) && !Dynamic.class.equals(filter.operation())
            && !filterOperationService.supports(filter.operation())) {
            throw new DynamicFilterConfigurationException("Filter operation '%s' used on path '%s' is not registered for JPA specifications"
                    .formatted(filter.operation().getCanonicalName(), formatPath(filter.path())));
        }

        FilterOperationMetadata metadata = filterOperationService.findMetadata(filter.operation());
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

    private static String formatArity(FilterArity arity) {
        if (arity.min() == arity.max()) {
            return "exactly " + arity.min();
        }
        if (arity.max() == FilterArity.UNBOUNDED) {
            return "at least " + arity.min();
        }
        return "between %d and %d".formatted(arity.min(), arity.max());
    }

    private static boolean isDynamicFilterParameter(Parameter parameter) {
        Class<?> parameterType = parameter.getType();
        return ConditionalStatement.class.equals(parameterType)
               || (parameterType.isInterface() && Specification.class.isAssignableFrom(parameterType));
    }
}
