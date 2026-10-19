package com.runestone.dynafilter.modules.jpa.spring;

import com.runestone.dynafilter.core.generator.annotation.Filter;
import com.runestone.dynafilter.core.generator.annotation.FilterTarget;
import com.runestone.dynafilter.core.operation.types.Equals;
import com.runestone.dynafilter.modules.jpa.tools.app.database.jpamodels.Person;

@FilterTarget(Person.class)
record DeclaredIdentifierSearch(@Filter(path = "id", parameters = "identifier", operation = Equals.class) Long identifier) {
}
