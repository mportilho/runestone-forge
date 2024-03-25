package com.runestone.dynafilter.core.generator.annotation.testdata.interfaces;

import com.runestone.dynafilter.core.generator.annotation.DisjunctionFrom;
import com.runestone.dynafilter.core.generator.annotation.StatementFrom;
import com.runestone.dynafilter.core.generator.annotation.testdata.dto.FilterDTO;
import com.runestone.dynafilter.core.generator.annotation.testdata.dto.StmtFilterDTO;

@DisjunctionFrom(value = FilterDTO.class, conjunctions = {@StatementFrom(StmtFilterDTO.class)})
public interface FilterByDisjunctionFromInterface {
}
