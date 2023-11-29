package com.runestone.dynafilter.core.generator.annotation.testquery;

import com.runestone.dynafilter.core.generator.annotation.Conjunction;
import com.runestone.dynafilter.core.generator.annotation.Filter;
import com.runestone.dynafilter.core.operation.types.Equals;
import com.runestone.dynafilter.core.operation.types.StartsWith;

@Conjunction(value = {
        @Filter(path = "name", parameters = "name", operation = StartsWith.class, targetType = String.class),
        @Filter(path = "documentNumber", parameters = "documentNumber", operation = Equals.class, targetType = String.class)
})
public interface SearchPeople {
}
