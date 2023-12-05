/*
 * MIT License
 * <p>
 * Copyright (c) 2023-2021-2023 Marcelo Silva Portilho
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
import com.runestone.dynafilter.core.generator.annotation.Statement;
import com.runestone.dynafilter.core.generator.annotation.testdata.StatusEnum;
import com.runestone.dynafilter.core.operation.types.Equals;
import com.runestone.dynafilter.core.operation.types.Greater;
import com.runestone.dynafilter.core.operation.types.GreaterOrEquals;

import java.time.LocalDate;

@Conjunction(
        value = {
                @Filter(path = "deleted", parameters = "delete", operation = Equals.class, constantValues = "false", targetType = Boolean.class),
                @Filter(path = "name", parameters = "name", operation = Equals.class, negate = "true", constantValues = "John", targetType = String.class)
        },
        disjunctions = {
                @Statement({
                        @Filter(path = "status", parameters = "status", operation = Equals.class, constantValues = "OK", targetType = StatusEnum.class)
                }),
                @Statement({
                        @Filter(path = "birthday", parameters = "birthday", operation = GreaterOrEquals.class, constantValues = "12/12/2012", targetType = LocalDate.class),
                        @Filter(path = "height", parameters = "height", operation = Greater.class, constantValues = "170", targetType = StatusEnum.class)
                })
        }
)
public interface FullConstantValuedConjunction {

}
