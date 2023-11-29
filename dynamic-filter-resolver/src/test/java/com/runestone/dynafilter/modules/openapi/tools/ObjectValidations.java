package com.runestone.dynafilter.modules.openapi.tools;

import com.runestone.dynafilter.core.generator.annotation.Disjunction;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ObjectValidations(

        @Max(-1)
        @Min(-10)
        @NotNull
        Integer negativeValues,

        @PositiveOrZero
        Integer positiveOrZeroNumbers,

        @PositiveOrZero
        Integer positiveNumbers,

        BigDecimal valorBigDecimal,

        @DecimalMin("0.00")
        @DecimalMax("100.00")
        BigDecimal limitedBigDecimal,

        @ParticipantName
        String participantName,

        @NotEmpty
        @Size(min = 1, max = 300)
        List<LocalDate> schedule

) {
}