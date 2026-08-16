package com.runestone.expeval_mk3.api;

/**
 * Test/benchmark bridge to {@code ExpressionEngine}'s package-private cache accessor, so callers outside
 * the {@code api} package (JMH) can force a miss between paired measurements without a public
 * invalidation operation on {@link ExpressionEngine} itself.
 */
public final class EngineCacheInvalidation {

    private EngineCacheInvalidation() {
    }

    public static void invalidate(ExpressionEngine engine, String source, ExpressionEnvironment environment) {
        engine.cache().invalidate(source, environment);
    }
}
