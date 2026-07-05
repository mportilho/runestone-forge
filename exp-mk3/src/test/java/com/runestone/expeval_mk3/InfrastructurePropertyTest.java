package com.runestone.expeval_mk3;

import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import org.assertj.core.api.Assertions;

class InfrastructurePropertyTest {

    @Property
    void integerAdditionIsCommutative(@ForAll int left, @ForAll int right) {
        Assertions.assertThat(left + right).isEqualTo(right + left);
    }
}
