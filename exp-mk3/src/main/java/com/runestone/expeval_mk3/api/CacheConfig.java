package com.runestone.expeval_mk3.api;

import java.time.Duration;
import java.util.Objects;

/**
 * Immutable capacity, retained-weight, and expiration policy for an {@link ExpressionEngine}'s compilation cache.
 * {@link #defaults()} limits the cache to 1024 {@link ExpressionCompilationResult}s, 64 Mi retained-weight
 * units, and no expiration; {@link #builder()} lets callers pick different limits and optional access-based
 * expiration. Retained-weight units are a conservative cache-admission estimate calibrated against JVM
 * layouts, not an exact heap-byte measurement.
 */
public final class CacheConfig {

    private static final int DEFAULT_MAXIMUM_ENTRIES = 1024;
    private static final int MAXIMUM_ENTRIES_CEILING = 65_536;
    private static final long DEFAULT_MAXIMUM_RETAINED_WEIGHT = 64L * 1024 * 1024;
    private static final long MAXIMUM_RETAINED_WEIGHT_CEILING = 1024L * 1024 * 1024;
    private static final CacheConfig DEFAULTS = new CacheConfig(
            DEFAULT_MAXIMUM_ENTRIES, DEFAULT_MAXIMUM_RETAINED_WEIGHT, null);

    private final int maximumEntries;
    private final long maximumRetainedWeight;
    private final Duration expireAfterAccess;

    private CacheConfig(int maximumEntries, long maximumRetainedWeight, Duration expireAfterAccess) {
        this.maximumEntries = maximumEntries;
        this.maximumRetainedWeight = maximumRetainedWeight;
        this.expireAfterAccess = expireAfterAccess;
    }

    public static CacheConfig defaults() {
        return DEFAULTS;
    }

    public static Builder builder() {
        return new Builder();
    }

    public int maximumEntries() {
        return maximumEntries;
    }

    public long maximumRetainedWeight() {
        return maximumRetainedWeight;
    }

    public boolean hasExpireAfterAccess() {
        return expireAfterAccess != null;
    }

    /**
     * @throws IllegalStateException if no expiration was configured; check {@link #hasExpireAfterAccess()} first
     */
    public Duration expireAfterAccess() {
        if (expireAfterAccess == null) {
            throw new IllegalStateException(
                    "no expireAfterAccess configured; check hasExpireAfterAccess() first");
        }
        return expireAfterAccess;
    }

    public static final class Builder {

        private int maximumEntries = DEFAULT_MAXIMUM_ENTRIES;
        private long maximumRetainedWeight = DEFAULT_MAXIMUM_RETAINED_WEIGHT;
        private Duration expireAfterAccess;

        private Builder() {
        }

        public Builder maximumEntries(int maximumEntries) {
            if (maximumEntries <= 0 || maximumEntries > MAXIMUM_ENTRIES_CEILING) {
                throw new IllegalArgumentException("maximumEntries must be between 1 and "
                        + MAXIMUM_ENTRIES_CEILING + ": " + maximumEntries);
            }
            this.maximumEntries = maximumEntries;
            return this;
        }

        public Builder maximumRetainedWeight(long maximumRetainedWeight) {
            if (maximumRetainedWeight <= 0 || maximumRetainedWeight > MAXIMUM_RETAINED_WEIGHT_CEILING) {
                throw new IllegalArgumentException("maximumRetainedWeight must be between 1 and "
                        + MAXIMUM_RETAINED_WEIGHT_CEILING + ": " + maximumRetainedWeight);
            }
            this.maximumRetainedWeight = maximumRetainedWeight;
            return this;
        }

        public Builder expireAfterAccess(Duration expireAfterAccess) {
            Objects.requireNonNull(expireAfterAccess, "expireAfterAccess");
            if (expireAfterAccess.isZero() || expireAfterAccess.isNegative()) {
                throw new IllegalArgumentException("expireAfterAccess must be positive: " + expireAfterAccess);
            }
            this.expireAfterAccess = expireAfterAccess;
            return this;
        }

        public CacheConfig build() {
            return new CacheConfig(maximumEntries, maximumRetainedWeight, expireAfterAccess);
        }
    }
}
