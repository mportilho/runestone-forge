package com.runestone.dynafilter.modules.jpa.spring;

import com.runestone.dynafilter.core.exceptions.DynamicFilterConfigurationException;
import com.runestone.dynafilter.core.generator.ConditionalStatement;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.MethodParameter;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** MVC metadata association only. Repository callers still supply the statement and ordering explicitly. */
public record SortContractAssociation(MethodParameter contract, MethodParameter ordering) {

    public static List<SortContractAssociation> forMethod(Method method) {
        List<MethodParameter> contracts = new ArrayList<>();
        List<MethodParameter> ordering = new ArrayList<>();
        for (int i = 0; i < method.getParameterCount(); i++) {
            var parameter = new MethodParameter(method, i);
            if (parameter.getParameterType() == ConditionalStatement.class) {
                contracts.add(parameter);
            } else if (Pageable.class.isAssignableFrom(parameter.getParameterType())
                    || Sort.class.isAssignableFrom(parameter.getParameterType())) {
                ordering.add(parameter);
            }
        }
        if (contracts.isEmpty() || ordering.isEmpty()) {
            return List.of();
        }
        if (contracts.size() == 1 && ordering.size() == 1) {
            return List.of(new SortContractAssociation(contracts.getFirst(), ordering.getFirst()));
        }
        List<SortContractAssociation> result = new ArrayList<>();
        Set<MethodParameter> associated = new HashSet<>();
        for (MethodParameter order : ordering) {
            Qualifier qualifier = order.getParameterAnnotation(Qualifier.class);
            if (qualifier == null || qualifier.value().isBlank()) {
                throw ambiguous(method);
            }
            var matches = contracts.stream().filter(contract -> {
                Qualifier value = contract.getParameterAnnotation(Qualifier.class);
                return value != null && value.value().equals(qualifier.value());
            }).toList();
            if (matches.size() != 1 || !associated.add(matches.getFirst())) {
                throw ambiguous(method);
            }
            result.add(new SortContractAssociation(matches.getFirst(), order));
        }
        return List.copyOf(result);
    }

    private static DynamicFilterConfigurationException ambiguous(Method method) {
        return new DynamicFilterConfigurationException("Sort arguments require unique matching @Qualifier values at " + method);
    }
}
