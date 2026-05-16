package com.runestone.dynafilter.modules.openapi;

import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.StringSchema;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class SchemaValidationUtilsTest {

    @Test
    @DisplayName("applies string validation constraints and composed annotations")
    void appliesStringValidationConstraints() throws NoSuchFieldException {
        StringSchema schema = new StringSchema();

        SchemaValidationUtils.applyValidations(schema, field("name"));

        assertThat(schema.getMinLength()).isEqualTo(2);
        assertThat(schema.getMaxLength()).isEqualTo(20);
        assertThat(schema.getPattern()).isEqualTo("[A-Z][a-z]+( [A-Z][a-z]+)*");
    }

    @Test
    @DisplayName("applies numeric validation constraints")
    void appliesNumericValidationConstraints() throws NoSuchFieldException {
        IntegerSchema schema = new IntegerSchema();

        SchemaValidationUtils.applyValidations(schema, field("age"));

        assertThat(schema.getMinimum()).isEqualByComparingTo(BigDecimal.valueOf(18));
        assertThat(schema.getMaximum()).isEqualByComparingTo(BigDecimal.valueOf(120));
    }

    @Test
    @DisplayName("applies array size validation constraints")
    void appliesArraySizeValidationConstraints() throws NoSuchFieldException {
        ArraySchema schema = new ArraySchema();

        SchemaValidationUtils.applyValidations(schema, field("tags"));

        assertThat(schema.getMinItems()).isEqualTo(1);
        assertThat(schema.getMaxItems()).isEqualTo(3);
    }

    private static Field field(String name) throws NoSuchFieldException {
        return ValidatedObject.class.getDeclaredField(name);
    }

    private static final class ValidatedObject {

        @ParticipantName
        private String name;

        @Min(18)
        @Max(120)
        private Integer age;

        @Size(min = 1, max = 3)
        private String[] tags;
    }

    @Constraint(validatedBy = {})
    @Size(min = 2, max = 20)
    @Pattern(regexp = "[A-Z][a-z]+( [A-Z][a-z]+)*")
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD, ElementType.ANNOTATION_TYPE})
    private @interface ParticipantName {

        String message() default "invalid participant name";

        Class<?>[] groups() default {};

        Class<? extends Payload>[] payload() default {};
    }
}
