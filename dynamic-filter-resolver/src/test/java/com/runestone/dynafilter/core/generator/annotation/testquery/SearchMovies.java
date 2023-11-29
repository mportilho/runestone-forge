package com.runestone.dynafilter.core.generator.annotation.testquery;

import com.runestone.dynafilter.core.generator.annotation.Conjunction;
import com.runestone.dynafilter.core.generator.annotation.Filter;
import com.runestone.dynafilter.core.generator.annotation.Statement;
import com.runestone.dynafilter.core.operation.types.Equals;
import com.runestone.dynafilter.core.operation.types.IsIn;
import com.runestone.dynafilter.core.operation.types.Like;
import com.runestone.dynafilter.core.operation.types.StartsWith;

@Conjunction(
        value = {
                @Filter(path = "name", parameters = "name", operation = StartsWith.class, targetType = String.class),
                @Filter(path = "genre", parameters = "genre", operation = Equals.class, targetType = String.class)
        },
        disjunctions = {
                @Statement({
                        @Filter(path = "actors", parameters = "actors", operation = IsIn.class, targetType = String.class),
                        @Filter(path = "director", parameters = "director", operation = Like.class, targetType = String.class)
                })

        })
public interface SearchMovies {
}
