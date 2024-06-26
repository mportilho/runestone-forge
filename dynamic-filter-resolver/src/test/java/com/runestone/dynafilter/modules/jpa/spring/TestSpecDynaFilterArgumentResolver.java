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

import com.runestone.converters.impl.DefaultDataConversionService;
import com.runestone.dynafilter.core.generator.annotation.AnnotationStatementGenerator;
import com.runestone.dynafilter.modules.jpa.operation.SpecificationFilterOperationService;
import com.runestone.dynafilter.modules.jpa.resolver.SpecificationDynamicFilterResolver;
import com.runestone.dynafilter.modules.jpa.spring.tools.SearchLanguages;
import com.runestone.dynafilter.modules.jpa.spring.tools.SearchState;
import jakarta.servlet.http.HttpServletRequest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.MethodParameter;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.servlet.HandlerMapping;

import java.util.Map;

public class TestSpecDynaFilterArgumentResolver {

    private SpecificationDynamicFilterArgumentResolver createSpecificationDynaFilterArgumentResolver() {
        GenericApplicationContext applicationContext = Mockito.mock(GenericApplicationContext.class);
        AnnotationStatementGenerator generator = new AnnotationStatementGenerator(null);
        SpecificationFilterOperationService service = new SpecificationFilterOperationService(new DefaultDataConversionService());
        SpecificationDynamicFilterResolver resolver = new SpecificationDynamicFilterResolver(service);
        SpringFilterDecoratorFactory filterDecoratorFactory = new SpringFilterDecoratorFactory(applicationContext);
        return new SpecificationDynamicFilterArgumentResolver(generator, resolver, filterDecoratorFactory);
    }

    @Test
    public void testNullConstructorParameters() {
        Assertions.assertThatThrownBy(() -> new SpecificationDynamicFilterArgumentResolver(null, null, null))
                .isExactlyInstanceOf(NullPointerException.class)
                .hasMessageContaining("Statement generator");

        AnnotationStatementGenerator generator = new AnnotationStatementGenerator(null);
        Assertions.assertThatThrownBy(() -> new SpecificationDynamicFilterArgumentResolver(generator, null, null))
                .isExactlyInstanceOf(NullPointerException.class)
                .hasMessageContaining("Dynamic filter resolver");

        SpecificationFilterOperationService service = new SpecificationFilterOperationService(new DefaultDataConversionService());
        SpecificationDynamicFilterResolver resolver = new SpecificationDynamicFilterResolver(service);
        Assertions.assertThatThrownBy(() -> new SpecificationDynamicFilterArgumentResolver(generator, resolver, null))
                .isExactlyInstanceOf(NullPointerException.class)
                .hasMessageContaining("Filter decorator factory");
    }

    @Test
    public void testCreateParametersMapThrowOnNullHttpServletRequest() {
        var argumentResolver = createSpecificationDynaFilterArgumentResolver();
        NativeWebRequest webRequest = Mockito.mock(NativeWebRequest.class);
        Assertions.assertThatThrownBy(() -> argumentResolver.createParametersMap(webRequest))
                .isExactlyInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Obligatory HTTP Context not found");
    }

    @Test
    public void testCreateParametersMapWithNoElements() {
        var argumentResolver = createSpecificationDynaFilterArgumentResolver();
        NativeWebRequest webRequest = Mockito.mock(NativeWebRequest.class);
        HttpServletRequest httpServletRequest = Mockito.mock(HttpServletRequest.class);

        Mockito.when(webRequest.getNativeRequest(HttpServletRequest.class)).thenReturn(httpServletRequest);
        Mockito.when(webRequest.getParameterMap()).thenReturn(Map.of());
        Mockito.when(httpServletRequest.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE)).thenReturn(null);

