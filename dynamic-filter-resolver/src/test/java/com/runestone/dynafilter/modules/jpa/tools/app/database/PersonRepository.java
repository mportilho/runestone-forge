package com.runestone.dynafilter.modules.jpa.tools.app.database;

import com.runestone.dynafilter.modules.jpa.tools.app.database.jpamodels.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface PersonRepository extends JpaRepository<Person, Long>, JpaSpecificationExecutor<Person> {
}
