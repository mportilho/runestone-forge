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

package com.runestone.dynafilter.core.generator.annotation.tool;

import com.runestone.dynafilter.core.model.statement.*;

public class ValueFinderVisitor implements StatementVisitor {

    private final String parameter;
    private Object[] value = null;

    public ValueFinderVisitor(String parameter) {
        this.parameter = parameter;
    }

    public static Object[] find(String parameter, AbstractStatement statement) {
        ValueFinderVisitor visitor = new ValueFinderVisitor(parameter);
        statement.acceptVisitor(visitor);
        return visitor.getValue();
    }

    @Override
    public void visit(NegatedStatement negatedStatement) {
        negatedStatement.getStatement().acceptVisitor(this);
    }

    @Override
    public void visit(CompoundStatement compoundStatement) {
        compoundStatement.getLeftStatement().acceptVisitor(this);
        compoundStatement.getRightStatement().acceptVisitor(this);
    }

    @Override
    public void visit(LogicalStatement logicalStatement) {
        if (logicalStatement.getFilterData().path().equals(parameter)) {
            value = logicalStatement.getFilterData().values();
        }
    }

    public String getParameter() {
        return parameter;
    }

    public Object[] getValue() {
        return value;
    }
}
