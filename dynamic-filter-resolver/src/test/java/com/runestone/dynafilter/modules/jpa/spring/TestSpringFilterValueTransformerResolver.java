package com.runestone.dynafilter.modules.jpa.spring;

import com.github.benmanes.caffeine.cache.Cache;
import com.runestone.dynafilter.core.exceptions.DynamicFilterConfigurationException;
import com.runestone.dynafilter.core.generator.ConditionalStatement;
import com.runestone.dynafilter.core.generator.annotation.AnnotationStatementGenerator;
import com.runestone.dynafilter.core.generator.annotation.AnnotationStatementInput;
import com.runestone.dynafilter.core.generator.annotation.Conjunction;
import com.runestone.dynafilter.core.generator.annotation.Filter;
import com.runestone.dynafilter.core.generator.annotation.FilterTarget;
import com.runestone.dynafilter.core.model.modifiers.ModIgnorePath;
import com.runestone.dynafilter.core.model.statement.LogicalStatement;
import com.runestone.dynafilter.core.operation.types.Equals;
import com.runestone.dynafilter.core.transformer.FilterValueContext;
import com.runestone.dynafilter.core.transformer.FilterValueTransformer;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.core.annotation.Order;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.stereotype.Controller;

import java.util.Map;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;

class TestSpringFilterValueTransformerResolver {

    @Test
    void resolvesTheOnlyCompatibleSingletonBeanByIdentity() {
        PrefixTransformer transformer = new PrefixTransformer("bean-");
        try (var context = contextWith("transformer", PrefixTransformer.class, transformer)) {
            var resolver = new SpringFilterValueTransformerResolver(context.getBeanFactory());

            assertThat(resolver.resolve(PrefixTransformer.class)).isSameAs(transformer);
        }
    }

    @Test
    void doesNotInstantiateAnUnregisteredTransformer() {
        try (var context = new AnnotationConfigApplicationContext()) {
            context.refresh();
            var resolver = new SpringFilterValueTransformerResolver(context.getBeanFactory());

            assertThatThrownBy(() -> resolver.resolve(PrefixTransformer.class))
                    .isInstanceOf(DynamicFilterConfigurationException.class)
                    .hasMessageContaining(PrefixTransformer.class.getCanonicalName())
                    .hasMessageContaining("none was found");
        }
    }

    @Test
    void rejectsAmbiguousBeansEvenWhenOneIsPrimary() {
        try (var context = new AnnotationConfigApplicationContext()) {
            context.registerBean("first", PrefixTransformer.class, () -> new PrefixTransformer("first-"),
                    definition -> definition.setPrimary(true));
            context.registerBean("second", PrefixTransformer.class, () -> new PrefixTransformer("second-"));
            context.refresh();
            var resolver = new SpringFilterValueTransformerResolver(context.getBeanFactory());

            assertThatThrownBy(() -> resolver.resolve(PrefixTransformer.class))
                    .isInstanceOf(DynamicFilterConfigurationException.class)
                    .hasMessageContaining("found 2")
                    .hasMessageContaining("first")
                    .hasMessageContaining("second");
        }
    }

    @Test
    void rejectsNonSingletonScopesBeforeRetrievingTheBean() {
        try (var context = new AnnotationConfigApplicationContext()) {
            context.registerBean("prototypeTransformer", PrefixTransformer.class,
                    () -> new PrefixTransformer("prototype-"),
                    definition -> definition.setScope(BeanDefinition.SCOPE_PROTOTYPE));
            context.refresh();
            var resolver = new SpringFilterValueTransformerResolver(context.getBeanFactory());

            assertThatThrownBy(() -> resolver.resolve(PrefixTransformer.class))
                    .isInstanceOf(DynamicFilterConfigurationException.class)
                    .hasMessageContaining("prototypeTransformer")
                    .hasMessageContaining("scope 'prototype'")
                    .hasMessageContaining("stateless and thread-safe");
        }
    }

