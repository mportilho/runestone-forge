package com.runestone.dynafilter.core.generator.annotation.testdata.interfaces;

import com.runestone.dynafilter.core.generator.annotation.Conjunction;
import com.runestone.dynafilter.core.generator.annotation.Filter;
import com.runestone.dynafilter.core.generator.annotation.FilterDecorators;
import com.runestone.dynafilter.core.generator.annotation.testdata.MockFilterDecor01;
import com.runestone.dynafilter.core.generator.annotation.testdata.MockFilterDecor02;
import com.runestone.dynafilter.core.operation.types.Decorated;
import com.runestone.dynafilter.core.operation.types.StartsWith;

@Conjunction({
        @Filter(path = "name", parameters = "name", operation = StartsWith.class),
        @Filter(path = "decorValue", parameters = "decorValue", operation = Decorated.class),
})
@FilterDecorators({
        MockFilterDecor01.class,
        MockFilterDecor02.class
})
public interface SearchWithFilters {
}
