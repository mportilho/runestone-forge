package com.runestone.dynafilter.modules.jpa.spring;

import com.runestone.dynafilter.core.generator.annotation.Filter;
import com.runestone.dynafilter.core.generator.annotation.FilterTarget;
import com.runestone.dynafilter.core.operation.types.Equals;
import com.runestone.dynafilter.modules.jpa.tools.app.database.jpamodels.Person;

@FilterTarget(Person.class)
record DeclaredNameSearch(@Filter(path = "name", parameters = "query", operation = Equals.class) String query) {
}
