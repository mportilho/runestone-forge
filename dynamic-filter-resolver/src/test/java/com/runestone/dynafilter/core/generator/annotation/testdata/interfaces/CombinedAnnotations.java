/*
 * MIT License
 * <p>
 * Copyright (c) 2023-2023 Marcelo Silva Portilho
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
