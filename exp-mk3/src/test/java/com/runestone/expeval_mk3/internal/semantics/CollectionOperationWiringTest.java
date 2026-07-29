package com.runestone.expeval_mk3.internal.semantics;

import com.runestone.expeval_mk3.api.CollectionOperationCatalog.OperationIdentity;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class CollectionOperationWiringTest {

    @Test
    void everyCatalogIdentityHasAnExplicitSemanticsAndRuntimeAnswer() {
        for (OperationIdentity identity : OperationIdentity.values()) {
            assertThatCode(() -> CollectionOperationWiring.semanticsSupported(identity))
                    .as("semantics wiring for %s", identity)
                    .doesNotThrowAnyException();
            assertThatCode(() -> CollectionOperationWiring.runtimeWired(identity))
                    .as("runtime wiring for %s", identity)
                    .doesNotThrowAnyException();
        }
    }

    @Test
    void standardIdentitiesAreSupportedByBothLayers() {
        for (OperationIdentity identity : OperationIdentity.values()) {
            if (identity == OperationIdentity.CUSTOM) {
                continue;
            }
            assertThat(CollectionOperationWiring.semanticsSupported(identity)).as(identity.name()).isTrue();
            assertThat(CollectionOperationWiring.runtimeWired(identity)).as(identity.name()).isTrue();
        }
    }

    @Test
    void customIsAnExplicitNotYetWiredMarkerRatherThanASilentGap() {
        assertThat(CollectionOperationWiring.semanticsSupported(OperationIdentity.CUSTOM)).isFalse();
        assertThat(CollectionOperationWiring.runtimeWired(OperationIdentity.CUSTOM)).isFalse();
    }

    @Test
    void averageOfAnEmptyCollectionIsUndefined() {
        assertThat(CollectionOperationWiring.isAverageOfEmptyCollectionUndefined(0)).isTrue();
        assertThat(CollectionOperationWiring.isAverageOfEmptyCollectionUndefined(1)).isFalse();
    }
}
