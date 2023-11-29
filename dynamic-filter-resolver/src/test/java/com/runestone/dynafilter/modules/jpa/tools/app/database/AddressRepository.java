package com.runestone.dynafilter.modules.jpa.tools.app.database;

import com.runestone.dynafilter.modules.jpa.tools.app.database.jpamodels.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface AddressRepository extends JpaRepository<Address, Long>, JpaSpecificationExecutor<Address> {
}
