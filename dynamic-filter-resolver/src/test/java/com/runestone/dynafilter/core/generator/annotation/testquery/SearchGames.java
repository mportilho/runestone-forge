/*
 * MIT License
 * <p>
 * Copyright (c) 2023-2022-2023 Marcelo Silva Portilho
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.runestone.dynafilter.core.generator.annotation.testquery;


import com.runestone.dynafilter.core.generator.annotation.Conjunction;
import com.runestone.dynafilter.core.generator.annotation.Filter;
import com.runestone.dynafilter.core.operation.types.Between;
import com.runestone.dynafilter.core.operation.types.Equals;
import com.runestone.dynafilter.core.operation.types.IsIn;
import com.runestone.dynafilter.core.operation.types.Like;

import java.math.BigDecimal;
import java.time.LocalDate;

@Conjunction(value = {
        @Filter(path = "genre", parameters = "genre", operation = Equals.class, targetType = String.class, required = true),
        @Filter(path = "title", parameters = "title", operation = Like.class, targetType = String.class),
        @Filter(path = "specialClient", parameters = "specialClient", operation = Equals.class, targetType = Boolean.class, defaultValues = "${allSpecialClients}"),
        @Filter(path = "dateSearchInterval", parameters = {"minDate", "maxDate"}, operation = Between.class, targetType = LocalDate.class),
        @Filter(path = "priceInterval", parameters = {"minPrice", "maxPrice"}, operation = Between.class, targetType = BigDecimal.class, defaultValues = {"0", "1000"}),
//        @Filter(path = "includePreRelease", parameters = "releaseType", operation = Equals.class, targetType = String.class, constantValues = {"PRE_RELEASE"}, negate = "${hidePreRelease}"),
        @Filter(path = "tags", parameters = "tags", operation = IsIn.class, targetType = String.class)
})
public interface SearchGames {
}
