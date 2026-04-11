package com.runestone.expeval.environment;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.MathContext;

import static org.assertj.core.api.Assertions.assertThat;

class ExpressionEnvironmentIdDerivationTest {

    // --- Fixtures ---

    static final class FixtureProvider {
        public BigDecimal noop(BigDecimal x) {
            return x;
        }
    }

    // --- Tests ---

    @Nested
    @DisplayName("Equivalent configurations produce the same ID")
    class EquivalentConfigurations {

        @Test
        @DisplayName("Empty environments share ID")
        void emptyEnvironmentsShareId() {
            ExpressionEnvironmentId id1 = ExpressionEnvironment.builder().build().environmentId();
            ExpressionEnvironmentId id2 = ExpressionEnvironment.builder().build().environmentId();

            assertThat(id1).isEqualTo(id2);
        }

        @Test
        @DisplayName("Same static provider built independently shares ID")
        void sameStaticProviderSharesId() {
            ExpressionEnvironmentId id1 = ExpressionEnvironment.builder().addMathFunctions().build().environmentId();
            ExpressionEnvironmentId id2 = ExpressionEnvironment.builder().addMathFunctions().build().environmentId();

            assertThat(id1).isEqualTo(id2);
        }

        @Test
        @DisplayName("Same external symbol registration shares ID")
        void sameExternalSymbolSharesId() {
            ExpressionEnvironmentId id1 = ExpressionEnvironment.builder()
                    .registerExternalSymbol("rate", BigDecimal.ZERO, true)
                    .build().environmentId();
            ExpressionEnvironmentId id2 = ExpressionEnvironment.builder()
                    .registerExternalSymbol("rate", BigDecimal.ZERO, true)
                    .build().environmentId();

            assertThat(id1).isEqualTo(id2);
        }

        @Test
        @DisplayName("Same MathContext shares ID")
        void sameMathContextSharesId() {
            ExpressionEnvironmentId id1 = ExpressionEnvironment.builder()
                    .withMathContext(MathContext.DECIMAL64).build().environmentId();
            ExpressionEnvironmentId id2 = ExpressionEnvironment.builder()
                    .withMathContext(MathContext.DECIMAL64).build().environmentId();

            assertThat(id1).isEqualTo(id2);
        }

        @Test
        @DisplayName("Reusing the same instance in different builders shares ID")
        void sameInstanceReusedInDifferentBuildersSharesId() {
            FixtureProvider instance = new FixtureProvider();
            ExpressionEnvironmentId id1 = ExpressionEnvironment.builder()
                    .registerInstanceProvider(instance).build().environmentId();
            ExpressionEnvironmentId id2 = ExpressionEnvironment.builder()
                    .registerInstanceProvider(instance).build().environmentId();

            assertThat(id1).isEqualTo(id2);
        }

        @Test
        @DisplayName("Different defaultValue for same symbol shares ID — defaultValue is excluded from the hash")
        void differentDefaultValueForSameSymbolSharesId() {
            // defaultValue affects runtime evaluation, not compilation — intentionally excluded from the cache key
            ExpressionEnvironmentId id1 = ExpressionEnvironment.builder()
                    .registerExternalSymbol("rate", BigDecimal.ZERO, true)
                    .build().environmentId();
            ExpressionEnvironmentId id2 = ExpressionEnvironment.builder()
                    .registerExternalSymbol("rate", BigDecimal.TEN, true)
                    .build().environmentId();

            assertThat(id1).isEqualTo(id2);
        }
    }

    @Nested
    @DisplayName("Registration order does not affect ID")
    class RegistrationOrderIndependence {

        @Test
        @DisplayName("Static providers in different order produce same ID")
        void staticProvidersInDifferentOrderShareId() {
            ExpressionEnvironmentId id1 = ExpressionEnvironment.builder()
                    .addMathFunctions().addTrigonometryFunctions().build().environmentId();
            ExpressionEnvironmentId id2 = ExpressionEnvironment.builder()
                    .addTrigonometryFunctions().addMathFunctions().build().environmentId();

            assertThat(id1).isEqualTo(id2);
        }