    @Test
    void rejectsScopedProxiesBasedOnTheirTargetScope() {
        try (var context = new AnnotationConfigApplicationContext(ScopedTransformerConfiguration.class)) {
            var resolver = new SpringFilterValueTransformerResolver(context.getBeanFactory());

            assertThatThrownBy(() -> resolver.resolve(ScopedTransformer.class))
                    .isInstanceOf(DynamicFilterConfigurationException.class)
                    .hasMessageContaining("scopedTransformer")
                    .hasMessageContaining("scope 'request'");
        }
    }

    @Test
    void acceptsScopedProxiesWhoseTargetIsSingleton() {
        try (var context = new AnnotationConfigApplicationContext(SingletonProxyTransformerConfiguration.class)) {
            var resolver = new SpringFilterValueTransformerResolver(context.getBeanFactory());

            assertThat(resolver.resolve(ScopedTransformer.class))
                    .isSameAs(context.getBean(ScopedTransformer.class));
        }
    }

    @Test
    void resolvesBeanMethodTransformersWithInjectedDependencies() {
        try (var context = new AnnotationConfigApplicationContext()) {
            context.register(DynamicFilterServletAutoConfiguration.class, BeanTransformerConfiguration.class);
            context.refresh();
            AnnotationStatementGenerator generator = context.getBean(AnnotationStatementGenerator.class);

            assertThat(generateValue(generator, InjectedFilter.class, "alias")).isEqualTo("42");
            assertThat(context.getBean(InjectedTransformer.class).dependency).isSameAs(context.getBean(LookupDependency.class));
        }
    }

    @Test
    void failsApplicationContextStartupWhenAControllerTransformerIsMissing() {
        try (var context = new AnnotationConfigApplicationContext()) {
            context.register(DynamicFilterServletAutoConfiguration.class,
                    FilterConfigurationAnalyserBeanPostProcessor.class);
            context.registerBean("missingTransformerController", MissingTransformerController.class,
                    definition -> definition.setLazyInit(true));

            assertThatThrownBy(context::refresh)
                    .isInstanceOf(DynamicFilterConfigurationException.class)
                    .hasMessage("Exactly one singleton Spring bean is required for filter value transformer '%s', but none was found"
                            .formatted(MissingTransformer.class.getCanonicalName()));
        }
    }

    @Test
    void failsApplicationContextStartupWhenAControllerTransformerIsAmbiguous() {
        try (var context = new AnnotationConfigApplicationContext()) {
            context.register(DynamicFilterServletAutoConfiguration.class,
                    FilterConfigurationAnalyserBeanPostProcessor.class);
            context.registerBean("missingTransformerController", MissingTransformerController.class,
                    definition -> definition.setLazyInit(true));
            context.registerBean("firstTransformer", MissingTransformer.class, MissingTransformer::new);
            context.registerBean("secondTransformer", MissingTransformer.class, MissingTransformer::new);

            assertThatThrownBy(context::refresh)
                    .isInstanceOf(DynamicFilterConfigurationException.class)
                    .hasMessageContaining("found 2")
                    .hasMessageContaining("firstTransformer")
                    .hasMessageContaining("secondTransformer");
        }
    }

    @Test
    void clearsContextOwnedPlansWhenTheApplicationContextCloses() {
        var context = new AnnotationConfigApplicationContext();
        context.register(DynamicFilterServletAutoConfiguration.class, BeanTransformerConfiguration.class);
        context.refresh();
        AnnotationStatementGenerator generator = context.getBean(AnnotationStatementGenerator.class);
        generator.warmup(input(InjectedFilter.class));
        @SuppressWarnings("unchecked")
        Cache<Object, Object> planCache = (Cache<Object, Object>) ReflectionTestUtils.getField(generator, "planCache");
        assertThat(planCache).isNotNull();
        assertThat(planCache.estimatedSize()).isEqualTo(1);

        context.close();

        assertThat(planCache.estimatedSize()).isZero();
    }

