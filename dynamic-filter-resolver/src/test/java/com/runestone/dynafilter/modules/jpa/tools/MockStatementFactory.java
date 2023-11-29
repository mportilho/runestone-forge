package com.runestone.dynafilter.modules.jpa.tools;

import com.runestone.dynafilter.core.model.FilterData;
import com.runestone.dynafilter.core.model.statement.LogicalStatement;
import com.runestone.dynafilter.core.operation.types.Like;

public class MockStatementFactory {

    public static LogicalStatement createLogicalStatementOnName() {
        var filterDataName = new FilterData("name", new String[]{"clientName"}, String.class, Like.class, true, new Object[]{"Joe"}, null, "");
        return new LogicalStatement(filterDataName);
    }

    public static LogicalStatement createLogicalStatementOnClientJob() {
        var filterDataJob = new FilterData("job", new String[]{"clientJob"}, String.class, Like.class, true, new Object[]{"Developer"}, null, "");
        return new LogicalStatement(filterDataJob);
    }

}
