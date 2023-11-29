package com.runestone.expeval.expression;

import com.runestone.expeval.support.callsite.OperationCallSite;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.invoke.MethodType;
import java.math.BigDecimal;
import java.util.Map;

public class TestExpressions {

    @Test
    public void testAddDictionaryAndFunction() {
        Expression expression = new Expression("adder(a, b)");
        expression.addDictionaryEntry("a", BigDecimal.ONE);
        expression.addDictionary(Map.of("b", BigDecimal.TEN));

        expression.addFunction("adder", MethodType.methodType(BigDecimal.class, BigDecimal.class, BigDecimal.class),
                p -> ((BigDecimal) p[0]).add((BigDecimal) p[1]));

        Assertions.assertThat(expression.<BigDecimal>evaluate()).isEqualByComparingTo("11");
    }

    @Test
    public void testAddOperationCallSite() {
        Expression expression = new Expression("adder(1, 10)");
        expression.addFunction(new OperationCallSite("adder", MethodType.methodType(BigDecimal.class, BigDecimal.class, BigDecimal.class),
                p -> ((BigDecimal) p[0]).add((BigDecimal) p[1])));
        Assertions.assertThat(expression.<BigDecimal>evaluate()).isEqualByComparingTo("11");
    }

    @Test
    public void test() {
        Expression expression = new Expression("a = b");
        expression.setVariable("a", 1);
        expression.setVariable("b", 2);
        Assertions.assertThat(expression.<Boolean>evaluate()).isFalse();
    }

}