    @Test
    void preservesAnnotationOrderAndRepeatedClassesRegardlessOfOrderMetadata() {
        try (var context = new AnnotationConfigApplicationContext()) {
            RepeatedTransformer repeated = new RepeatedTransformer();
            context.registerBean("prefix", PrefixTransformer.class, () -> new PrefixTransformer("prefix-"));
            context.registerBean("suffix", SuffixTransformer.class, SuffixTransformer::new);
            context.registerBean("repeated", RepeatedTransformer.class, () -> repeated);
            context.refresh();
            AnnotationStatementGenerator generator = generator(context);

            assertThat(generateValue(generator, OrderedFilter.class, "value")).isEqualTo("prefix-value-suffix");
            assertThat(generateValue(generator, RepeatedFilter.class, "value")).isEqualTo("value-x-x");
            assertThat(repeated.calls).isEqualTo(2);
        }
    }

    @Test
    void isolatesBoundPlansAndBeansBetweenApplicationContexts() {
        try (var first = contextWith("transformer", PrefixTransformer.class, new PrefixTransformer("first-"));
             var second = contextWith("transformer", PrefixTransformer.class, new PrefixTransformer("second-"))) {
            AnnotationStatementGenerator firstGenerator = generator(first);
            AnnotationStatementGenerator secondGenerator = generator(second);

            assertThat(generateValue(firstGenerator, IsolatedFilter.class, "value")).isEqualTo("first-value");
            assertThat(generateValue(secondGenerator, IsolatedFilter.class, "value")).isEqualTo("second-value");
            assertThat(firstGenerator).isNotSameAs(secondGenerator);
        }
    }

    @Test
    void performsNoBeanFactoryLookupAfterPlanWarmup() {
        ConfigurableListableBeanFactory beanFactory = Mockito.mock(ConfigurableListableBeanFactory.class);
        PrefixTransformer transformer = new PrefixTransformer("bean-");
        Mockito.when(beanFactory.getBeanNamesForType(PrefixTransformer.class, true, false))
                .thenReturn(new String[]{"transformer"});
        Mockito.when(beanFactory.containsBean("scopedTarget.transformer")).thenReturn(false);
        Mockito.when(beanFactory.isSingleton("transformer")).thenReturn(true);
        Mockito.when(beanFactory.getBean("transformer", PrefixTransformer.class)).thenReturn(transformer);
        AnnotationStatementGenerator generator = new AnnotationStatementGenerator(null,
                new SpringFilterValueTransformerResolver(beanFactory));
        AnnotationStatementInput input = input(IsolatedFilter.class);
        generator.warmup(input);
        Mockito.clearInvocations(beanFactory);

        generator.generateStatements(input, Map.of("value", "one"));
        generator.generateStatements(input, Map.of("value", "two"));

        verifyNoInteractions(beanFactory);
    }

    @Test
    void safelyReusesTheContextBoundSingletonConcurrently() throws Exception {
        try (var context = contextWith("transformer", PrefixTransformer.class, new PrefixTransformer("bean-"))) {
            AnnotationStatementGenerator generator = generator(context);
            AnnotationStatementInput input = input(IsolatedFilter.class);
            generator.warmup(input);

            try (var executor = Executors.newFixedThreadPool(8)) {
                var tasks = IntStream.range(0, 200)
                        .mapToObj(index -> (java.util.concurrent.Callable<Object>) () ->
                                generateValue(generator, IsolatedFilter.class, Integer.toString(index)))
                        .toList();

                var results = executor.invokeAll(tasks);
                for (int i = 0; i < results.size(); i++) {
                    assertThat(results.get(i).get()).isEqualTo("bean-" + i);
                }
            }
        }
    }

    private static AnnotationStatementGenerator generator(AnnotationConfigApplicationContext context) {
        return new AnnotationStatementGenerator(null,
                new SpringFilterValueTransformerResolver(context.getBeanFactory()));
    }

    private static AnnotationConfigApplicationContext contextWith(
            String beanName, Class<PrefixTransformer> beanType, PrefixTransformer transformer) {
        var context = new AnnotationConfigApplicationContext();
        context.registerBean(beanName, beanType, () -> transformer);
        context.refresh();
        return context;
    }

