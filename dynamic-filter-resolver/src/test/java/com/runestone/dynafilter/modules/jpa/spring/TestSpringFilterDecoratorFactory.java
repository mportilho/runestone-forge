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

package com.runestone.dynafilter.modules.jpa.spring;

import com.runestone.dynafilter.core.generator.annotation.AnnotationStatementInput;
import com.runestone.dynafilter.core.generator.annotation.FilterDecorators;
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
public class TestSpringFilterDecoratorFactory {

    @Autowired
    private GenericApplicationContext applicationContext;

    @FilterDecorators(NoArgsConstructorFilterDecorator.class)
    static class NoArgsFilterDecorClassContainer {
    }

    @FilterDecorators(MultiArgsConstructorFilterDecorator.class)
    static class MultiArgsFilterDecorClassContainer {
    }

    @FilterDecorators({NoArgsConstructorFilterDecorator.class, MultiArgsConstructorFilterDecorator.class})
    static class MultipleFilterDecorClassContainer {
    }

    @Test
    public void testNullParameters() {
        SpringFilterDecoratorFactory factory = new SpringFilterDecoratorFactory(applicationContext);
        FilterDecorator<Specification<?>> filterDecorators = factory.createFilterDecorators(null);
        Assertions.assertThat(filterDecorators).isNull();

        filterDecorators = factory.createFilterDecorators(new AnnotationStatementInput(null, null));
        Assertions.assertThat(filterDecorators).isNull();
    }

    @Test
    public void testWithFetchingAnnotations() {
        SpringFilterDecoratorFactory factory = new SpringFilterDecoratorFactory(applicationContext);
        var annotations = FetchingAnnotationHolder.class.getAnnotations();
        AnnotationStatementInput input = new AnnotationStatementInput(null, annotations);
        FetchingFilterDecorator filterDecorators = (FetchingFilterDecorator) factory.createFilterDecorators(input);
        Assertions.assertThat(filterDecorators).isNotNull();
        Assertions.assertThat(filterDecorators.getFetches()).hasSize(1);
    }

    @Test
    public void testWithFetchingMultiAnnotations() {
        SpringFilterDecoratorFactory factory = new SpringFilterDecoratorFactory(applicationContext);
        var annotations = FetchingMultiAnnotationHolder.class.getAnnotations();
        AnnotationStatementInput input = new AnnotationStatementInput(null, annotations);
        FetchingFilterDecorator filterDecorators = (FetchingFilterDecorator) factory.createFilterDecorators(input);
        Assertions.assertThat(filterDecorators).isNotNull();
        Assertions.assertThat(filterDecorators.getFetches()).hasSize(2);
    }

    @Test
    public void testWithNoArgsFilterDecorators() {
        SpringFilterDecoratorFactory factory = new SpringFilterDecoratorFactory(applicationContext);
        AnnotationStatementInput input = new AnnotationStatementInput(NoArgsFilterDecorClassContainer.class, null);
        CompositeFilterDecorator<?> filterDecorators = (CompositeFilterDecorator<?>) factory.createFilterDecorators(input);
        Assertions.assertThat(filterDecorators).isNotNull();
        Assertions.assertThat(filterDecorators.getDecorators()).hasSize(1);
    }

    @Test
    public void testWithMultiArgsFilterDecorators() {
        SpringFilterDecoratorFactory factory = new SpringFilterDecoratorFactory(applicationContext);
        AnnotationStatementInput input = new AnnotationStatementInput(MultiArgsFilterDecorClassContainer.class, null);
        CompositeFilterDecorator<?> filterDecorators = (CompositeFilterDecorator<?>) factory.createFilterDecorators(input);
        Assertions.assertThat(filterDecorators).isNotNull();
        Assertions.assertThat(filterDecorators.getDecorators()).hasSize(1);
    }

    @Test
    public void testWithMultiOriginFilters() {
        SpringFilterDecoratorFactory factory = new SpringFilterDecoratorFactory(applicationContext);
        var annotations = FetchingAnnotationHolder.class.getAnnotations();
        AnnotationStatementInput input = new AnnotationStatementInput(MultipleFilterDecorClassContainer.class, annotations);
        CompositeFilterDecorator<?> filterDecorators = (CompositeFilterDecorator<?>) factory.createFilterDecorators(input);
        Assertions.assertThat(filterDecorators).isNotNull();
        Assertions.assertThat(filterDecorators.getDecorators()).hasSize(3);
    }

}
