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

package com.runestone.dynafilter.core.generator.annotation;

import com.runestone.dynafilter.core.generator.StatementWrapper;
import com.runestone.dynafilter.core.model.FilterData;
import com.runestone.dynafilter.core.model.statement.LogicalStatement;
import com.runestone.dynafilter.core.model.statement.NoOpStatement;
import com.runestone.dynafilter.core.operation.FilterOperation;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

public class TestAnnotationStatementGeneratorWithCustomOperations {

    @Test
    @DisplayName("AnnotationStatementGenerator preserves custom operation types in FilterData")
    public void testCustomOperationIsPreservedInFilterData() {
        AnnotationStatementInput input = new AnnotationStatementInput(SearchWithCustomOperation.class, null);
        AnnotationStatementGenerator generator = new AnnotationStatementGenerator();

        StatementWrapper statementWrapper = generator.generateStatements(input, Map.of("q", "needle"));

        Assertions.assertThat(statementWrapper.statement()).isInstanceOf(LogicalStatement.class);
        LogicalStatement statement = (LogicalStatement) statementWrapper.statement();
        FilterData filterData = statement.getFilterData();
        Assertions.assertThat(filterData.path()).containsExactly("description");
        Assertions.assertThat(filterData.parameters()).containsExactly("q");
        Assertions.assertThat(filterData.operation()).isEqualTo(CustomOperation.class);
        Assertions.assertThat(filterData.values()).containsExactly("needle");
    }

    @Test
    @DisplayName("AnnotationStatementGenerator ignores absent IsFimVigente custom parameters")
    public void testAbsentIsFimVigenteParameterIsIgnored() {
        AnnotationStatementInput input = new AnnotationStatementInput(SearchWithIsFimVigente.class, null);
        AnnotationStatementGenerator generator = new AnnotationStatementGenerator();

        StatementWrapper statementWrapper = generator.generateStatements(input, Map.of());

        Assertions.assertThat(statementWrapper.statement()).isInstanceOf(NoOpStatement.class);
    }

    @Conjunction(@Filter(path = "description", parameters = "q", operation = CustomOperation.class))
    private interface SearchWithCustomOperation {
    }

    @Conjunction(@Filter(path = "fimVigencia", parameters = "vigente", operation = IsFimVigente.class))
    private interface SearchWithIsFimVigente {
    }

    private interface CustomOperation<T> extends FilterOperation<T> {
    }

    private interface IsFimVigente<T> extends FilterOperation<T> {
    }

}
