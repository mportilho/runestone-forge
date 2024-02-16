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

package com.runestone.dynafilter.modules.jpa.resolver;

import com.runestone.dynafilter.core.model.statement.*;
import com.runestone.dynafilter.core.operation.FilterOperationService;
import org.springframework.data.jpa.domain.Specification;

public class SpecificationStatementAnalyser implements StatementAnalyser<Specification<?>> {

    private final FilterOperationService<Specification<?>> filterOperationService;

    public SpecificationStatementAnalyser(FilterOperationService<Specification<?>> filterOperationService) {
        this.filterOperationService = filterOperationService;
    }

    @Override
    public Specification<?> analyseNegationStatement(NegatedStatement negatedStatement) {
        Specification<?> specification = negatedStatement.getStatement().acceptAnalyser(this);
        return Specification.not(specification);
    }

    @Override
    public Specification<?> analyseLogicalStatement(LogicalStatement logicalStatement) {
        return filterOperationService.createFilter(logicalStatement.getFilterData());
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public Specification<?> analyseCompoundStatement(CompoundStatement compoundStatement) {
        Specification leftSpecification = compoundStatement.getLeftStatement().acceptAnalyser(this);
        Specification rightSpecification = compoundStatement.getRightStatement().acceptAnalyser(this);
        return compoundStatement.getLogicOperator().isConjunction() ? leftSpecification.and(rightSpecification) : leftSpecification.or(rightSpecification);
    }

    @Override
    public Specification<?> analyseNoOpStatement(NoOpStatement noOpStatement) {
        return Specification.where(null);
    }
}
