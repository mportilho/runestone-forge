package com.runestone.dynafilter.core.generator.annotation.testdata;

import com.runestone.dynafilter.core.generator.StatementWrapper;
import com.runestone.dynafilter.core.resolver.FilterDecorator;

public class MockFilterDecor01 implements FilterDecorator<String> {

    @Override
    public String decorate(String filter, StatementWrapper statementWrapper) {
        return filter + "-01";
    }

}
