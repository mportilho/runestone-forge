package com.runestone.expeval_mk3.internal.semantics;

import com.runestone.expeval_mk3.api.ExpressionType;
import com.runestone.expeval_mk3.api.RuntimeNullability;

import java.util.Objects;

/**
 * Selects which contextual Item Atual member a {@code PropertyNavigationLink} reads: a map-entry
 * key/value or a reduction accumulator/item. Runtime switches on {@link #member()} instead of
 * inspecting the receiver's runtime type.
 */
public record ContextualMemberNavigationBinding(
        Member member,
        ExpressionType resultType,
        RuntimeNullability resultNullability,
        boolean pure) implements NavigationBinding {

    public ContextualMemberNavigationBinding {
        Objects.requireNonNull(member, "member");
        Objects.requireNonNull(resultType, "resultType");
        Objects.requireNonNull(resultNullability, "resultNullability");
    }

    public enum Member {
        MAP_ENTRY_KEY,
        MAP_ENTRY_VALUE,
        REDUCTION_ACCUMULATOR,
        REDUCTION_ITEM
    }
}
