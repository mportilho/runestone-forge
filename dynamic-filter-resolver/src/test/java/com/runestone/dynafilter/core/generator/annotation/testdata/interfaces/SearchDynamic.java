package com.runestone.dynafilter.core.generator.annotation.testdata.interfaces;

import com.runestone.dynafilter.core.generator.annotation.Conjunction;
import com.runestone.dynafilter.core.generator.annotation.Filter;
import com.runestone.dynafilter.core.operation.types.Dynamic;
import com.runestone.dynafilter.core.operation.types.StartsWith;

@Conjunction({
        @Filter(path = "name", parameters = {"name"}, operation = StartsWith.class),
        @Filter(path = "age", parameters = {"age"}, operation = Dynamic.class),
})
public interface SearchDynamic {
}
