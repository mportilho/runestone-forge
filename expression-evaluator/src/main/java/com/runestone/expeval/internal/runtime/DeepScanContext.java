package com.runestone.expeval.internal.runtime;

import java.util.*;

/**
 * Per-thread reusable context for deep scans, eliminating allocations of {@code ArrayList},
 * {@code IdentityHashMap}, and {@code ArrayDeque} per invocation. Structures are cleared
 * before each use but never deallocated.
 */
final class DeepScanContext {

    static final ThreadLocal<DeepScanContext> INSTANCE =
            ThreadLocal.withInitial(DeepScanContext::new);

    final List<Object> results = new ArrayList<>(16);
    final Set<Object> visited = Collections.newSetFromMap(new IdentityHashMap<>());
    final Deque<Object> queue = new ArrayDeque<>();

    private DeepScanContext() {}
}
