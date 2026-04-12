package com.runestone.expeval.internal.navigation;

/**
 * Tracks the navigation mode during property-chain evaluation.
 *
 * <p>Shared between the compilation ({@code ExecutableCollectionFunction.resultMode}) and the runtime
 * ({@code AbstractObjectEvaluator.evaluatePropertyChain}) so that no conversion switch is needed.
 */
public enum NavigationMode {
    /** Scalar value — the current node is a plain object. */
    SCALAR,
    /** Collection mode — the current node is a {@link java.util.List}. */
    COLLECTION,
    /** Map mode — the current node is a {@link java.util.Map}. */
    MAP
}
