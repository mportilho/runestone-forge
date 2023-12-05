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

import com.runestone.dynafilter.core.generator.StatementWrapper;
import com.runestone.dynafilter.core.operation.FilterOperationService;
import com.runestone.dynafilter.core.resolver.DynamicFilterResolver;
import com.runestone.dynafilter.core.resolver.FilterDecorator;
import org.springframework.data.jpa.domain.Specification;

import java.util.Objects;

public class SpecificationDynamicFilterResolver implements DynamicFilterResolver<Specification<?>> {

    private final SpecificationStatementAnalyser statementAnalyser;

    public SpecificationDynamicFilterResolver(FilterOperationService<Specification<?>> filterOperationService) {
        this.statementAnalyser = new SpecificationStatementAnalyser(filterOperationService);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <R extends Specification<?>> R createFilter(StatementWrapper statementWrapper, FilterDecorator<Specification<?>> decorator) {
        Objects.requireNonNull(statementWrapper, "Statement wrapper cannot be null");
        Specification<?> specification = statementWrapper.statement().acceptAnalyser(statementAnalyser);
        return (R) (decorator != null ? decorator.decorate(specification, statementWrapper) : specification);
    }

}
