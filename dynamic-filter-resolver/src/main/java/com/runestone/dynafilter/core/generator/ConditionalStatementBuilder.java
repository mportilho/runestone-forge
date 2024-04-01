//package com.runestone.dynafilter.core.generator;
//
//import com.runestone.dynafilter.core.model.FilterData;
//import com.runestone.dynafilter.core.model.FilterModifier;
//import com.runestone.dynafilter.core.model.statement.*;
//import com.runestone.dynafilter.core.operation.DefinedFilterOperation;
//import com.runestone.dynafilter.core.operation.types.Equals;
//
//import java.util.List;
//
//public class ConditionalStatementBuilder {
//
//    private AbstractStatement statement;
//
//    public ConditionalStatementBuilder addStatement(LogicOperator operator, String path, Class<?> type,
//                                                    Class<? super DefinedFilterOperation> operation, boolean negate,
//                                                    Object value, List<Class<? extends FilterModifier>> modifiers) {
//        FilterData filterData = new FilterData(path, new String[]{operation.getSimpleName()}, type, operation, negate, new Object[]{value}, modifiers, null);
//        createStatement(filterData, operator);
//        return this;
//    }
//
////    public interface DefinedFilterOperation extends Between, Decorated, Dynamic, EndsWith, Equals,
////            Greater, GreaterOrEquals, IsIn, IsNull, Less, LessOrEquals, Like, StartsWith {
////    }
//
//    public final ConditionalStatementBuilder addEquals(String path, Object value, LogicOperator operator, List<Class<? extends FilterModifier>> modifiers) {
//        FilterData filterData = new FilterData(path, new String[]{"equals"}, Object.class, Equals.class, false, new Object[]{value}, modifiers, null);
//        createStatement(filterData, operator);
//        return this;
//    }
//
//    private void createStatement(FilterData filterData, LogicOperator logicType) {
//        if (statement == null) {
//            statement = ofLogicalStatement(filterData);
//        } else {
//            statement = new CompoundStatement(statement, ofLogicalStatement(filterData), logicType);
//        }
//    }
//
//    private AbstractStatement ofLogicalStatement(FilterData filterData) {
//        LogicalStatement statement = new LogicalStatement(filterData);
//        return filterData.negate() ? new NegatedStatement(statement) : statement;
//    }
//
//}
