package com.runestone.dynafilter.modules.jpa.spring;

import com.runestone.dynafilter.core.generator.ValueExpressionResolver;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.context.ApplicationContext;
import org.springframework.util.StringValueResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

public class TestServletDynaFilterAutoConfiguration {

    @Test
    public void testServletConfigurationCreation() {
        ServletDynaFilterAutoConfiguration servletConfig = new ServletDynaFilterAutoConfiguration();
        servletConfig.setApplicationContext(Mockito.mock(ApplicationContext.class));
        servletConfig.setEmbeddedValueResolver(Mockito.mock(StringValueResolver.class));
        WebMvcConfigurer webMvcConfigurer = servletConfig.webMvcConfigurer(servletConfig.dataConversionService(), null);
        Assertions.assertThat(webMvcConfigurer).isNotNull();
    }

}
