package com.runestone.dynafilter.core.transformer;

import com.runestone.dynafilter.core.exceptions.DynamicFilterConfigurationException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TestFilterValueTransformerRegistry {

    @Test
    void publishesAnImmutableResolutionSnapshot() {
        FilterValueTransformerRegistry registry = new FilterValueTransformerRegistry();
        FirstTransformer first = new FirstTransformer();
        registry.register(FirstTransformer.class, first);
        FilterValueTransformerResolver resolver = registry.toResolver();

        registry.register(SecondTransformer.class, new SecondTransformer());

        assertThat(resolver.resolve(FirstTransformer.class)).isSameAs(first);
        assertThatThrownBy(() -> resolver.resolve(SecondTransformer.class))
                .isInstanceOf(DynamicFilterConfigurationException.class)
                .hasMessageContaining(SecondTransformer.class.getCanonicalName());
    }

    @Test
    void rejectsDuplicateTypes() {
        FilterValueTransformerRegistry registry = new FilterValueTransformerRegistry();
        registry.register(FirstTransformer.class, new FirstTransformer());

        assertThatThrownBy(() -> registry.register(FirstTransformer.class, new FirstTransformer()))
                .isInstanceOf(DynamicFilterConfigurationException.class)
                .hasMessageContaining("already registered");
    }

    private static final class FirstTransformer implements FilterValueTransformer {
        @Override
        public Object transform(Object value, FilterValueContext context) {
            return value;
        }
    }

    private static final class SecondTransformer implements FilterValueTransformer {
        @Override
        public Object transform(Object value, FilterValueContext context) {
            return value;
        }
    }
}
