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

package com.runestone.dynafilter.core.resolver;

import com.runestone.dynafilter.core.exceptions.StatementGenerationException;
import com.runestone.dynafilter.core.generator.StatementWrapper;
import com.runestone.dynafilter.modules.jpa.tools.MockStatementFactory;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class TestCoreFilterDecorator {

    @Test
    public void testCompositeFilterDecorator() {
        FilterDecorator<String> decorator1 = (s, statement) -> s + "1";
        FilterDecorator<String> decorator2 = (s, statement) -> s + "2";
        FilterDecorator<String> composite = FilterDecorator.of(List.of(decorator1, decorator2));
        String decorated = composite.decorate("Test", new StatementWrapper(MockStatementFactory.createLogicalStatementOnName(), null, null));
        Assertions.assertThat(decorated).isEqualTo("Test12");
    }

    @Test
    public void testCompositeThrowOnReturnNull() {
        FilterDecorator<String> decorator1 = (s, statement) -> s + "1";
        FilterDecorator<String> decorator2 = (s, statement) -> null;
        FilterDecorator<String> composite = FilterDecorator.of(List.of(decorator1, decorator2));
        Assertions.assertThatThrownBy(() -> composite.decorate("Test", new StatementWrapper(MockStatementFactory.createLogicalStatementOnName(), null, null)))
                .isExactlyInstanceOf(StatementGenerationException.class)
                .hasMessageContaining("Filter decorator returned null");
    }

}
