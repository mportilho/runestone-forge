package com.runestone.dynafilter.modules.openapi;

import com.runestone.dynafilter.core.annotation.ConjunctionFrom;
import com.runestone.dynafilter.core.annotation.DisjunctionFrom;
import com.runestone.dynafilter.core.annotation.Filter;
import com.runestone.dynafilter.core.annotation.FilterTarget;
import com.runestone.dynafilter.core.generator.ConditionalStatement;
import com.runestone.dynafilter.core.operation.Dynamic;
import com.runestone.dynafilter.core.operation.Equals;
import com.runestone.dynafilter.core.operation.IsIn;
import com.runestone.dynafilter.core.operation.IsNull;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.BooleanSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.method.HandlerMethod;

import java.lang.reflect.Method;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DynaFilterOperationCustomizerTest {

    private final DynaFilterOperationCustomizer customizer = new DynaFilterOperationCustomizer();

    @Test
    @DisplayName("removes technical parameter and documents request filters")
    void removesTechnicalParameterAndDocumentsFilters() throws NoSuchMethodException {
        Operation operation = new Operation().parameters(List.of(new Parameter().name("filters").in("query")));

        Operation customized = customizer.customize(operation, handlerMethod("list", ConditionalStatement.class));

        assertThat(customized.getParameters()).extracting(Parameter::getName).containsExactly("name");
        Parameter name = customized.getParameters().getFirst();
        assertThat(name.getIn()).isEqualTo("query");
        assertThat(name.getRequired()).isFalse();
        assertThat(name.getSchema().getMinLength()).isEqualTo(2);
        assertThat(name.getSchema().getMaxLength()).isEqualTo(30);
        assertThat(name.getSchema().getPattern()).isEqualTo("[A-Z].*");
    }

    @Test
    @DisplayName("omits filters with constant values")
    void omitsFiltersWithConstantValues() throws NoSuchMethodException {
        Operation operation = new Operation().parameters(List.of(new Parameter().name("constantFilters").in("query")));

        Operation customized = customizer.customize(operation, handlerMethod("constantFilters", ConditionalStatement.class));

        assertThat(customized.getParameters()).isEmpty();
    }

    @Test
    @DisplayName("creates special schemas for Dynamic, IsIn and IsNull")
    void createsSpecialSchemas() throws NoSuchMethodException {
        Operation operation = new Operation().parameters(List.of(new Parameter().name("specialFilters").in("query")));

        Operation customized = customizer.customize(operation, handlerMethod("specialFilters", ConditionalStatement.class));

        Parameter dynamic = parameter(customized, "search");
        assertThat(dynamic.getSchema()).isInstanceOf(ArraySchema.class);
        assertThat(((ArraySchema) dynamic.getSchema()).getMinItems()).isEqualTo(2);
        Parameter tags = parameter(customized, "tag");
        assertThat(tags.getSchema()).isInstanceOf(ArraySchema.class);
        Parameter deleted = parameter(customized, "deleted");
        assertThat(deleted.getSchema()).isInstanceOf(BooleanSchema.class);
    }

    @Test
    @DisplayName("documents isolated DisjunctionFrom filters")
    void documentsIsolatedDisjunctionFromFilters() throws NoSuchMethodException {
        Operation operation = new Operation().parameters(List.of(new Parameter().name("disjunctionFilters").in("query")));

        Operation customized = customizer.customize(operation, handlerMethod("disjunctionFilters", ConditionalStatement.class));

        assertThat(customized.getParameters()).extracting(Parameter::getName).containsExactly("name");
    }

    @Test
    @DisplayName("preserves existing path parameters as required")
    void preservesExistingPathParameterAsRequired() throws NoSuchMethodException {
        Operation operation = new Operation().parameters(List.of(
                new Parameter().name("pathFilters").in("query"),
                new Parameter().name("code").in("path")
        ));

        Operation customized = customizer.customize(operation, handlerMethod("pathFilters", ConditionalStatement.class));

        Parameter code = parameter(customized, "code");
        assertThat(code.getIn()).isEqualTo("path");
        assertThat(code.getRequired()).isTrue();
    }

    private static Parameter parameter(Operation operation, String name) {
        return operation.getParameters().stream()
                .filter(parameter -> parameter.getName().equals(name))
                .findFirst()
                .orElseThrow();
    }

    private static HandlerMethod handlerMethod(String methodName, Class<?> parameterType) throws NoSuchMethodException {
        Method method = TestController.class.getDeclaredMethod(methodName, parameterType);
        return new HandlerMethod(new TestController(), method);
    }

    private static final class TestController {

        void list(@io.swagger.v3.oas.annotations.Parameter(name = "filters") @ConjunctionFrom(NameFilters.class) ConditionalStatement filters) {
        }

        void constantFilters(@io.swagger.v3.oas.annotations.Parameter(name = "constantFilters") @ConjunctionFrom(ConstantFilters.class) ConditionalStatement filters) {
        }

        void specialFilters(@io.swagger.v3.oas.annotations.Parameter(name = "specialFilters") @ConjunctionFrom(SpecialFilters.class) ConditionalStatement filters) {
        }

        void disjunctionFilters(@io.swagger.v3.oas.annotations.Parameter(name = "disjunctionFilters") @DisjunctionFrom(NameFilters.class) ConditionalStatement filters) {
        }

        void pathFilters(@io.swagger.v3.oas.annotations.Parameter(name = "pathFilters") @ConjunctionFrom(PathFilters.class) ConditionalStatement filters) {
        }
    }

    @FilterTarget(TargetObject.class)
    private static final class NameFilters {

        @Filter(path = "name", parameters = "name", operation = Equals.class)
        private String name;
    }

    @FilterTarget(TargetObject.class)
    private static final class ConstantFilters {

        @Filter(path = "name", parameters = "name", operation = Equals.class, constantValues = "Ada")
        private String name;
    }

    @FilterTarget(TargetObject.class)
    private static final class SpecialFilters {

        @Filter(path = "name", parameters = "search", operation = Dynamic.class)
        private String search;

        @Filter(path = "tags", parameters = "tag", operation = IsIn.class)
        private String tag;

        @Filter(path = "deleted", parameters = "deleted", operation = IsNull.class)
        private String deleted;
    }

    @FilterTarget(TargetObject.class)
    private static final class PathFilters {

        @Filter(path = "code", parameters = "code", operation = Equals.class)
        private String code;
    }

    private static final class TargetObject {

        @Size(min = 2, max = 30)
        @Pattern(regexp = "[A-Z].*")
        private String name;

        private String[] tags;

        private Boolean deleted;

        private String code;
    }
}
