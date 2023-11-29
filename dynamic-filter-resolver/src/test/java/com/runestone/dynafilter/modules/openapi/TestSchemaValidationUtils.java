package com.runestone.dynafilter.modules.openapi;

import com.runestone.dynafilter.modules.openapi.tools.ObjectValidations;
import io.swagger.v3.oas.models.media.Schema;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;

public class TestSchemaValidationUtils {

    @Test
    public void testIntegerValidations() throws Exception {
        Schema<?> schema;
        Field field = ObjectValidations.class.getDeclaredField("negativeValues");

        schema = new Schema<>();
        schema.setName("numeroDiasValidade");
        schema.setType("integer");
        SchemaValidationUtils.applyValidations(schema, field);
        assertThat(schema.getMaximum()).isEqualByComparingTo("-1");
        assertThat(schema.getMinimum()).isEqualByComparingTo("-10");
        assertThat(schema.getName()).isEqualTo("numeroDiasValidade");
        assertThat(SchemaValidationUtils.isFieldRequired(field)).isTrue();
    }

    @Test
    public void testIntegerPositiveOrZero() throws Exception {
        Schema<?> schema;
        schema = new Schema<>();
        schema.setName("numeroDiasValidade");
        schema.setType("integer");
        SchemaValidationUtils.applyValidations(schema, ObjectValidations.class.getDeclaredField("positiveOrZeroNumbers"));
        assertThat(schema.getMaximum()).isEqualByComparingTo(Long.toString(Long.MAX_VALUE));
        assertThat(schema.getMinimum()).isEqualByComparingTo("0");
        assertThat(schema.getName()).isEqualTo("numeroDiasValidade");
    }

    @Test
    public void testCustomAnnotation() throws Exception {
        Field field = ObjectValidations.class.getDeclaredField("negativeValues");
        Schema<?> schema;
        schema = new Schema<>();
        schema.setName("name");
        schema.setType("string");

        SchemaValidationUtils.applyValidations(schema, ObjectValidations.class.getDeclaredField("participantName"));
        assertThat(SchemaValidationUtils.isFieldRequired(field)).isTrue();
        assertThat(schema.getPattern()).isEqualTo("\\w*\\W*");
        assertThat(schema.getMaxLength()).isEqualTo(150);
        assertThat(schema.getMinLength()).isEqualTo(0);
        assertThat(schema.getMaximum()).isNull();
        assertThat(schema.getMinimum()).isNull();
        assertThat(schema.getMaxItems()).isNull();
        assertThat(schema.getMaxItems()).isNull();
        assertThat(schema.getExclusiveMinimum()).isNull();
        assertThat(schema.getExclusiveMaximum()).isNull();
    }

    @Test
    public void testDecimalMinMax() throws Exception {
        Field field = ObjectValidations.class.getDeclaredField("limitedBigDecimal");
        Schema<?> schema;
        schema = new Schema<>();
        schema.setName("limitedBigDecimal");
        schema.setType("number");

        SchemaValidationUtils.applyValidations(schema, ObjectValidations.class.getDeclaredField("limitedBigDecimal"));
        assertThat(SchemaValidationUtils.isFieldRequired(field)).isFalse();
        assertThat(schema.getPattern()).isNull();
        assertThat(schema.getMaxLength()).isNull();
        assertThat(schema.getMinLength()).isNull();
        assertThat(schema.getMaximum()).isEqualTo("100.00");
        assertThat(schema.getMinimum()).isEqualTo("0.00");
        assertThat(schema.getMaxItems()).isNull();
        assertThat(schema.getMaxItems()).isNull();
        assertThat(schema.getExclusiveMinimum()).isEqualTo(false);
        assertThat(schema.getExclusiveMaximum()).isEqualTo(false);
    }

    @Test
    public void testListParameter() throws Exception {
        Field field = ObjectValidations.class.getDeclaredField("schedule");
        Schema<?> schema;
        schema = new Schema<>();
        schema.setName("calendar");
        schema.setType("array");

        SchemaValidationUtils.applyValidations(schema, ObjectValidations.class.getDeclaredField("schedule"));
        assertThat(SchemaValidationUtils.isFieldRequired(field)).isTrue();
        assertThat(schema.getMaxLength()).isNull();
        assertThat(schema.getMinLength()).isNull();
        assertThat(schema.getMaximum()).isNull();
        assertThat(schema.getMinimum()).isNull();
        assertThat(schema.getMaxItems()).isEqualTo(300);
        assertThat(schema.getMinItems()).isEqualTo(1);
        assertThat(schema.getExclusiveMinimum()).isNull();
        assertThat(schema.getExclusiveMaximum()).isNull();
    }
}
