package com.runestone.dynafilter.core.resolver;

import com.runestone.dynafilter.core.generator.annotation.AnnotationStatementInput;

public interface FilterDecoratorFactory<T> {

    FilterDecorator<T> createFilterDecorators(AnnotationStatementInput input);

}
