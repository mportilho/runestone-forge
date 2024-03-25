package com.runestone.dynafilter.core.generator.annotation.testdata.dto;

import com.runestone.dynafilter.core.generator.annotation.Filter;
import com.runestone.dynafilter.core.generator.annotation.testdata.StatusEnum;
import com.runestone.dynafilter.core.operation.types.Equals;
import com.runestone.dynafilter.core.operation.types.StartsWith;

public record StmtFilterDTO(

        @Filter(path = "nameStatement", parameters = "nameStatement", operation = StartsWith.class)
        String nameStatement,

        @Filter(path = "statusStatement", parameters = "statusStatement", operation = Equals.class)
        StatusEnum statusStatement
) {
}
