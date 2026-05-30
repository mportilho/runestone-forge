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

package com.runestone.dynafilter.modules.openapi;

import com.runestone.dynafilter.core.generator.ConditionalStatement;
import com.runestone.dynafilter.core.generator.annotation.Conjunction;
import com.runestone.dynafilter.core.generator.annotation.Filter;
import com.runestone.dynafilter.core.generator.annotation.FilterTarget;
import com.runestone.dynafilter.core.operation.types.Dynamic;
import com.runestone.dynafilter.core.operation.types.Equals;
import com.runestone.dynafilter.core.operation.types.IsIn;
import com.runestone.dynafilter.core.operation.types.IsNull;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.BooleanSchema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.method.HandlerMethod;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class TestDynaFilterOperationCustomizer {

    @Test
    @DisplayName("Dynamic filter parameters are documented as arrays with at least two items")
    public void testDynamicFilterCreatesArraySchemaWithMinItems() throws NoSuchMethodException {
        Operation operation = customize("documentDynamicFilter");

        Parameter parameter = findParameter(operation, "dynamicName");

        Assertions.assertThat(parameter.getSchema()).isInstanceOf(ArraySchema.class);
        Assertions.assertThat(((ArraySchema) parameter.getSchema()).getMinItems()).isEqualTo(2);
        Assertions.assertThat(((ArraySchema) parameter.getSchema()).getItems()).isInstanceOf(StringSchema.class);
    }

    @Test
    @DisplayName("IsIn filter parameters are documented as arrays")
    public void testIsInFilterCreatesArraySchema() throws NoSuchMethodException {
        Operation operation = customize("documentIsInFilter");

        Parameter parameter = findParameter(operation, "tags");

        Assertions.assertThat(parameter.getSchema()).isInstanceOf(ArraySchema.class);
        Assertions.assertThat(((ArraySchema) parameter.getSchema()).getItems()).isInstanceOf(StringSchema.class);
    }

    @Test
    @DisplayName("IsNull filter parameters are documented as booleans")
    public void testIsNullFilterCreatesBooleanSchema() throws NoSuchMethodException {
        Operation operation = customize("documentIsNullFilter");

        Parameter parameter = findParameter(operation, "deleted");

        Assertions.assertThat(parameter.getSchema()).isInstanceOf(BooleanSchema.class);
    }

    @Test
    @DisplayName("Common filter parameters use the schema resolved from the target field")
    public void testCommonFilterUsesTargetFieldSchema() throws NoSuchMethodException {
        Operation operation = customize("documentCommonFilter");

        Parameter parameter = findParameter(operation, "name");

        Assertions.assertThat(parameter.getSchema()).isInstanceOf(StringSchema.class);
        Assertions.assertThat(parameter.getDescription()).isEqualTo("User name");
    }

    @Test
    @DisplayName("Filters backed only by constant values are not exposed as OpenAPI parameters")
    public void testConstantValueFilterDoesNotExposeParameter() throws NoSuchMethodException {
        Operation operation = customize("documentConstantValueFilter");

        Assertions.assertThat(operation.getParameters())
                .extracting(Parameter::getName)
                .doesNotContain("status", "filters");
    }

    private static Operation customize(String methodName) throws NoSuchMethodException {
        Operation operation = new Operation();
        operation.setParameters(new ArrayList<>(List.of(new Parameter().name("filters"))));

        DynaFilterOperationCustomizer customizer = new DynaFilterOperationCustomizer(null);
        return customizer.customize(operation, handlerMethod(methodName));
    }

    private static HandlerMethod handlerMethod(String methodName) throws NoSuchMethodException {
        Method method = OpenApiController.class.getDeclaredMethod(methodName, ConditionalStatement.class);
        return new HandlerMethod(new OpenApiController(), method);
    }

    private static Parameter findParameter(Operation operation, String name) {
        return operation.getParameters().stream()
                .filter(parameter -> parameter.getName().equals(name))
                .findFirst()
                .orElseThrow();
    }

    @SuppressWarnings("unused")
    private static final class OpenApiController {

        public void documentDynamicFilter(
                @io.swagger.v3.oas.annotations.Parameter(name = "filters")
                @Conjunction(@Filter(path = "name", parameters = "dynamicName", operation = Dynamic.class))
                ConditionalStatement filters
        ) {
        }

        public void documentIsInFilter(
                @io.swagger.v3.oas.annotations.Parameter(name = "filters")
                @FilterTarget(OpenApiFilterTarget.class)
                @Conjunction(@Filter(path = "tags", parameters = "tags", operation = IsIn.class))
                ConditionalStatement filters
        ) {
        }

        public void documentIsNullFilter(
                @io.swagger.v3.oas.annotations.Parameter(name = "filters")
                @FilterTarget(OpenApiFilterTarget.class)
                @Conjunction(@Filter(path = "deleted", parameters = "deleted", operation = IsNull.class))
                ConditionalStatement filters
        ) {
        }

        public void documentCommonFilter(
                @io.swagger.v3.oas.annotations.Parameter(name = "filters")
                @FilterTarget(OpenApiFilterTarget.class)
                @Conjunction(@Filter(path = "name", parameters = "name", operation = Equals.class, description = "User name"))
                ConditionalStatement filters
        ) {
        }

        public void documentConstantValueFilter(
                @io.swagger.v3.oas.annotations.Parameter(name = "filters")
                @FilterTarget(OpenApiFilterTarget.class)
                @Conjunction(@Filter(path = "status", parameters = "status", operation = Equals.class, constantValues = "ACTIVE"))
                ConditionalStatement filters
        ) {
        }
    }

    @SuppressWarnings("unused")
    private static final class OpenApiFilterTarget {
        private String name;
        private String tags;
        private Boolean deleted;
        private String status;
    }
}
