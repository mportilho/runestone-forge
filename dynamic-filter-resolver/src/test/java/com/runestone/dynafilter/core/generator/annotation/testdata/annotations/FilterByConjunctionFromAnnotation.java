package com.runestone.dynafilter.core.generator.annotation.testdata.annotations;

import com.runestone.dynafilter.core.generator.annotation.ConjunctionFrom;
import com.runestone.dynafilter.core.generator.annotation.StatementFrom;
import com.runestone.dynafilter.core.generator.annotation.testdata.dto.FilterDTO;
import com.runestone.dynafilter.core.generator.annotation.testdata.dto.StmtFilterDTO;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Retention(RUNTIME)
@Target({PARAMETER, TYPE})
@ConjunctionFrom(value = FilterDTO.class, disjunctions = @StatementFrom(StmtFilterDTO.class))
public @interface FilterByConjunctionFromAnnotation {
}
