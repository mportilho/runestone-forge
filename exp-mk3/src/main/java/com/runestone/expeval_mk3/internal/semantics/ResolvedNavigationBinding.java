package com.runestone.expeval_mk3.internal.semantics;

import com.runestone.expeval_mk3.internal.ast.NavigationSafety;

import java.util.Objects;

public record ResolvedNavigationBinding(
        NavigationBindingKind kind,
        NavigationBindingTarget target,
        NavigationSafety safety,
        NavigationBindingDetail detail) {

    public ResolvedNavigationBinding {
        Objects.requireNonNull(kind, "kind");
        Objects.requireNonNull(target, "target");
        Objects.requireNonNull(safety, "safety");
        Objects.requireNonNull(detail, "detail");
    }
}
