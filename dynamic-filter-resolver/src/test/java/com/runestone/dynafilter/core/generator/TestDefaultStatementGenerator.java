/*
 * MIT License
 * <p>
 * Copyright (c) 2023-2023 Marcelo Silva Portilho
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.runestone.dynafilter.core.generator;

import com.runestone.dynafilter.core.exceptions.StatementGenerationException;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.Test;

import java.util.Map;

public class TestDefaultStatementGenerator {

    private static ValueExpressionResolver<Object> getValueExpressionResolver() {
        return value -> {
            if (value != null) {
                return value.startsWith("${") && value.endsWith("}") ? value.substring(2, value.length() - 1) + "-Resolved" : value;
            }
            return null;
        };
    }

    private static ValueExpressionResolver<Object> getMappedValueExpressionResolver(Map<String, Object> valueMap) {
        return value -> {
            if (value != null && value.startsWith("${") && value.endsWith("}")) {
                String key = value.substring(2, value.length() - 1);
                if (key.equals("throwError")) {
                    throw new RuntimeException("Test error on ValueExpressionResolver");
                }
                if (valueMap.containsKey(key)) {
                    return valueMap.get(key);
                } else {
                    return key + "-Resolved";
                }
            }
            return null;
        };
    }

    private static DefaultStatementGenerator<Object> getDefaultStatementProcessor(ValueExpressionResolver<Object> valueExpressionResolver) {
        return new DefaultStatementGenerator<>(valueExpressionResolver) {
            @Override
            public StatementWrapper generateStatements(Object filterInputs, Map<String, Object> filterParameters) {
                return null;
            }
        };
    }

    // ############################ One Parameter ############################

    @Test
    public void testComputeSimpleValue() {
        var defaultStatementProcessor = new DefaultStatementGenerator<>() {
            @Override
            public StatementWrapper generateStatements(Object filterInputs, Map<String, Object> filterParameters) {
                return null;
            }
        };
        Map<String, Object> parametersMap = Map.of("test", "testValue");
        Object computeValue = defaultStatementProcessor.computeValues(new String[]{"test"}, null, null, parametersMap);
        Assertions.assertThat(computeValue).isEqualTo(new Object[]{"testValue"});
    }

    @Test
    public void testComputeSimpleArrayValue() {
        var defaultStatementProcessor = new DefaultStatementGenerator<>() {
            @Override
            public StatementWrapper generateStatements(Object filterInputs, Map<String, Object> filterParameters) {
                return null;
            }
        };
        Map<String, Object> parametersMap = Map.of("test", new Object[]{"testValue"});
        Object computeValue = defaultStatementProcessor.computeValues(new String[]{"test"}, null, null, parametersMap);
        Assertions.assertThat(computeValue).isEqualTo(new Object[]{new Object[]{"testValue"}});
    }

    @Test
    public void testEmptyValue() {
        var defaultStatementProcessor = getDefaultStatementProcessor(null);
        Map<String, Object> parametersMap = Map.of("test", new Object[]{});
        Object computeValue = defaultStatementProcessor.computeValues(new String[]{"test"}, null, null, parametersMap);
        Assertions.assertThat(computeValue).isEqualTo(new Object[]{new Object[]{}});
    }

    @Test
    public void testNoValueFound() {
        var defaultStatementProcessor = getDefaultStatementProcessor(null);
        Map<String, Object> parametersMap = Map.of("test", "testValue");
        Object computeValue = defaultStatementProcessor.computeValues(new String[]{"test2"}, null, null, parametersMap);
        Assertions.assertThat(computeValue).isNull();
    }

    @Test
    public void testNoValueFoundWithDefault() {
        var defaultStatementProcessor = getDefaultStatementProcessor(null);
        Map<String, Object> parametersMap = Map.of("test", new Object[]{"testValue"});
        Object computeValue = defaultStatementProcessor.computeValues(new String[]{"test2"}, new Object[]{"defaultValue"}, null, parametersMap);
        Assertions.assertThat(computeValue).isEqualTo(new Object[]{"defaultValue"});
    }

    @Test
    public void testNoValueFoundWithDefaultAndExpressionResolver() {
        var defaultStatementProcessor = getDefaultStatementProcessor(getValueExpressionResolver());
        Map<String, Object> parametersMap = Map.of("test", new Object[]{"testValue"});
        Object computeValue = defaultStatementProcessor.computeValues(new String[]{"test2"}, new Object[]{"${defaultValue}"}, null, parametersMap);
        Assertions.assertThat(computeValue).isEqualTo(new Object[]{"defaultValue-Resolved"});
    }

    @Test
    public void testNoValueFoundWithConstant() {
        var defaultStatementProcessor = getDefaultStatementProcessor(null);
        Map<String, Object> parametersMap = Map.of("test2", new Object[]{"testValue"});
        Object computeValue = defaultStatementProcessor.computeValues(new String[]{"test2"}, null, new Object[]{"constantValue"}, parametersMap);
        Assertions.assertThat(computeValue).isEqualTo(new Object[]{"constantValue"}); // constant value have precedence over default or parameter value
    }

    @Test
    public void testNoValueFoundWithConstantAndExpressionResolver() {
        var defaultStatementProcessor = getDefaultStatementProcessor(getValueExpressionResolver());
        Map<String, Object> parametersMap = Map.of("test2", new Object[]{"testValue"});
        Object computeValue = defaultStatementProcessor.computeValues(new String[]{"test2"}, null, new Object[]{"${constantValue}"}, parametersMap);
        Assertions.assertThat(computeValue).isEqualTo(new Object[]{"constantValue-Resolved"});
    }

    @Test
    public void testThrowOnNullParameter() {
        var defaultStatementProcessor = getDefaultStatementProcessor(null);
        Map<String, Object> parametersMap = Map.of();
        Assertions.assertThatThrownBy(() -> defaultStatementProcessor.computeValues(null, null, null, parametersMap))
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Parameters cannot be null or empty");
    }

    @Test
    public void testThrowOnEmptyNameParameter() {
        var defaultStatementProcessor = getDefaultStatementProcessor(null);
        Map<String, Object> parametersMap = Map.of();
        Assertions.assertThatThrownBy(() -> defaultStatementProcessor.computeValues(new String[]{""}, new Object[]{"defaultValue"}, null, parametersMap))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Parameter name cannot be null or empty");
    }

    @Test
    public void testThrownOnParamsAndDefaultValuesWithDiffSizes() {
        var defaultStatementProcessor = getDefaultStatementProcessor(null);
        Map<String, Object> parametersMap = Map.of("param1", new Object[]{"value1"}, "param2", new Object[]{"value2"});
        var defaultValues = new Object[]{new Object[]{"defaultValue1", "defaultValue2"}};

        AssertionsForClassTypes.assertThatThrownBy(() -> defaultStatementProcessor.computeValues(new String[]{"param1", "param2"}, defaultValues, null, parametersMap))
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Parameters and default values have different sizes");
    }

    @Test
    public void testThrownOnParamsAndConstantValuesWithDiffSizes() {
        var defaultStatementProcessor = getDefaultStatementProcessor(null);
        Map<String, Object> parametersMap = Map.of("param1", new Object[]{"value1"}, "param2", new Object[]{"value2"});
        var constantValues = new Object[]{new Object[]{"defaultValue1", "defaultValue2"}};

        AssertionsForClassTypes.assertThatThrownBy(() -> defaultStatementProcessor.computeValues(new String[]{"param1", "param2"}, null, constantValues, parametersMap))
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Parameters and constant values have different sizes");
    }

    // ############################ Multi parameters ############################

    @Test
    public void testMultiParamsOneValue() {
        var defaultStatementProcessor = getDefaultStatementProcessor(null);
        Map<String, Object> parametersMap = Map.of(
                "param1", "value1",
                "param2", "value2"
        );
        Object[] computeValues = defaultStatementProcessor.computeValues(new String[]{"param1", "param2"}, null, null, parametersMap);
        Assertions.assertThat(computeValues).isEqualTo(new Object[]{"value1", "value2"});
    }

    @Test
    public void testMultiParamEmptyValue() {
        var defaultStatementProcessor = getDefaultStatementProcessor(null);
        Map<String, Object> parametersMap = Map.of(
                "param1", new Object[]{},
                "param2", new Object[]{}
        );
        Object[] computeValues = defaultStatementProcessor.computeValues(new String[]{"param1", "param2"}, null, null, parametersMap);
        Assertions.assertThat(computeValues).isEqualTo(new Object[]{new Object[]{}, new Object[]{}});
    }

    @Test
    public void testMultiParamNoValueFound() {
        var defaultStatementProcessor = getDefaultStatementProcessor(null);
        Map<String, Object> parametersMap = Map.of(
                "param1", new Object[]{"value1"},
                "param2", new Object[]{"value2"}
        );
        Object[] computeValues = defaultStatementProcessor.computeValues(new String[]{"param3", "param4"}, null, null, parametersMap);
        Assertions.assertThat(computeValues).isNull();
    }

    @Test
    public void testMultiParamOneValueNotFound() {
        var defaultStatementProcessor = getDefaultStatementProcessor(null);
        Map<String, Object> parametersMap = Map.of(
                "param1", new Object[]{"value1"},
                "param2", new Object[]{"value2"}
        );
        Object[] computeValues = defaultStatementProcessor.computeValues(new String[]{"param1", "param3"}, null, null, parametersMap);
        Assertions.assertThat(computeValues).isEqualTo(new Object[][]{new Object[]{"value1"}, null});
    }

    @Test
    public void testMultiParamsWithDefaultValues() {
        var defaultStatementProcessor = getDefaultStatementProcessor(null);
        Map<String, Object> parametersMap = Map.of();
        var defaultValues = new Object[]{new Object[]{"defaultValue1"}, new Object[]{"defaultValue2"}};
        Object[] computeValues = defaultStatementProcessor.computeValues(new String[]{"param1", "param2"}, defaultValues, null, parametersMap);
        Assertions.assertThat(computeValues).isEqualTo(new Object[][]{new Object[]{"defaultValue1"}, new Object[]{"defaultValue2"}});
    }

    @Test
    public void testMultiParamWithDefaultValuesAndOneResolvableExpression() {
        var defaultStatementProcessor = getDefaultStatementProcessor(getValueExpressionResolver());
        Map<String, Object> parametersMap = Map.of();
        var defaultValues = new Object[]{"${defaultValue}", "normalDefault"};
        Object computeValue = defaultStatementProcessor.computeValues(new String[]{"test1", "test2"}, defaultValues, null, parametersMap);
        Assertions.assertThat(computeValue).isEqualTo(new Object[]{"defaultValue-Resolved", "normalDefault"});
    }

    @Test
    public void testMultiParamsWithDefaultArrayValues() {
        var defaultStatementProcessor = getDefaultStatementProcessor(null);
        Map<String, Object> parametersMap = Map.of();
        var defaultValues = new Object[]{new Object[]{"defaultValue1", "defaultValue1.1"}, new Object[]{"defaultValue2"}};
        Object[] computeValues = defaultStatementProcessor.computeValues(new String[]{"param1", "param2"}, defaultValues, null, parametersMap);
        Assertions.assertThat(computeValues).isEqualTo(new Object[]{new Object[]{"defaultValue1", "defaultValue1.1"}, new Object[]{"defaultValue2"}});
    }

    @Test
    public void testMultiParamsWithDefaultArrayValuesWithResolver() {
        var defaultStatementProcessor = getDefaultStatementProcessor(getValueExpressionResolver());
        Map<String, Object> parametersMap = Map.of();
        var defaultValues = new Object[]{new Object[]{123, "${defaultValue1.1}"}, new Object[]{"${defaultValue2}"}};
        Object[] computeValues = defaultStatementProcessor.computeValues(new String[]{"param1", "param2"}, defaultValues, null, parametersMap);
        Assertions.assertThat(computeValues).isEqualTo(new Object[]{new Object[]{123, "defaultValue1.1-Resolved"}, new Object[]{"defaultValue2-Resolved"}});
    }

    @Test
    public void testMultiParamsWithConstantValues() {
        var defaultStatementProcessor = getDefaultStatementProcessor(null);
        Map<String, Object> parametersMap = Map.of();
        var constantValues = new Object[]{new Object[]{"constantValue1"}, new Object[]{"constantValue2"}};
        Object[] computeValues = defaultStatementProcessor.computeValues(new String[]{"param1", "param2"}, null, constantValues, parametersMap);
        Assertions.assertThat(computeValues).isEqualTo(new Object[][]{new Object[]{"constantValue1"}, new Object[]{"constantValue2"}});
    }

    @Test
    public void testMultiParamWithConstantValuesAndOneResolvableExpression() {
        var defaultStatementProcessor = getDefaultStatementProcessor(getValueExpressionResolver());
        Map<String, Object> parametersMap = Map.of();
        var constantValues = new Object[]{"${constantValue}", "normalConstant"};
        Object computeValue = defaultStatementProcessor.computeValues(new String[]{"test1", "test2"}, null, constantValues, parametersMap);
        Assertions.assertThat(computeValue).isEqualTo(new Object[]{"constantValue-Resolved", "normalConstant"});
    }

    @Test
    public void testMultiParamsWithConstantScalarValues() {
        var defaultStatementProcessor = getDefaultStatementProcessor(null);
        Map<String, Object> parametersMap = Map.of();
        var constantValues = new Object[]{new Object[]{"constantValue1", "constantValue1.1"}, new Object[]{"constantValue2"}, "constantValue3"};
        Object[] computeValues = defaultStatementProcessor.computeValues(new String[]{"param1", "param2", "param3"}, null, constantValues, parametersMap);
        Assertions.assertThat(computeValues).isEqualTo(new Object[]{new Object[]{"constantValue1", "constantValue1.1"}, new Object[]{"constantValue2"}, "constantValue3"});
    }

    @Test
    public void testMultiParamsWithConstantArrayValues() {
        var defaultStatementProcessor = getDefaultStatementProcessor(null);
        Map<String, Object> parametersMap = Map.of();
        var constantValues = new Object[]{new Object[]{"constantValue1", "constantValue1.1"}, new Object[]{"constantValue2"}};
        Object[] computeValues = defaultStatementProcessor.computeValues(new String[]{"param1", "param2"}, null, constantValues, parametersMap);
        Assertions.assertThat(computeValues).isEqualTo(new Object[]{new Object[]{"constantValue1", "constantValue1.1"}, new Object[]{"constantValue2"}});
    }

    @Test
    public void testMultiParamsWithConstantArrayValuesWithResolver() {
        var defaultStatementProcessor = getDefaultStatementProcessor(getValueExpressionResolver());
        Map<String, Object> parametersMap = Map.of();
        var constantValues = new Object[]{new Object[]{123, "${constantValue1.1}"}, new Object[]{"${constantValue2}"}};
        Object[] computeValues = defaultStatementProcessor.computeValues(new String[]{"param1", "param2"}, null, constantValues, parametersMap);
        Assertions.assertThat(computeValues).isEqualTo(new Object[]{new Object[]{123, "constantValue1.1-Resolved"}, new Object[]{"constantValue2-Resolved"}});
    }

    // ############################ computeNegatingParameter ############################

    @Test
    public void testNegateWithNull() {
        var defaultStatementProcessor = getDefaultStatementProcessor(getValueExpressionResolver());
        Boolean negate = defaultStatementProcessor.computeNegatingParameter(null);
        Assertions.assertThat(negate).isFalse();
    }

    @Test
    public void testNegateWithTrue() {
        var defaultStatementProcessor = getDefaultStatementProcessor(getValueExpressionResolver());
        Boolean negate = defaultStatementProcessor.computeNegatingParameter(true);
        Assertions.assertThat(negate).isTrue();
    }

    @Test
    public void testNegateWithFalse() {
        var defaultStatementProcessor = getDefaultStatementProcessor(getValueExpressionResolver());
        Boolean negate = defaultStatementProcessor.computeNegatingParameter(false);
        Assertions.assertThat(negate).isFalse();
    }

    @Test
    public void testNegateWithStringTrue() {
        var defaultStatementProcessor = getDefaultStatementProcessor(getValueExpressionResolver());
        Boolean negate = defaultStatementProcessor.computeNegatingParameter("true");
        Assertions.assertThat(negate).isTrue();

        negate = defaultStatementProcessor.computeNegatingParameter("TRUE");
        Assertions.assertThat(negate).isTrue();
    }

    @Test
    public void testNegateWithStringFalse() {
        var defaultStatementProcessor = getDefaultStatementProcessor(getValueExpressionResolver());
        Boolean negate = defaultStatementProcessor.computeNegatingParameter("false");
        Assertions.assertThat(negate).isFalse();

        negate = defaultStatementProcessor.computeNegatingParameter("FALSE");
        Assertions.assertThat(negate).isFalse();
    }

    @Test
    public void testNegateWithStringValue() {
        var defaultStatementProcessor = getDefaultStatementProcessor(getMappedValueExpressionResolver(Map.of("param1", "true")));
        Boolean negate = defaultStatementProcessor.computeNegatingParameter("${param1}");
        Assertions.assertThat(negate).isTrue();
    }

    @Test
    public void testNegateWithStringArrayOneValue() {
        var defaultStatementProcessor = getDefaultStatementProcessor(getMappedValueExpressionResolver(Map.of("param1", new Object[]{"true"})));
        Boolean negate = defaultStatementProcessor.computeNegatingParameter("${param1}");
        Assertions.assertThat(negate).isTrue();
    }

    @Test
    public void testNegateThrowOnWithStringArrayMultiValue() {
        var defaultStatementProcessor = getDefaultStatementProcessor(getMappedValueExpressionResolver(Map.of("param1", new Object[]{"true", "true"})));
        Assertions.assertThatThrownBy(() -> defaultStatementProcessor.computeNegatingParameter("${param1}"))
                .isExactlyInstanceOf(StatementGenerationException.class)
                .hasMessageStartingWith("Default value for negating parameter produced more than one value");
    }

    @Test
    public void testNegateThrowOnInsideValueExpressionResolver() {
        var defaultStatementProcessor = getDefaultStatementProcessor(getMappedValueExpressionResolver(Map.of("param1", "true")));
        Assertions.assertThatThrownBy(() -> defaultStatementProcessor.computeNegatingParameter("${throwError}"))
                .isExactlyInstanceOf(StatementGenerationException.class)
                .hasMessage("Provided expression resolver threw an error");
    }

}
