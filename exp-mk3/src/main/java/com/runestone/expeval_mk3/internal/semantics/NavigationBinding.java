package com.runestone.expeval_mk3.internal.semantics;

/**
 * The one authoritative execution-selection record for a navigation link, keyed by its {@code NodeId}
 * in {@link SemanticModel#navigationBindings()}. Planning and runtime consume this closed hierarchy
 * instead of rederiving receiver kind, member, subscript kind, or collection operation identity.
 */
public sealed interface NavigationBinding
        permits CollectionOperationBinding, ContextualMemberNavigationBinding, FilterNavigationBinding,
        IndexSubscriptNavigationBinding, MapKeySubscriptNavigationBinding, RegisteredMethodNavigationBinding,
        RegisteredPropertyNavigationBinding, SliceSubscriptNavigationBinding, WildcardNavigationBinding {

    /**
     * Whether this link, together with everything to its left in the chain, is safe to evaluate at
     * plan-build time (ADR 0019, issue #117): declared once per link kind at resolution and consumed,
     * never rediscovered, by navigation-prefix constant folding.
     */
    boolean pure();
}
