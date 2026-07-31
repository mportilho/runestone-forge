package com.runestone.expeval_mk3.internal.semantics;

import com.runestone.expeval_mk3.internal.ast.CallNavigationLink;
import com.runestone.expeval_mk3.internal.ast.FilterNavigationLink;
import com.runestone.expeval_mk3.internal.ast.IndexSubscriptNavigationLink;
import com.runestone.expeval_mk3.internal.ast.NavigationLink;
import com.runestone.expeval_mk3.internal.ast.PropertyNavigationLink;
import com.runestone.expeval_mk3.internal.ast.SliceSubscriptNavigationLink;
import com.runestone.expeval_mk3.internal.ast.StringKeySubscriptNavigationLink;
import com.runestone.expeval_mk3.internal.ast.WildcardNavigationLink;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The invariant behind issue #91: every accepted {@code NavigationLink} family maps to at least one
 * closed {@link NavigationBinding} leaf, so planning/runtime never rediscover a link's meaning.
 * {@code PropertyNavigationLink} and {@code CallNavigationLink} each resolve to one of two mutually
 * exclusive binding kinds depending on receiver type (contextual vs. registered object, and
 * collection operation vs. registered method); every other link family maps 1:1.
 */
class NavigationBindingCoverageTest {

    private static final Map<Class<? extends NavigationLink>, Set<Class<? extends NavigationBinding>>> EXPECTED_BINDINGS = Map.of(
            IndexSubscriptNavigationLink.class, Set.of(IndexSubscriptNavigationBinding.class),
            SliceSubscriptNavigationLink.class, Set.of(SliceSubscriptNavigationBinding.class),
            StringKeySubscriptNavigationLink.class, Set.of(MapKeySubscriptNavigationBinding.class),
            FilterNavigationLink.class, Set.of(FilterNavigationBinding.class),
            WildcardNavigationLink.class, Set.of(WildcardNavigationBinding.class),
            PropertyNavigationLink.class, Set.of(
                    ContextualMemberNavigationBinding.class, RegisteredPropertyNavigationBinding.class),
            CallNavigationLink.class, Set.of(
                    CollectionOperationBinding.class, RegisteredMethodNavigationBinding.class));

    @Test
    void everyNavigationLinkPermittedSubtypeHasAtLeastOneDeclaredBinding() {
        assertThat(NavigationLink.class.getPermittedSubclasses())
                .as("NavigationLink sealed hierarchy")
                .containsExactlyInAnyOrderElementsOf(EXPECTED_BINDINGS.keySet());
    }

    @Test
    void everyDeclaredBindingBelongsToTheClosedNavigationBindingHierarchy() {
        Set<Class<?>> declaredBindings = EXPECTED_BINDINGS.values().stream()
                .flatMap(Set::stream)
                .collect(java.util.stream.Collectors.toSet());

        assertThat(NavigationBinding.class.getPermittedSubclasses())
                .as("NavigationBinding sealed hierarchy")
                .containsExactlyInAnyOrderElementsOf(declaredBindings);
    }
}
