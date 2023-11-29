package com.runestone.dynafilter.core.generator.annotation.testquery;

import com.runestone.dynafilter.core.generator.annotation.Conjunction;
import com.runestone.dynafilter.core.generator.annotation.Filter;
import com.runestone.dynafilter.core.model.modifiers.ModIgnoreCase;
import com.runestone.dynafilter.core.operation.types.Equals;
import com.runestone.dynafilter.core.operation.types.Like;

@Conjunction({
        @Filter(path = "title", parameters = {"title"}, operation = Like.class, modifiers = {ModIgnoreCase.class}),
        @Filter(path = "artist", parameters = {"artist"}, operation = Like.class),
        @Filter(path = "onSalePeriod", parameters = {"onSalePeriod"}, operation = Equals.class, constantValues = {"false"}, negate = "${showOnlyOnSale}"),
        @Filter(path = "priceInterval", parameters = {"minPrice", "maxPrice"}, operation = Equals.class, constantValues = {"${minPrice}", "${maxPrice}"})
})
public interface SearchMusic {
}
