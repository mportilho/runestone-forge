package com.runestone.expeval.internal.semantic;

import java.util.Map;
import java.util.Objects;

public record SemanticResolution(
        SemanticModel model,
        Map<MemberBindingKey, ResolvedMemberBinding> memberBindings
) {

    public SemanticResolution {
        Objects.requireNonNull(model, "model must not be null");
        Objects.requireNonNull(memberBindings, "memberBindings must not be null");
    }
}
