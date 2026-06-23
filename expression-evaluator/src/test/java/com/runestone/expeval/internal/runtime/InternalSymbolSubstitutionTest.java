package com.runestone.expeval.internal.runtime;

import com.runestone.expeval.api.MathExpression;
import com.runestone.expeval.environment.ExpressionEnvironment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Internal symbols substitution check")
class InternalSymbolSubstitutionTest {

    private static final ExpressionEnvironment ENV = ExpressionEnvironment.builder().build();

    @Test
    @DisplayName("User cannot override an assigned (internal) variable")
    void cannotOverrideInternalVariable() {
        // 'x' is an internal variable assigned within the expression
        MathExpression math = MathExpression.compile("x = 10; x + 5", ENV);

        // Attempting to provide 'x' from the outside should fail
        assertThatThrownBy(() -> math.compute(Map.of("x", 20)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("symbol 'x' is internal to the expression");
    }

    @Test
    @DisplayName("User can provide external variables that are not assigned")
    void canProvideExternalVariable() {
        MathExpression math = MathExpression.compile("x + 5", ENV);
        
        // This should work as 'x' is external (not assigned)
        math.compute(Map.of("x", 10));
    }
}
