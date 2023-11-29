package com.runestone.dynafilter.core.generator.annotation;

import com.runestone.dynafilter.core.generator.annotation.testdata.interfaces.NoDeleteInterface;
import com.runestone.dynafilter.core.generator.annotation.testdata.interfaces.NoDeleteStatusOKExtended;
import com.runestone.dynafilter.core.generator.annotation.testdata.interfaces.StatusOkInterface;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestTypeAnnotationUtilsInterfaceExtraction {

    @Test
    public void testNullValuedInput() {
        var list = TypeAnnotationUtils.extractProcessableInterfaces(null);
        Assertions.assertThat(list).isEmpty();
    }

    @Test
    public void testOneInterface() {
        var list = TypeAnnotationUtils.extractProcessableInterfaces(StatusOkInterface.class);
        Assertions.assertThat(list).contains(StatusOkInterface.class);
    }

    @Test
    public void testTwoInterfaces() {
        var list = TypeAnnotationUtils.extractProcessableInterfaces(NoDeleteStatusOKExtended.class);
        Assertions.assertThat(list).hasSize(3).containsExactlyInAnyOrder(NoDeleteInterface.class, StatusOkInterface.class, NoDeleteStatusOKExtended.class);
    }

}
