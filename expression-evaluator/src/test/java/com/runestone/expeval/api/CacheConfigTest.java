package com.runestone.expeval.api;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("CacheConfig")
class CacheConfigTest {

    // --- Happy path ---

    @Test
    @DisplayName("accepts valid maximumSize and null TTL")
    void acceptsValidMaximumSizeAndNullTtl() {
        var config = new CacheConfig(512, null);

        assertThat(config.maximumSize()).isEqualTo(512);
        assertThat(config.expireAfterWrite()).isNull();
    }

    @Test
    @DisplayName("accepts maximumSize of one (minimum valid)")
    void acceptsMinimumMaximumSizeOfOne() {
        var config = new CacheConfig(1, null);

        assertThat(config.maximumSize()).isEqualTo(1);
    }

    @Test
    @DisplayName("accepts explicit TTL duration")
    void acceptsExplicitTtlDuration() {
        var config = new CacheConfig(1_024, Duration.ofHours(1));

        assertThat(config.expireAfterWrite()).isEqualTo(Duration.ofHours(1));
    }

    // --- Validation ---

    @Test
    @DisplayName("rejects maximumSize of zero")
    void rejectsZeroMaximumSize() {
        assertThatThrownBy(() -> new CacheConfig(0, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("maximumSize");
    }

    @Test
    @DisplayName("rejects negative maximumSize")
    void rejectsNegativeMaximumSize() {
        assertThatThrownBy(() -> new CacheConfig(-1, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("maximumSize");
    }

    // --- defaults() without system properties ---

    @Nested
    @DisplayName("defaults() without system properties")
    class Defaults {

        @BeforeEach
        void clearProperties() {
            System.clearProperty("expeval.cache.maximumSize");
            System.clearProperty("expeval.cache.ttlSeconds");
        }

        @AfterEach
        void restoreProperties() {
            System.clearProperty("expeval.cache.maximumSize");
            System.clearProperty("expeval.cache.ttlSeconds");
        }

        @Test
        @DisplayName("returns maximumSize of 1024")
        void defaultMaximumSizeIs1024() {
            assertThat(CacheConfig.defaults().maximumSize()).isEqualTo(1_024);
        }

        @Test
        @DisplayName("returns null TTL (no expiration)")
        void defaultTtlIsNull() {
            assertThat(CacheConfig.defaults().expireAfterWrite()).isNull();
        }
    }

    // --- defaults() with system properties ---

    @Nested
    @DisplayName("defaults() with system properties")
    class SystemPropertyOverrides {

        @AfterEach
        void restoreProperties() {
            System.clearProperty("expeval.cache.maximumSize");
            System.clearProperty("expeval.cache.ttlSeconds");
        }

        @Test
        @DisplayName("reads maximumSize from system property")
        void readMaximumSizeFromSystemProperty() {
            System.setProperty("expeval.cache.maximumSize", "2048");

            assertThat(CacheConfig.defaults().maximumSize()).isEqualTo(2_048);
        }

        @Test
        @DisplayName("reads TTL in seconds from system property")
        void readTtlSecondsFromSystemProperty() {
            System.setProperty("expeval.cache.ttlSeconds", "3600");

            assertThat(CacheConfig.defaults().expireAfterWrite()).isEqualTo(Duration.ofHours(1));
        }

        @Test
        @DisplayName("ttlSeconds of zero produces null TTL")
        void zeroTtlSecondsProducesNullDuration() {
            System.setProperty("expeval.cache.ttlSeconds", "0");

            assertThat(CacheConfig.defaults().expireAfterWrite()).isNull();
        }
    }
}
