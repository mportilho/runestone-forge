package com.runestone.dynafilter.modules.jpa.spring;

import com.runestone.dynafilter.core.resolver.CompositeFilterDecorator;
import com.runestone.dynafilter.core.resolver.FilterDecorator;
import com.runestone.dynafilter.modules.jpa.resolver.FetchingFilterDecorator;
import com.runestone.dynafilter.modules.jpa.spring.tools.FetchingAnnotationHolder;
import com.runestone.dynafilter.modules.jpa.spring.tools.FetchingMultiAnnotationHolder;
import com.runestone.dynafilter.modules.jpa.spring.tools.MultiArgsConstructorFilterDecorator;
import com.runestone.dynafilter.modules.jpa.spring.tools.NoArgsConstructorFilterDecorator;
import com.runestone.dynafilter.modules.jpa.tools.app.simple.SimpleApplication;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@SpringJUnitConfig(SimpleApplication.class)
public class TestFilterDecoratorSpringFactory {

    @Autowired
    private GenericApplicationContext applicationContext;

    @Test
    public void testNullParameters() {
        FilterDecoratorSpringFactory factory = new FilterDecoratorSpringFactory(applicationContext);
        FilterDecorator<Specification<?>> filterDecorators = factory.createFilterDecorators(null, null);
        Assertions.assertThat(filterDecorators).isNull();
    }

    @Test
    public void testWithFetchingAnnotations() {
        FilterDecoratorSpringFactory factory = new FilterDecoratorSpringFactory(applicationContext);
        var annotations = FetchingAnnotationHolder.class.getAnnotations();
        FetchingFilterDecorator filterDecorators = (FetchingFilterDecorator) factory.createFilterDecorators(annotations, null);
        Assertions.assertThat(filterDecorators).isNotNull();
        Assertions.assertThat(filterDecorators.getFetches()).hasSize(1);
    }

    @Test
    public void testWithFetchingMultiAnnotations() {
        FilterDecoratorSpringFactory factory = new FilterDecoratorSpringFactory(applicationContext);
        var annotations = FetchingMultiAnnotationHolder.class.getAnnotations();
        FetchingFilterDecorator filterDecorators = (FetchingFilterDecorator) factory.createFilterDecorators(annotations, null);
        Assertions.assertThat(filterDecorators).isNotNull();
        Assertions.assertThat(filterDecorators.getFetches()).hasSize(2);
    }

    @Test
    public void testWithNoArgsFilterDecorators() {
        FilterDecoratorSpringFactory factory = new FilterDecoratorSpringFactory(applicationContext);
        Class<? extends FilterDecorator<?>>[] decoratorClasses = new Class[]{NoArgsConstructorFilterDecorator.class};
        CompositeFilterDecorator<?> filterDecorators = (CompositeFilterDecorator<?>) factory.createFilterDecorators(null, decoratorClasses);
        Assertions.assertThat(filterDecorators).isNotNull();
        Assertions.assertThat(filterDecorators.getDecorators()).hasSize(1);
    }

    @Test
    public void testWithMultiArgsFilterDecorators() {
        FilterDecoratorSpringFactory factory = new FilterDecoratorSpringFactory(applicationContext);
        Class<? extends FilterDecorator<?>>[] decoratorClasses = new Class[]{MultiArgsConstructorFilterDecorator.class};
        CompositeFilterDecorator<?> filterDecorators = (CompositeFilterDecorator<?>) factory.createFilterDecorators(null, decoratorClasses);
        Assertions.assertThat(filterDecorators).isNotNull();
        Assertions.assertThat(filterDecorators.getDecorators()).hasSize(1);
    }

    @Test
    public void testWithMultiOriginFilters() {
        FilterDecoratorSpringFactory factory = new FilterDecoratorSpringFactory(applicationContext);
        var annotations = FetchingAnnotationHolder.class.getAnnotations();
        Class<? extends FilterDecorator<?>>[] decoratorClasses = new Class[]{NoArgsConstructorFilterDecorator.class, MultiArgsConstructorFilterDecorator.class};
        CompositeFilterDecorator<?> filterDecorators = (CompositeFilterDecorator<?>) factory.createFilterDecorators(annotations, decoratorClasses);
        Assertions.assertThat(filterDecorators).isNotNull();
        Assertions.assertThat(filterDecorators.getDecorators()).hasSize(3);
    }

}
