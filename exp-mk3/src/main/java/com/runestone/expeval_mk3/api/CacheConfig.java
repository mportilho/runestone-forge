package com.runestone.expeval_mk3.api;

import java.time.Duration;
import java.util.Objects;

/**
 * Immutable capacity and expiration policy for an {@link ExpressionEngine}'s compilation cache.
 * {@link #defaults()} limits the cache to 1024 {@link ExpressionCompilationResult}s with no expiration;
 * {@link #builder()} lets callers pick a different capacity and an optional access-based expiration.
 */
public final class CacheConfig {

    private static final int DEFAULT_MAXIMUM_ENTRIES = 1024;
    private static final CacheConfig DEFAULTS = new CacheConfig(DEFAULT_MAXIMUM_ENTRIES, null);

    private final int maximumEntries;
    private final Duration expireAfterAccess;

    private CacheConfig(int maximumEntries, Duration expireAfterAccess) {
        this.maximumEntries = maximumEntries;
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
        private Duration expireAfterAccess;

        private Builder() {
        }

        public Builder maximumEntries(int maximumEntries) {
            if (maximumEntries <= 0) {
                throw new IllegalArgumentException("maximumEntries must be positive: " + maximumEntries);
            }
            this.maximumEntries = maximumEntries;
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
            return new CacheConfig(maximumEntries, expireAfterAccess);
        }
    }
}
