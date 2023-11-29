package com.runestone.dynafilter.core.model;

import com.runestone.dynafilter.core.model.statement.LogicOperator;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestLogicOperator {

    @Test
    public void testLogicOperator() {
        Assertions.assertThat(LogicOperator.CONJUNCTION.isConjunction()).isTrue();
        Assertions.assertThat(LogicOperator.CONJUNCTION.opposite().isConjunction()).isFalse();
    }

}
