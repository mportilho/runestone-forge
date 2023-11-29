package com.runestone.dynafilter.core.generator.annotation.testquery;

import com.runestone.dynafilter.core.generator.annotation.Conjunction;
import com.runestone.dynafilter.core.generator.annotation.Filter;
import com.runestone.dynafilter.core.generator.annotation.Statement;
import com.runestone.dynafilter.core.operation.types.Equals;
import com.runestone.dynafilter.core.operation.types.StartsWith;

@Conjunction(value = {
        @Filter(path = "name", parameters = "name", operation = StartsWith.class, targetType = String.class),
        @Filter(path = "documentNumber", parameters = "documentNumber", operation = Equals.class, targetType = String.class)
}, disjunctions = {
        @Statement({
                @Filter(path = "all", parameters = "all", operation = Equals.class, targetType = Boolean.class),
                @Filter(path = "deleted", parameters = "deleted", operation = Equals.class, targetType = Boolean.class),
        })
})
public interface SearchPeopleAndGames extends SearchGames {
}
