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

package com.runestone.dynafilter.modules.jpa.resolver;

import com.runestone.converters.impl.DefaultDataConversionService;
import com.runestone.dynafilter.core.generator.annotation.AnnotationStatementGenerator;
import com.runestone.dynafilter.modules.jpa.operation.SpecificationFilterOperationService;
import com.runestone.dynafilter.modules.jpa.resolver.tools.SearchEmployees;
import com.runestone.dynafilter.modules.jpa.resolver.tools.SearchMultiDataEmployees;
import com.runestone.dynafilter.modules.jpa.spring.SpringFilterDecoratorFactory;
import com.runestone.dynafilter.modules.jpa.spring.SpecificationDynamicFilterArgumentResolver;
import com.runestone.dynafilter.modules.jpa.tools.app.database.InMemoryDatabaseApplication;
import com.runestone.dynafilter.modules.jpa.tools.app.database.PersonRepository;
import com.runestone.dynafilter.modules.jpa.tools.app.database.jpamodels.Person;
import jakarta.servlet.http.HttpServletRequest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.MethodParameter;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.servlet.HandlerMapping;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@SpringJUnitConfig(InMemoryDatabaseApplication.class)
public class TestFetchingFilterDecorator {

    private SpringFilterDecoratorFactory filterDecoratorFactory;
    private SpecificationDynamicFilterResolver resolver;

    @Autowired
    private GenericApplicationContext applicationContext;

    @Autowired
    private PersonRepository personRepository;

    private SpecificationDynamicFilterArgumentResolver createSpecificationDynaFilterArgumentResolver() {
        AnnotationStatementGenerator generator = new AnnotationStatementGenerator(null);
        SpecificationFilterOperationService service = new SpecificationFilterOperationService(new DefaultDataConversionService());
        resolver = Mockito.spy(new SpecificationDynamicFilterResolver(service));
        filterDecoratorFactory = Mockito.spy(new SpringFilterDecoratorFactory(applicationContext));
        return new SpecificationDynamicFilterArgumentResolver(generator, resolver, filterDecoratorFactory);
    }

    @Test
    public void testThrowOnEmptyFetches() {
        Assertions.assertThatThrownBy(() -> new FetchingFilterDecorator(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("fetches is required to be not empty");
        Assertions.assertThatThrownBy(() -> new FetchingFilterDecorator(List.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("fetches is required to be not empty");
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void testWithFetchingAnnotations() {
        SpecificationDynamicFilterArgumentResolver argumentResolver = createSpecificationDynaFilterArgumentResolver();
        MethodParameter parameter = Mockito.mock(MethodParameter.class);
        NativeWebRequest webRequest = Mockito.mock(NativeWebRequest.class);
        HttpServletRequest httpServletRequest = Mockito.mock(HttpServletRequest.class);

        Mockito.when(parameter.getParameterType()).thenReturn((Class) Specification.class);
        Mockito.when(parameter.getParameterAnnotations()).thenReturn(SearchEmployees.class.getAnnotations());
        Mockito.when(webRequest.getNativeRequest(HttpServletRequest.class)).thenReturn(httpServletRequest);
        Mockito.when(webRequest.getParameterMap()).thenReturn(Map.of(
                "name", new String[]{"English"},
                "state", new String[]{"ON_USE"},
                "minCreationDate", new String[]{"2021-01-01"},
                "maxCreationDate", new String[]{"2021-12-31"}
        ));
        Mockito.when(httpServletRequest.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE)).thenReturn(null);

        Specification<Person> specification = (Specification<Person>) argumentResolver.resolveArgument(parameter, null, webRequest, null);
        assert specification != null;
        personRepository.findAll(specification);

        Mockito.verify(filterDecoratorFactory, Mockito.times(1)).createFilterDecorators(Mockito.any());
        Mockito.verify(resolver, Mockito.times(1)).createFilter(Mockito.any(), Mockito.argThat(filterDecor -> {
            if (filterDecor instanceof FetchingFilterDecorator fetchingFilterDecorator) {
                Collection<Fetching> fetches = fetchingFilterDecorator.getFetches();
                Assertions.assertThat(fetches).hasSize(1);
                Assertions.assertThat(fetches).anyMatch(fetching -> fetching.value().length == 1 && fetching.value()[0].equals("addresses"));
                return true;
            }
            return false;
        }));
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void testWithFetchingMultiAnnotations() {
        var argumentResolver = createSpecificationDynaFilterArgumentResolver();
        MethodParameter parameter = Mockito.mock(MethodParameter.class);
        NativeWebRequest webRequest = Mockito.mock(NativeWebRequest.class);
        HttpServletRequest httpServletRequest = Mockito.mock(HttpServletRequest.class);

        Mockito.when(parameter.getParameterType()).thenReturn((Class) Specification.class);
        Mockito.when(parameter.getParameterAnnotations()).thenReturn(SearchMultiDataEmployees.class.getAnnotations());
        Mockito.when(webRequest.getNativeRequest(HttpServletRequest.class)).thenReturn(httpServletRequest);
        Mockito.when(webRequest.getParameterMap()).thenReturn(Map.of(
                "name", new String[]{"English"},
                "state", new String[]{"ON_USE"},
                "minCreationDate", new String[]{"2021-01-01"},
                "maxCreationDate", new String[]{"2021-12-31"}
        ));
        Mockito.when(httpServletRequest.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE)).thenReturn(null);

        Specification<Person> specification = (Specification<Person>) argumentResolver.resolveArgument(parameter, null, webRequest, null);
        assert specification != null;
        personRepository.findAll(specification);

        Mockito.verify(filterDecoratorFactory, Mockito.times(1)).createFilterDecorators(Mockito.any());
        Mockito.verify(resolver, Mockito.times(1)).createFilter(Mockito.any(), Mockito.argThat(filterDecor -> {
            if (filterDecor instanceof FetchingFilterDecorator fetchingFilterDecorator) {
                Collection<Fetching> fetches = fetchingFilterDecorator.getFetches();
                Assertions.assertThat(fetches).hasSize(2);
                Assertions.assertThat(fetches).anyMatch(fetching -> fetching.value().length == 1 && fetching.value()[0].equals("addresses.location"));
                Assertions.assertThat(fetches).anyMatch(fetching -> fetching.value().length == 1 && fetching.value()[0].equals("phones"));
                return true;
            }
            return false;
        }));
    }

}
