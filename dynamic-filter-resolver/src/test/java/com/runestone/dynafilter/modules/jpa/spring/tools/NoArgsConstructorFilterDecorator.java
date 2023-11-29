package com.runestone.dynafilter.modules.jpa.spring.tools;

import com.runestone.dynafilter.core.generator.StatementWrapper;
import com.runestone.dynafilter.core.resolver.FilterDecorator;

public class NoArgsConstructorFilterDecorator implements FilterDecorator<String> {

    @Override
    public String decorate(String filter, StatementWrapper statementWrapper) {
        return filter + " decorated with " + getClass().getSimpleName();
    }

}
