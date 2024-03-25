package com.runestone.dynafilter.core.generator.annotation.testdata.interfaces;

import com.runestone.dynafilter.core.generator.annotation.ConjunctionFrom;
import com.runestone.dynafilter.core.generator.annotation.StatementFrom;
import com.runestone.dynafilter.core.generator.annotation.testdata.dto.FilterDTO;
import com.runestone.dynafilter.core.generator.annotation.testdata.dto.StmtFilterDTO;

@ConjunctionFrom(value = FilterDTO.class, disjunctions = {@StatementFrom(StmtFilterDTO.class)})
public interface FilterByConjunctionFromInterface {
}
