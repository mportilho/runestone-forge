package com.runestone.dynafilter.modules.jpa.spring;

import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;
import java.util.Objects;

public final class SpecificationDynamicFilterWebMvcConfigurer implements WebMvcConfigurer {

    private final SpecificationDynamicFilterArgumentResolver argumentResolver;

    public SpecificationDynamicFilterWebMvcConfigurer(SpecificationDynamicFilterArgumentResolver argumentResolver) {
        this.argumentResolver = Objects.requireNonNull(argumentResolver, "argumentResolver must not be null");
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(argumentResolver);
    }
}
