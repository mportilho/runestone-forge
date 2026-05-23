package com.runestone.expeval.internal.semantic;

import com.runestone.expeval.types.NullType;
import com.runestone.expeval.types.ScalarType;
import com.runestone.expeval.types.UnknownType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SemanticTypeMatcherTest {

    @Test
    void shouldMatchEqualArgumentTypes() {
        assertThat(SemanticTypeMatcher.matchesArguments(
                List.of(ScalarType.NUMBER, ScalarType.STRING),
                List.of(ScalarType.NUMBER, ScalarType.STRING)))
                .isTrue();
    }

    @Test
    void shouldTreatUnknownAndNullAsCompatible() {
        assertThat(SemanticTypeMatcher.matchesArguments(
                List.of(ScalarType.NUMBER, NullType.INSTANCE, ScalarType.STRING),
                List.of(UnknownType.INSTANCE, ScalarType.BOOLEAN, NullType.INSTANCE)))
                .isTrue();
    }

    @Test
    void shouldRejectDifferentKnownTypes() {
        assertThat(SemanticTypeMatcher.matchesArguments(
                List.of(ScalarType.NUMBER),
                List.of(ScalarType.STRING)))
                .isFalse();
    }

    @Test
    void shouldRejectDifferentArity() {
        assertThat(SemanticTypeMatcher.matchesArguments(
                List.of(ScalarType.NUMBER),
                List.of(ScalarType.NUMBER, ScalarType.STRING)))
                .isFalse();
    }
}
