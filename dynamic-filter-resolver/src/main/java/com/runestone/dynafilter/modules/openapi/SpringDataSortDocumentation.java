package com.runestone.dynafilter.modules.openapi;

import com.runestone.dynafilter.core.exceptions.DynamicFilterConfigurationException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.core.MethodParameter;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.data.web.SortHandlerMethodArgumentResolver;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

/** Reads metadata from the selected Spring Data resolver without parsing or validating requests. */
final class SpringDataSortDocumentation {
    private SpringDataSortDocumentation() {
    }

    static SortHandlerMethodArgumentResolver resolver(BeanFactory beans, MethodParameter parameter) {
        var adapter = beans.getBeanProvider(RequestMappingHandlerAdapter.class).getIfAvailable();
        if (adapter != null && adapter.getArgumentResolvers() != null) {
            var selected = adapter.getArgumentResolvers().stream().filter(value -> value.supportsParameter(parameter))
                    .findFirst().orElseThrow(() -> unavailable(parameter));
            return sortResolver(selected, parameter);
        }
        // Also support customizers used with standalone MVC or before the handler adapter is available.
        if (Pageable.class.isAssignableFrom(parameter.getParameterType())) {
            var pageable = beans.getBeanProvider(PageableHandlerMethodArgumentResolver.class).getIfAvailable();
            if (pageable != null) {
                return sortResolver(pageable, parameter);
            }
        }
        var sort = beans.getBeanProvider(SortHandlerMethodArgumentResolver.class).getIfAvailable();
        if (sort == null) {
            throw unavailable(parameter);
        }
        return sort;
    }

    private static SortHandlerMethodArgumentResolver sortResolver(HandlerMethodArgumentResolver selected, MethodParameter parameter) {
        if (selected instanceof SortHandlerMethodArgumentResolver sort) {
            return sort;
        }
        if (selected instanceof PageableHandlerMethodArgumentResolver pageable) {
            // Spring Data does not expose the sort resolver used by Pageable through a public accessor.
            var field = ReflectionUtils.findField(PageableHandlerMethodArgumentResolver.class, "sortResolver");
            if (field != null) {
                ReflectionUtils.makeAccessible(field);
                if (ReflectionUtils.getField(field, pageable) instanceof SortHandlerMethodArgumentResolver sort) {
                    return sort;
                }
            }
        }
        throw unavailable(parameter);
    }

    static String parameterName(SortHandlerMethodArgumentResolver resolver, MethodParameter parameter) {
        // Invoke the configured resolver's accessor, including subclass overrides and qualifiers.
        var accessor = ReflectionUtils.findMethod(resolver.getClass(), "getSortParameter", MethodParameter.class);
        if (accessor == null) {
            throw unavailable(parameter);
        }
        ReflectionUtils.makeAccessible(accessor);
        return (String) ReflectionUtils.invokeMethod(accessor, resolver, parameter);
    }

    private static DynamicFilterConfigurationException unavailable(MethodParameter parameter) {
        return new DynamicFilterConfigurationException("A configured Spring Data sort resolver is required for sort documentation at " + parameter);
    }
}
