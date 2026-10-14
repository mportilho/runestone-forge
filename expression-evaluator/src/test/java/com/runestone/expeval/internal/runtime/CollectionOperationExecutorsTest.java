package com.runestone.expeval.internal.runtime;

import com.runestone.expeval.api.CollectionOperationCatalog.OperationIdentity;
import com.runestone.expeval.internal.semantics.CollectionOperationWiring;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

class CollectionOperationExecutorsTest {

    @Test
    void resolvesAnExecutorForEveryRuntimeWiredIdentity() {
        for (OperationIdentity identity : OperationIdentity.values()) {
            if (!CollectionOperationWiring.runtimeWired(identity)) {
                continue;
            }
            assertThat(CollectionOperationExecutors.executorFor(identity)).as(identity.name()).isNotNull();
        }
    }

    @Test
    void rejectsAnIdentityWithoutARuntimeExecutorInsteadOfSilentlyDispatching() {
        assertThat(CollectionOperationWiring.runtimeWired(OperationIdentity.CUSTOM)).isFalse();
        assertThatIllegalStateException()
                .isThrownBy(() -> CollectionOperationExecutors.executorFor(OperationIdentity.CUSTOM))
                .withMessageContaining("CUSTOM");
    }
}
