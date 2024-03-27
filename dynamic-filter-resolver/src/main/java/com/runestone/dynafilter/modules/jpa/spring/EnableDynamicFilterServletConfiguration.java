package com.runestone.dynafilter.modules.jpa.spring;

import com.runestone.dynafilter.modules.jpa.repository.DynamicFilterJpaRepositoryBeanPostProcessor;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * Enables autoconfiguration of the {@link DynamicFilterServletAutoConfiguration} for a Spring MVC based project
 *
 * @author Marcelo Portilho
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Import({DynamicFilterServletAutoConfiguration.class, DynamicFilterJpaRepositoryBeanPostProcessor.class})
public @interface EnableDynamicFilterServletConfiguration {
}
