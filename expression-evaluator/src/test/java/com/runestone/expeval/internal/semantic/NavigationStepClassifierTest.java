package com.runestone.expeval.internal.semantic;

import com.runestone.expeval.internal.ast.PropertyChainNode;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class NavigationStepClassifierTest {

    @Test
    void recognizesLegacyPropertyAndMethodChains() {
        assertThat(NavigationStepClassifier.isLegacyAccessChain(List.of(
                new PropertyChainNode.PropertyAccess("customer"),
                new PropertyChainNode.SafePropertyAccess("address"),
                new PropertyChainNode.MethodCallAccess("total", List.of()),
                new PropertyChainNode.SafeMethodCallAccess("discount", List.of())))).isTrue();
    }

    @Test
    void rejectsCollectionAndMapNavigationAsLegacyChains() {
        assertThat(NavigationStepClassifier.isLegacyAccessChain(List.of(
                new PropertyChainNode.PropertyAccess("items"),
                new PropertyChainNode.WildcardStep()))).isFalse();
    }
}
