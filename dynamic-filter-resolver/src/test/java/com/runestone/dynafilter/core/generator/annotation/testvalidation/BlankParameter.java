package com.runestone.dynafilter.core.generator.annotation.testvalidation;

import com.runestone.dynafilter.core.generator.annotation.Conjunction;
import com.runestone.dynafilter.core.generator.annotation.Filter;
import com.runestone.dynafilter.core.operation.types.Like;

@Conjunction({
        @Filter(path = "genre", parameters = {}, operation = Like.class),
})
public interface BlankParameter {
}