        Map<String, Object> filterParameters = argumentResolver.createParametersMap(webRequest);
        Assertions.assertThat(filterParameters).isNotNull().isEmpty();
    }

    @Test
    public void testCreateParametersMapWithScalarElements() {
        var argumentResolver = createSpecificationDynaFilterArgumentResolver();
        NativeWebRequest webRequest = Mockito.mock(NativeWebRequest.class);
        HttpServletRequest httpServletRequest = Mockito.mock(HttpServletRequest.class);

        Mockito.when(webRequest.getNativeRequest(HttpServletRequest.class)).thenReturn(httpServletRequest);
        Mockito.when(webRequest.getParameterMap()).thenReturn(Map.of(
                "name", new String[]{"John"},
                "age", new String[]{"30"},
                "salary", new String[]{"1000.00"},
                "tags", new String[]{"tag1", "tag2"}
        ));
        Mockito.when(httpServletRequest.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE)).thenReturn(null);

        Map<String, Object> filterParameters = argumentResolver.createParametersMap(webRequest);
        Assertions.assertThat(filterParameters).isNotNull().containsExactlyInAnyOrderEntriesOf(Map.of(
                "name", "John",
                "age", "30",
                "salary", "1000.00",
                "tags", new String[]{"tag1", "tag2"}
        ));
    }

    @Test
    public void testCreateParametersMapWithUriVariableAttributes() {
        var argumentResolver = createSpecificationDynaFilterArgumentResolver();
        NativeWebRequest webRequest = Mockito.mock(NativeWebRequest.class);
        HttpServletRequest httpServletRequest = Mockito.mock(HttpServletRequest.class);

        Mockito.when(webRequest.getNativeRequest(HttpServletRequest.class)).thenReturn(httpServletRequest);
        Mockito.when(webRequest.getParameterMap()).thenReturn(Map.of());
        Mockito.when(httpServletRequest.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE)).thenReturn(Map.of(
                "streetName", "Main Street",
                "houseNumber", 123,
                "classification", new String[]{"A", "B"}
        ));

        Map<String, Object> filterParameters = argumentResolver.createParametersMap(webRequest);
        Assertions.assertThat(filterParameters).isNotNull().containsExactlyInAnyOrderEntriesOf(Map.of(
                "streetName", "Main Street",
                "houseNumber", 123,
                "classification", new String[]{"A", "B"}
        ));
    }

    @Test
    public void testCreateParametersMapWithMultiOriginData() {
        var argumentResolver = createSpecificationDynaFilterArgumentResolver();
        NativeWebRequest webRequest = Mockito.mock(NativeWebRequest.class);
        HttpServletRequest httpServletRequest = Mockito.mock(HttpServletRequest.class);

        Mockito.when(webRequest.getNativeRequest(HttpServletRequest.class)).thenReturn(httpServletRequest);
        Mockito.when(webRequest.getParameterMap()).thenReturn(Map.of(
                "name", new String[]{"John"},
                "age", new String[]{"30"},
                "salary", new String[]{"1000.00"},
                "tags", new String[]{"tag1", "tag2"}
        ));
        Mockito.when(httpServletRequest.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE)).thenReturn(Map.of(
                "streetName", "Main Street",
                "houseNumber", 123,
                "classification", new String[]{"A", "B"}
        ));

        Map<String, Object> filterParameters = argumentResolver.createParametersMap(webRequest);
        Assertions.assertThat(filterParameters).isNotNull().containsExactlyInAnyOrderEntriesOf(Map.of(
                "name", "John",
                "age", "30",
                "salary", "1000.00",
                "tags", new String[]{"tag1", "tag2"},
                "streetName", "Main Street",
                "houseNumber", 123,
                "classification", new String[]{"A", "B"}
        ));
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void testResolveArgumentWithAnnotations() {
        var argumentResolver = createSpecificationDynaFilterArgumentResolver();
        MethodParameter parameter = Mockito.mock(MethodParameter.class);
        NativeWebRequest webRequest = Mockito.mock(NativeWebRequest.class);
        HttpServletRequest httpServletRequest = Mockito.mock(HttpServletRequest.class);

        Mockito.when(parameter.getParameterType()).thenReturn((Class) Specification.class);
        Mockito.when(parameter.getParameterAnnotations()).thenReturn(SearchLanguages.class.getAnnotations());

        Mockito.when(webRequest.getNativeRequest(HttpServletRequest.class)).thenReturn(httpServletRequest);
        Mockito.when(webRequest.getParameterMap()).thenReturn(Map.of(
                "name", new String[]{"English"},
                "state", new String[]{"ON_USE"},
                "minCreationDate", new String[]{"2021-01-01"},
                "maxCreationDate", new String[]{"2021-12-31"}
        ));
        Mockito.when(httpServletRequest.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE)).thenReturn(null);

        Specification<?> specification = (Specification<?>) argumentResolver.resolveArgument(parameter, null, webRequest, null);
        Assertions.assertThat(specification).isNotNull();
    }

    @Test
    public void testResolveArgumentWithTypeAndAnnotations() {
        var argumentResolver = createSpecificationDynaFilterArgumentResolver();
        MethodParameter parameter = Mockito.mock(MethodParameter.class);
        NativeWebRequest webRequest = Mockito.mock(NativeWebRequest.class);
        HttpServletRequest httpServletRequest = Mockito.mock(HttpServletRequest.class);

        Mockito.when(parameter.getParameterType()).thenReturn((Class) SearchState.class);
        Mockito.when(parameter.getParameterAnnotations()).thenReturn(SearchLanguages.class.getAnnotations());

        Mockito.when(webRequest.getNativeRequest(HttpServletRequest.class)).thenReturn(httpServletRequest);
        Mockito.when(webRequest.getParameterMap()).thenReturn(Map.of(
                "name", new String[]{"English"},
                "state", new String[]{"ON_USE"},
                "minCreationDate", new String[]{"2021-01-01"},
                "maxCreationDate", new String[]{"2021-12-31"}
        ));
        Mockito.when(httpServletRequest.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE)).thenReturn(null);

        Specification<?> specification = (Specification<?>) argumentResolver.resolveArgument(parameter, null, webRequest, null);
        Assertions.assertThat(specification).isNotNull();
    }

    @Test
    public void testSupportsOperation() {
        var argumentResolver = createSpecificationDynaFilterArgumentResolver();
        MethodParameter parameter = Mockito.mock(MethodParameter.class);
        Mockito.when(parameter.getParameterType()).thenReturn((Class) SearchState.class);
        Assertions.assertThat(argumentResolver.supportsParameter(parameter)).isTrue();
    }

    @Test
    public void testDoesNotSupportsOperation() {
        var argumentResolver = createSpecificationDynaFilterArgumentResolver();
        MethodParameter parameter = Mockito.mock(MethodParameter.class);
        Mockito.when(parameter.getParameterType()).thenReturn((Class) SearchLanguages.class);
        Assertions.assertThat(argumentResolver.supportsParameter(parameter)).isFalse();
    }

}
