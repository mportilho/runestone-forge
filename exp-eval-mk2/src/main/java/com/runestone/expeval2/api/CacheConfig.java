package com.runestone.expeval2.api;

import java.time.Duration;

/**
 * Cache configuration for the expression compiler.
 *
 * <p>Instances are immutable and can be passed to
 * {@link com.runestone.expeval2.internal.runtime.ExpressionRuntimeSupport#configure(CacheConfig)}
 * before the first compilation to override the JVM-wide singleton cache settings.
 *
 * <p>System-property defaults (read at first access):
 * <ul>
 *   <li>{@code expeval.cache.maximumSize} — maximum number of cached entries (default: 1 024)</li>
 *   <li>{@code expeval.cache.ttlSeconds} — time-to-live in seconds; {@code 0} means no TTL (default: 0)</li>
 * </ul>
 *
 * @param maximumSize      maximum number of compiled expressions held in the cache; must be positive
 * @param expireAfterWrite time-to-live per entry; {@code null} disables expiration
 */
public record CacheConfig(long maximumSize, Duration expireAfterWrite) {

    public CacheConfig {
        if (maximumSize <= 0) {
            throw new IllegalArgumentException("maximumSize must be positive, got: " + maximumSize);
        }
    }

    /**
     * Returns a {@code CacheConfig} built from system properties, falling back to built-in defaults
     * when the properties are absent.
     */
    public static CacheConfig defaults() {
        long size = Long.getLong("expeval.cache.maximumSize", 1_024);
        long ttlSeconds = Long.getLong("expeval.cache.ttlSeconds", 0L);
        Duration ttl = ttlSeconds > 0 ? Duration.ofSeconds(ttlSeconds) : null;
        return new CacheConfig(size, ttl);
    }
}
