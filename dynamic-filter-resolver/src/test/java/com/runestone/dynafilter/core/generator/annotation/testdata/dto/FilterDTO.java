package com.runestone.dynafilter.core.generator.annotation.testdata.dto;

import com.runestone.dynafilter.core.generator.annotation.Filter;
import com.runestone.dynafilter.core.generator.annotation.testdata.StatusEnum;
import com.runestone.dynafilter.core.operation.types.Equals;
import com.runestone.dynafilter.core.operation.types.StartsWith;

public record FilterDTO(

        @Filter(path = "name", parameters = "name", operation = StartsWith.class)
        String name,

        @Filter(path = "status", parameters = "status", operation = Equals.class)
        StatusEnum status

) {
}
