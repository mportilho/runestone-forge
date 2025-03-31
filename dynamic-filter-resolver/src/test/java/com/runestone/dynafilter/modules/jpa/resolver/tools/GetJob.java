package com.runestone.dynafilter.modules.jpa.resolver.tools;

import com.runestone.dynafilter.core.generator.annotation.Conjunction;
import com.runestone.dynafilter.core.generator.annotation.Filter;
import com.runestone.dynafilter.core.operation.types.Decorated;
import com.runestone.dynafilter.core.operation.types.StartsWith;

@Conjunction({
        @Filter(path = "name", parameters = "name", operation = StartsWith.class),
        @Filter(path = "job", parameters = "jobDecorated", operation = Decorated.class)
})
public interface GetJob {
}
