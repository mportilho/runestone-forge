package com.runestone.dynafilter.modules.jpa.spring;

import com.runestone.dynafilter.core.exceptions.DynamicFilterConfigurationException;
import com.runestone.dynafilter.core.transformer.FilterValueTransformer;
import com.runestone.dynafilter.core.transformer.FilterValueTransformerResolver;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.HierarchicalBeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.aop.scope.ScopedProxyUtils;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

final class SpringFilterValueTransformerResolver implements FilterValueTransformerResolver {

    private static final String SCOPED_TARGET_PREFIX = "scopedTarget.";

    private final ConfigurableListableBeanFactory beanFactory;

    SpringFilterValueTransformerResolver(ConfigurableListableBeanFactory beanFactory) {
        this.beanFactory = Objects.requireNonNull(beanFactory, "beanFactory is required");
    }

    @Override
    public FilterValueTransformer resolve(Class<? extends FilterValueTransformer> transformerType) {
        Objects.requireNonNull(transformerType, "transformerType is required");
        String[] candidateNames = BeanFactoryUtils.beanNamesForTypeIncludingAncestors(
                beanFactory, transformerType, true, false);
        Set<String> logicalCandidateNames = logicalCandidateNames(candidateNames);

        if (logicalCandidateNames.isEmpty()) {
            throw new DynamicFilterConfigurationException(
                    "Exactly one singleton Spring bean is required for filter value transformer '%s', but none was found"
                            .formatted(transformerType.getCanonicalName()));
        }
        if (logicalCandidateNames.size() > 1) {
            throw new DynamicFilterConfigurationException(
                    "Exactly one singleton Spring bean is required for filter value transformer '%s', but found %d: %s"
                            .formatted(transformerType.getCanonicalName(), logicalCandidateNames.size(), logicalCandidateNames));
        }

        String beanName = logicalCandidateNames.iterator().next();
        String scopedTargetName = SCOPED_TARGET_PREFIX + beanName;
        if (containsBean(scopedTargetName) && !beanFactory.isSingleton(scopedTargetName)) {
            throw incompatibleScope(transformerType, beanName, scopeOf(scopedTargetName));
        }
        if (!beanFactory.isSingleton(beanName)) {
            throw incompatibleScope(transformerType, beanName, scopeOf(beanName));
        }
        return beanFactory.getBean(beanName, transformerType);
    }

    private static Set<String> logicalCandidateNames(String[] candidateNames) {
        Arrays.sort(candidateNames);
        Set<String> logicalNames = new LinkedHashSet<>(candidateNames.length);
        for (String candidateName : candidateNames) {
            logicalNames.add(ScopedProxyUtils.isScopedTarget(candidateName)
                    ? candidateName.substring(SCOPED_TARGET_PREFIX.length())
                    : candidateName);
        }
        return logicalNames;
    }

    private boolean containsBean(String beanName) {
        return beanFactory.containsBean(beanName);
    }

    private String scopeOf(String beanName) {
        ConfigurableListableBeanFactory current = beanFactory;
        while (current != null) {
            if (current.containsBeanDefinition(beanName)) {
                BeanDefinition beanDefinition = current.getMergedBeanDefinition(beanName);
                String scope = beanDefinition.getScope();
                return scope == null || scope.isBlank() ? BeanDefinition.SCOPE_SINGLETON : scope;
            }
            BeanFactory parent = current instanceof HierarchicalBeanFactory hierarchical
                    ? hierarchical.getParentBeanFactory()
                    : null;
            current = parent instanceof ConfigurableListableBeanFactory configurable ? configurable : null;
        }
        return "non-singleton";
    }

    private static DynamicFilterConfigurationException incompatibleScope(
            Class<? extends FilterValueTransformer> transformerType, String beanName, String scope) {
        return new DynamicFilterConfigurationException(
                "Filter value transformer bean '%s' for type '%s' has incompatible scope '%s'; only singleton, stateless and thread-safe beans are supported"
                        .formatted(beanName, transformerType.getCanonicalName(), scope));
    }
}