    private static AnnotationStatementInput input(Class<?> filterType) {
        return new AnnotationStatementInput(filterType, null);
    }

    private static Object generateValue(AnnotationStatementGenerator generator, Class<?> filterType, Object value) {
        var wrapper = generator.generateStatements(input(filterType), Map.of("value", value));
        return ((LogicalStatement) wrapper.statement()).getFilterData().values()[0];
    }

    @Conjunction(@Filter(path = "target", parameters = "value", operation = Equals.class,
            transformers = {PrefixTransformer.class, SuffixTransformer.class}))
    private interface OrderedFilter {
    }

    @Conjunction(@Filter(path = "target", parameters = "value", operation = Equals.class,
            transformers = {RepeatedTransformer.class, RepeatedTransformer.class}))
    private interface RepeatedFilter {
    }

    @Conjunction(@Filter(path = "target", parameters = "value", operation = Equals.class,
            transformers = PrefixTransformer.class))
    private interface IsolatedFilter {
    }

    @Conjunction(@Filter(path = "target", parameters = "value", operation = Equals.class,
            targetType = Integer.class, transformers = InjectedTransformer.class))
    private interface InjectedFilter {
    }

    @Controller
    static class MissingTransformerController {
        public void search(
                @Conjunction(@Filter(path = "target", parameters = "value", operation = Equals.class,
                        transformers = MissingTransformer.class, modifiers = ModIgnorePath.class))
                @FilterTarget(Object.class)
                ConditionalStatement filter) {
        }
    }

    @Order(100)
    static class PrefixTransformer implements FilterValueTransformer {
        private final String prefix;

        PrefixTransformer() {
            this("unused-");
        }

        PrefixTransformer(String prefix) {
            this.prefix = prefix;
        }

        @Override
        public Object transform(Object value, FilterValueContext context) {
            return prefix + value;
        }
    }

    @Order(-100)
    static class SuffixTransformer implements FilterValueTransformer {
        @Override
        public Object transform(Object value, FilterValueContext context) {
            return value + "-suffix";
        }
    }

    static class RepeatedTransformer implements FilterValueTransformer {
        private int calls;

        @Override
        public Object transform(Object value, FilterValueContext context) {
            calls++;
            return value + "-x";
        }
    }

    static class ScopedTransformer implements FilterValueTransformer {
        @Override
        public Object transform(Object value, FilterValueContext context) {
            return value;
        }
    }

    static class MissingTransformer implements FilterValueTransformer {
        @Override
        public Object transform(Object value, FilterValueContext context) {
            return value;
        }
    }

    static final class LookupDependency {
        String resolve(String value) {
            return value.equals("alias") ? "42" : value;
        }
    }

    static final class InjectedTransformer implements FilterValueTransformer {
        private final LookupDependency dependency;

        InjectedTransformer(LookupDependency dependency) {
            this.dependency = dependency;
        }

        @Override
        public Object transform(Object value, FilterValueContext context) {
            return dependency.resolve(value.toString());
        }
    }

    @Configuration(proxyBeanMethods = false)
    static class ScopedTransformerConfiguration {
        @Bean
        @Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
        ScopedTransformer scopedTransformer() {
            return new ScopedTransformer();
        }
    }

    @Configuration(proxyBeanMethods = false)
    static class SingletonProxyTransformerConfiguration {
        @Bean
        @Scope(value = BeanDefinition.SCOPE_SINGLETON, proxyMode = ScopedProxyMode.TARGET_CLASS)
        ScopedTransformer scopedTransformer() {
            return new ScopedTransformer();
        }
    }

    @Configuration(proxyBeanMethods = false)
    static class BeanTransformerConfiguration {
        @Bean
        LookupDependency lookupDependency() {
            return new LookupDependency();
        }

        @Bean
        InjectedTransformer injectedTransformer(LookupDependency dependency) {
            return new InjectedTransformer(dependency);
        }
    }
}
