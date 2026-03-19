package com.runestone.expeval2.internal.runtime;

import com.runestone.expeval2.types.ResolvedType;
import com.runestone.expeval2.types.ScalarType;
import com.runestone.expeval2.types.UnknownType;
import com.runestone.expeval2.types.VectorType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;

sealed interface RuntimeValue permits 
    RuntimeValue.BooleanValue, 
    RuntimeValue.DateTimeValue, 
    RuntimeValue.DateValue, 
    RuntimeValue.NullValue, 
    RuntimeValue.NumberValue, 
    RuntimeValue.StringValue, 
    RuntimeValue.TimeValue, 
    RuntimeValue.VectorValue {

    ResolvedType type();

    Object raw();

    record BooleanValue(boolean value) implements RuntimeValue {
        @Override
        public ResolvedType type() {
            return ScalarType.BOOLEAN;
        }

        @Override
        public Object raw() {
            return value;
        }
    }

    record DateTimeValue(LocalDateTime value) implements RuntimeValue {
        public DateTimeValue {
            Objects.requireNonNull(value, "value must not be null");
        }

        @Override
        public ResolvedType type() {
            return ScalarType.DATETIME;
        }

        @Override
        public Object raw() {
            return value;
        }
    }

    record DateValue(LocalDate value) implements RuntimeValue {
        public DateValue {
            Objects.requireNonNull(value, "value must not be null");
        }

        @Override
        public ResolvedType type() {
            return ScalarType.DATE;
        }

        @Override
        public Object raw() {
            return value;
        }
    }

    enum NullValue implements RuntimeValue {
        INSTANCE;

        @Override
        public ResolvedType type() {
            return UnknownType.INSTANCE;
        }

        @Override
        public Object raw() {
            return null;
        }
    }

    record NumberValue(BigDecimal value) implements RuntimeValue {
        public NumberValue {
            Objects.requireNonNull(value, "value must not be null");
        }

        @Override
        public ResolvedType type() {
            return ScalarType.NUMBER;
        }

        @Override
        public Object raw() {
            return value;
        }
    }

    record StringValue(String value) implements RuntimeValue {
        public StringValue {
            Objects.requireNonNull(value, "value must not be null");
        }

        @Override
        public ResolvedType type() {
            return ScalarType.STRING;
        }

        @Override
        public Object raw() {
            return value;
        }
    }

    record TimeValue(LocalTime value) implements RuntimeValue {
        public TimeValue {
            Objects.requireNonNull(value, "value must not be null");
        }

        @Override
        public ResolvedType type() {
            return ScalarType.TIME;
        }

        @Override
        public Object raw() {
            return value;
        }
    }

    record VectorValue(List<RuntimeValue> elements) implements RuntimeValue {
        public VectorValue {
            Objects.requireNonNull(elements, "elements must not be null");
        }

        @Override
        public ResolvedType type() {
            return VectorType.INSTANCE;
        }

        @Override
        public Object raw() {
            return elements;
        }
    }
}
