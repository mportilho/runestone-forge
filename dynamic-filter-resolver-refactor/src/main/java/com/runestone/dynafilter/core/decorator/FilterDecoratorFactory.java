package com.runestone.dynafilter.core.decorator;

import com.runestone.dynafilter.core.generator.annotation.AnnotationStatementInput;

public interface FilterDecoratorFactory {

    FilterDecorator<?> createDecorator(AnnotationStatementInput input);
}
