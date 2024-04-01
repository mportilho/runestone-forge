package com.runestone.dynafilter.modules.jpa.resolver.tools;

import com.runestone.dynafilter.core.generator.StatementWrapper;
import com.runestone.dynafilter.core.resolver.FilterDecorator;
import com.runestone.dynafilter.modules.jpa.tools.app.database.jpamodels.Person;
import org.springframework.data.jpa.domain.Specification;

public class PersonFilterDecorador implements FilterDecorator<Specification<Person>> {

    @Override
    public Specification<Person> decorate(Specification<Person> filter, StatementWrapper statementWrapper) {
        return filter;
    }

}