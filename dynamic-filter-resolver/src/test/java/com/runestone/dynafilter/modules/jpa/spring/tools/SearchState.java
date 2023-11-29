package com.runestone.dynafilter.modules.jpa.spring.tools;

import com.runestone.dynafilter.core.generator.annotation.Conjunction;
import com.runestone.dynafilter.core.generator.annotation.Filter;
import com.runestone.dynafilter.core.operation.types.Equals;
import org.springframework.data.jpa.domain.Specification;

@Conjunction({
        @Filter(path = "state", parameters = {"state"}, operation = Equals.class),
})
public interface SearchState<T> extends Specification<T> {
}