        @Test
        @DisplayName("External symbols in different order produce same ID")
        void externalSymbolsInDifferentOrderShareId() {
            ExpressionEnvironmentId id1 = ExpressionEnvironment.builder()
                    .registerExternalSymbol("rate", BigDecimal.ZERO, true)
                    .registerExternalSymbol("cap", BigDecimal.ZERO, false)
                    .build().environmentId();
            ExpressionEnvironmentId id2 = ExpressionEnvironment.builder()
                    .registerExternalSymbol("cap", BigDecimal.ZERO, false)
                    .registerExternalSymbol("rate", BigDecimal.ZERO, true)
                    .build().environmentId();

            assertThat(id1).isEqualTo(id2);
        }
    }

    @Nested
    @DisplayName("Different configurations produce different IDs")
    class DifferentConfigurations {

        @Test
        @DisplayName("Different static providers produce different IDs")
        void differentStaticProvidersDifferentIds() {
            ExpressionEnvironmentId id1 = ExpressionEnvironment.builder().addMathFunctions().build().environmentId();
            ExpressionEnvironmentId id2 = ExpressionEnvironment.builder().addTrigonometryFunctions().build().environmentId();

            assertThat(id1).isNotEqualTo(id2);
        }

        @Test
        @DisplayName("Different MathContext produces different IDs")
        void differentMathContextsDifferentIds() {
            ExpressionEnvironmentId id1 = ExpressionEnvironment.builder()
                    .withMathContext(MathContext.DECIMAL128).build().environmentId();
            ExpressionEnvironmentId id2 = ExpressionEnvironment.builder()
                    .withMathContext(MathContext.DECIMAL64).build().environmentId();

            assertThat(id1).isNotEqualTo(id2);
        }

        @Test
        @DisplayName("Different external symbol names produce different IDs")
        void differentSymbolNamesDifferentIds() {
            ExpressionEnvironmentId id1 = ExpressionEnvironment.builder()
                    .registerExternalSymbol("rate", BigDecimal.ZERO, true).build().environmentId();
            ExpressionEnvironmentId id2 = ExpressionEnvironment.builder()
                    .registerExternalSymbol("cap", BigDecimal.ZERO, true).build().environmentId();

            assertThat(id1).isNotEqualTo(id2);
        }

        @Test
        @DisplayName("Different external symbol types produce different IDs")
        void differentSymbolTypesDifferentIds() {
            ExpressionEnvironmentId id1 = ExpressionEnvironment.builder()
                    .registerExternalSymbol("flag", BigDecimal.ZERO, true).build().environmentId();
            ExpressionEnvironmentId id2 = ExpressionEnvironment.builder()
                    .registerExternalSymbol("flag", Boolean.TRUE, true).build().environmentId();

            assertThat(id1).isNotEqualTo(id2);
        }

        @Test
        @DisplayName("Different overridable flag produces different IDs")
        void differentOverridableFlagDifferentIds() {
            ExpressionEnvironmentId id1 = ExpressionEnvironment.builder()
                    .registerExternalSymbol("rate", BigDecimal.ZERO, true).build().environmentId();
            ExpressionEnvironmentId id2 = ExpressionEnvironment.builder()
                    .registerExternalSymbol("rate", BigDecimal.ZERO, false).build().environmentId();

            assertThat(id1).isNotEqualTo(id2);
        }

        @Test
        @DisplayName("Different instances of same class produce different IDs")
        void differentInstancesDifferentIds() {
            ExpressionEnvironmentId id1 = ExpressionEnvironment.builder()
                    .registerInstanceProvider(new FixtureProvider()).build().environmentId();
            ExpressionEnvironmentId id2 = ExpressionEnvironment.builder()
                    .registerInstanceProvider(new FixtureProvider()).build().environmentId();

            assertThat(id1).isNotEqualTo(id2);
        }

        @Test
        @DisplayName("Extra symbol compared to empty environment produces different ID")
        void extraSymbolDifferentIds() {
            ExpressionEnvironmentId id1 = ExpressionEnvironment.builder().build().environmentId();
            ExpressionEnvironmentId id2 = ExpressionEnvironment.builder()
                    .registerExternalSymbol("rate", BigDecimal.ZERO, true).build().environmentId();

            assertThat(id1).isNotEqualTo(id2);
        }
    }

    @Nested
    @DisplayName("ID format")
    class IdFormat {

        @Test
        @DisplayName("ID is a non-blank lowercase hex string of 16 characters")
        void idIsNonBlankHexString() {
            String id = ExpressionEnvironment.builder().build().environmentId().value();

            assertThat(id)
                    .isNotBlank()
                    .hasSize(16)
                    .matches("[0-9a-f]+");
        }
    }
}
