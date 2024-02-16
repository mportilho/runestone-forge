package com.runestone.dynafilter.modules.jpa.spring;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * Enables autoconfiguration of the {@link ServletDynamicFilterAutoConfiguration} for a Spring MVC based project
 *
 * @author Marcelo Portilho
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Import({ServletDynamicFilterAutoConfiguration.class})
public @interface EnableDynamicFilterConfiguration {
}
