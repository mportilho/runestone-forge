package com.runestone.dynafilter.core.operation;

import com.runestone.dynafilter.core.exceptions.FilterOperationNotDefinedException;
import com.runestone.dynafilter.core.model.FilterData;
import com.runestone.dynafilter.core.operation.types.Equals;
import com.runestone.dynafilter.core.operation.types.Like;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestFilterOperationService {

    @Test
    public void testFilterOperationService() {
        var stringFilterOperationService = new StringFilterOperationService();
        var filterData = FilterData.of("name", new String[]{"clientName"}, String.class, Equals.class, new Object[]{"Joe"});
        String filter = stringFilterOperationService.createFilter(filterData);
        Assertions.assertThat(filter).isEqualTo("=Joe");
    }

    @Test
    public void testNoFilterOperationFoundOnService() {
        var stringFilterOperationService = new StringFilterOperationService();
        var filterData = FilterData.of("name", new String[]{"clientName"}, String.class, Like.class, new Object[][]{{"Joe"}});
        Assertions.assertThatThrownBy(() -> stringFilterOperationService.createFilter(filterData)).isInstanceOf(FilterOperationNotDefinedException.class);
    }

}
