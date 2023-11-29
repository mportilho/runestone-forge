package com.runestone.dynafilter.modules.openapi;

import io.swagger.v3.oas.models.media.Schema;
import jakarta.validation.constraints.*;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.math.BigDecimal;

public class SchemaValidationUtils {

    /**
     * Applies Bean Validation's requirements on the request parameter based on the
     * annotated attribute located on the {@link com.runestone.dynafilter.core.generator.annotation.Filter#path()}
     */
    public static void applyValidations(Schema<?> schema, AnnotatedElement annotatedElement) {
        if (isNumberType(schema)) {
            if (AnnotationUtils.findAnnotation(annotatedElement, PositiveOrZero.class) != null) {
                schema.setMaximum(BigDecimal.valueOf(Long.MAX_VALUE));
                schema.setMinimum(BigDecimal.ZERO);
            }

            Min min = AnnotationUtils.findAnnotation(annotatedElement, Min.class);
            if (min != null) {
                schema.setMinimum(new BigDecimal(min.value()));
            }

            Max max = AnnotationUtils.findAnnotation(annotatedElement, Max.class);
            if (max != null) {
                schema.setMaximum(new BigDecimal(max.value()));
            }

            DecimalMin decimalMin = AnnotationUtils.findAnnotation(annotatedElement, DecimalMin.class);
            if (decimalMin != null) {
                schema.setMinimum(new BigDecimal(decimalMin.value()));
                schema.setExclusiveMinimum(!decimalMin.inclusive());
            }

            DecimalMax decimalMax = AnnotationUtils.findAnnotation(annotatedElement, DecimalMax.class);
            if (decimalMax != null) {
                schema.setMaximum(new BigDecimal(decimalMax.value()));
                schema.setExclusiveMaximum(!decimalMax.inclusive());
            }
        }

        if (isStringType(schema)) {
            Size size = AnnotationUtils.findAnnotation(annotatedElement, Size.class);
            if (size != null) {
                schema.minLength(size.min());
                schema.maxLength(size.max());
            }

            Pattern pattern = AnnotationUtils.findAnnotation(annotatedElement, Pattern.class);
            if (pattern != null) {
                schema.setPattern(pattern.regexp());
            }
        }

        if (isArrayType(schema)) {
            Size size = AnnotationUtils.findAnnotation(annotatedElement, Size.class);
            if (size != null) {
                schema.setMinItems(size.min());
                schema.setMaxItems(size.max());
            }
        }
    }

    private static boolean isNumberType(Schema<?> schema) {
        return "integer".equals(schema.getType()) || "number".equals(schema.getType());
    }

    private static boolean isStringType(Schema<?> schema) {
        return "string".equals(schema.getType());
    }

    private static boolean isArrayType(Schema<?> schema) {
        return "array".equals(schema.getType());
    }

    public static boolean isFieldRequired(Field field) {
        return AnnotationUtils.findAnnotation(field, NotNull.class) != null
                || AnnotationUtils.findAnnotation(field, NotBlank.class) != null
                || AnnotationUtils.findAnnotation(field, NotEmpty.class) != null;
    }

}
