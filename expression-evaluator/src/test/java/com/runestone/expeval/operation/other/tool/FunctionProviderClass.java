package com.runestone.expeval.operation.other.tool;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class FunctionProviderClass {

    public Boolean extractedBoolean() {
        return Boolean.TRUE;
    }

    public BigDecimal extractedNumber() {
        return BigDecimal.ONE;
    }

    public String extractedString() {
        return "food";
    }

    public LocalDate extractedDate() {
        return LocalDate.now();
    }

    public LocalTime extractedTime() {
        return LocalTime.of(2, 3, 0);
    }

    public LocalDateTime extractedDateTime() {
        return LocalDateTime.of(LocalDate.now(), LocalTime.of(2, 3, 0));
    }

    public BigDecimal add(BigDecimal a, BigDecimal b) {
        return a.add(b);
    }

}
