package com.runestone.dynafilter.modules.jpa.resolver.tools;

import com.runestone.dynafilter.core.generator.annotation.Filter;
import com.runestone.dynafilter.core.generator.annotation.FilterDecorators;
import com.runestone.dynafilter.core.generator.annotation.FilterTarget;
import com.runestone.dynafilter.core.operation.types.Decorated;
import com.runestone.dynafilter.core.operation.types.Dynamic;
import com.runestone.dynafilter.core.operation.types.StartsWith;
import com.runestone.dynafilter.modules.jpa.tools.app.database.jpamodels.Person;

import java.time.LocalDate;

@FilterDecorators({PersonFilterDecorador.class})
@FilterTarget(Person.class)
public record GetPerson(

        @Filter(path = "name", parameters = "name", operation = StartsWith.class)
        String name,

        @Filter(path = "birthday", parameters = "birthday", operation = Dynamic.class)
        LocalDate birthday,

        @Filter(path = "decoratedAttribute", parameters = "decoratedAttribute", operation = Decorated.class)
        String decoratedAttribute

) {
}
