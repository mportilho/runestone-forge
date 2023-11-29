package com.runestone.dynafilter.modules.jpa.resolver.tools;

import com.runestone.dynafilter.core.generator.annotation.Conjunction;
import com.runestone.dynafilter.core.generator.annotation.Filter;
import com.runestone.dynafilter.core.operation.types.StartsWith;
import com.runestone.dynafilter.modules.jpa.resolver.Fetching;

@Conjunction({
        @Filter(path = "name", parameters = "name", operation = StartsWith.class)
})
@Fetching("addresses")
public interface SearchEmployees {
}
