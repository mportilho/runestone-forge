package com.runestone.dynafilter.modules.openapi;

import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Negative;
import jakarta.validation.constraints.NegativeOrZero;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import org.springframework.core.annotation.AnnotatedElementUtils;

import java.lang.reflect.AnnotatedElement;
import java.math.BigDecimal;
import java.util.Objects;

public final class SchemaValidationUtils {

    private SchemaValidationUtils() {
    }

    public static void applyValidations(Schema<?> schema, AnnotatedElement annotatedElement) {
        Objects.requireNonNull(schema, "schema must not be null");
        if (annotatedElement == null) {
            return;
        }
        applyRequired(schema, annotatedElement);
        applySize(schema, annotatedElement);
        applyPattern(schema, annotatedElement);
        applyNumericBounds(schema, annotatedElement);
    }

    private static void applyRequired(Schema<?> schema, AnnotatedElement annotatedElement) {
        if (AnnotatedElementUtils.hasAnnotation(annotatedElement, NotNull.class)
                || AnnotatedElementUtils.hasAnnotation(annotatedElement, NotBlank.class)
                || AnnotatedElementUtils.hasAnnotation(annotatedElement, NotEmpty.class)) {
            schema.setNullable(false);
        }
    }

    private static void applySize(Schema<?> schema, AnnotatedElement annotatedElement) {
        Size size = AnnotatedElementUtils.findMergedAnnotation(annotatedElement, Size.class);
        if (size == null) {
            return;
        }
        if (schema instanceof ArraySchema arraySchema) {
            arraySchema.setMinItems(size.min());
            arraySchema.setMaxItems(size.max());
            return;
        }
        schema.setMinLength(size.min());
        schema.setMaxLength(size.max());
    }

    private static void applyPattern(Schema<?> schema, AnnotatedElement annotatedElement) {
        Pattern pattern = AnnotatedElementUtils.findMergedAnnotation(annotatedElement, Pattern.class);
        if (pattern != null) {
            schema.setPattern(pattern.regexp());
        }
    }

    private static void applyNumericBounds(Schema<?> schema, AnnotatedElement annotatedElement) {
        Min min = AnnotatedElementUtils.findMergedAnnotation(annotatedElement, Min.class);
        if (min != null) {
            schema.setMinimum(BigDecimal.valueOf(min.value()));
        }
        Max max = AnnotatedElementUtils.findMergedAnnotation(annotatedElement, Max.class);
        if (max != null) {
            schema.setMaximum(BigDecimal.valueOf(max.value()));
        }
        DecimalMin decimalMin = AnnotatedElementUtils.findMergedAnnotation(annotatedElement, DecimalMin.class);
        if (decimalMin != null) {
            schema.setMinimum(new BigDecimal(decimalMin.value()));
            schema.setExclusiveMinimum(!decimalMin.inclusive());
        }
        DecimalMax decimalMax = AnnotatedElementUtils.findMergedAnnotation(annotatedElement, DecimalMax.class);
        if (decimalMax != null) {
            schema.setMaximum(new BigDecimal(decimalMax.value()));
            schema.setExclusiveMaximum(!decimalMax.inclusive());
        }
        if (AnnotatedElementUtils.hasAnnotation(annotatedElement, Positive.class)) {
            schema.setMinimum(BigDecimal.ZERO);
            schema.setExclusiveMinimum(true);
        } else if (AnnotatedElementUtils.hasAnnotation(annotatedElement, PositiveOrZero.class)) {
            schema.setMinimum(BigDecimal.ZERO);
        }
        if (AnnotatedElementUtils.hasAnnotation(annotatedElement, Negative.class)) {
            schema.setMaximum(BigDecimal.ZERO);
            schema.setExclusiveMaximum(true);
        } else if (AnnotatedElementUtils.hasAnnotation(annotatedElement, NegativeOrZero.class)) {
            schema.setMaximum(BigDecimal.ZERO);
        }
    }
}
