package com.runestone.dynafilter.modules.jpa.spring.tools;

import com.runestone.dynafilter.core.generator.annotation.Conjunction;
import com.runestone.dynafilter.core.generator.annotation.Filter;
import com.runestone.dynafilter.core.operation.types.Between;
import com.runestone.dynafilter.core.operation.types.Like;

@Conjunction({
        @Filter(path = "name", parameters = {"name"}, operation = Like.class),
        @Filter(path = "creationDate", parameters = {"minCreationDate", "maxCreationDate"}, operation = Between.class),
})
public interface SearchLanguages {
}
