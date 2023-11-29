package com.runestone.dynafilter.core.generator.annotation.testdata.interfaces;

import com.runestone.dynafilter.core.generator.annotation.Disjunction;
import com.runestone.dynafilter.core.generator.annotation.Filter;
import com.runestone.dynafilter.core.generator.annotation.Statement;
import com.runestone.dynafilter.core.generator.annotation.testdata.annotations.StatusOkNoDeleteCombined;
import com.runestone.dynafilter.core.operation.types.Equals;

@StatusOkNoDeleteCombined
@Disjunction(value = {
        @Filter(path = "author", parameters = "author", operation = Equals.class),
        @Filter(path = "genre", parameters = "genre", operation = Equals.class),
}, conjunctions = {
        @Statement({
                @Filter(path = "specialClient", parameters = "specialClient", operation = Equals.class),
                @Filter(path = "price", parameters = "priceInterval", operation = Equals.class),
        }),
        @Statement({
                @Filter(path = "onSale", parameters = "onSale", operation = Equals.class),
                @Filter(path = "newRelease", parameters = "newRelease", operation = Equals.class),
        }),
})
public interface CombinedAnnotations {
}
