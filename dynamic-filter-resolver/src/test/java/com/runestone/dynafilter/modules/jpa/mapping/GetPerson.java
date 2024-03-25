package com.runestone.dynafilter.modules.jpa.mapping;

import com.runestone.dynafilter.core.generator.annotation.Filter;
import com.runestone.dynafilter.core.operation.types.Dynamic;
import com.runestone.dynafilter.core.operation.types.StartsWith;

import java.time.LocalDate;

public record GetPerson(

        @Filter(path = "name", parameters = "name", operation = StartsWith.class)
        String name,

        @Filter(path = "birthday", parameters = "birthday", operation = Dynamic.class)
        LocalDate birthday

) {
}
