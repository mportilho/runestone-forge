package com.runestone.dynafilter.modules.jpa.resolver;

import com.runestone.converters.impl.DefaultDataConversionService;
import com.runestone.dynafilter.core.generator.annotation.AnnotationStatementGenerator;
import com.runestone.dynafilter.core.generator.annotation.ConjunctionFrom;
import com.runestone.dynafilter.core.resolver.CompositeFilterDecorator;
import com.runestone.dynafilter.modules.jpa.operation.SpecificationFilterOperationService;
import com.runestone.dynafilter.modules.jpa.resolver.tools.GetPerson;
import com.runestone.dynafilter.modules.jpa.resolver.tools.PersonFilterDecorador;
import com.runestone.dynafilter.modules.jpa.spring.SpecificationDynamicFilterArgumentResolver;
import com.runestone.dynafilter.modules.jpa.spring.SpringFilterDecoratorFactory;
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

import java.util.Map;

@SpringJUnitConfig(InMemoryDatabaseApplication.class)
public class TestGenericJpaFilterDecorator {

    private SpringFilterDecoratorFactory filterDecoratorFactory;
    private SpecificationDynamicFilterResolver resolver;

    @Autowired
    private GenericApplicationContext applicationContext;

    @Autowired
    private PersonRepository personRepository;

    @ConjunctionFrom(GetPerson.class)
    interface AnnotatedParameter {
    }

    private SpecificationDynamicFilterArgumentResolver createSpecificationDynaFilterArgumentResolver() {
        AnnotationStatementGenerator generator = new AnnotationStatementGenerator(null);
        SpecificationFilterOperationService service = new SpecificationFilterOperationService(new DefaultDataConversionService());
        resolver = Mockito.spy(new SpecificationDynamicFilterResolver(service));
        filterDecoratorFactory = Mockito.spy(new SpringFilterDecoratorFactory(applicationContext));
        return new SpecificationDynamicFilterArgumentResolver(generator, resolver, filterDecoratorFactory);
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void testFilterDecoratorWithDecoratedAttribute() {
        SpecificationDynamicFilterArgumentResolver argumentResolver = createSpecificationDynaFilterArgumentResolver();
        MethodParameter parameter = Mockito.mock(MethodParameter.class);
        NativeWebRequest webRequest = Mockito.mock(NativeWebRequest.class);
        HttpServletRequest httpServletRequest = Mockito.mock(HttpServletRequest.class);

        Mockito.when(parameter.getParameterType()).thenReturn((Class) Specification.class);
        Mockito.when(parameter.getParameterAnnotations()).thenReturn(AnnotatedParameter.class.getAnnotations());
        Mockito.when(webRequest.getNativeRequest(HttpServletRequest.class)).thenReturn(httpServletRequest);
        Mockito.when(webRequest.getParameterMap()).thenReturn(Map.of(
                "name", new String[]{"English"},
                "decoratedAttribute", new String[]{"decor_value"}
        ));
        Mockito.when(httpServletRequest.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE)).thenReturn(null);

        Specification<Person> specification = (Specification<Person>) argumentResolver.resolveArgument(parameter, null, webRequest, null);

        Assertions.assertThat(specification).isNotNull();
        personRepository.findAll(specification);

        Mockito.verify(filterDecoratorFactory, Mockito.times(1)).createFilterDecorators(Mockito.any());
        Mockito.verify(resolver, Mockito.times(1)).createFilter(Mockito.any(), Mockito.argThat(filterDecor -> {
            if (filterDecor instanceof CompositeFilterDecorator<Specification<?>> compositeFilterDecorator) {
                Assertions.assertThat(compositeFilterDecorator.getDecorators()).hasSize(1);
                compositeFilterDecorator.getDecorators().stream().findFirst().ifPresent(decorator -> {
                    Assertions.assertThat(decorator.getClass().equals(PersonFilterDecorador.class)).isTrue();
                });
                return true;
            }
            return false;
        }));
    }

}
