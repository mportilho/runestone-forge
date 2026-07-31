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
}
