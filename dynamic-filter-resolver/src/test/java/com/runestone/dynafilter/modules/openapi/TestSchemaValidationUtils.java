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
