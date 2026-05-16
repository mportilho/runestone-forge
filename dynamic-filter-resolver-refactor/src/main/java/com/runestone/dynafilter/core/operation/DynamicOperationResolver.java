package com.runestone.dynafilter.core.operation;

import com.runestone.dynafilter.core.exception.DynamicFilterConfigurationException;
import com.runestone.dynafilter.core.model.FilterData;

import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public final class DynamicOperationResolver {

    private static final Map<String, Class<? extends DefinedFilterOperation>> OPERATIONS = Map.ofEntries(
            Map.entry("EQ", Equals.class),
            Map.entry("LT", Less.class),
            Map.entry("LE", LessOrEquals.class),
            Map.entry("GT", Greater.class),
            Map.entry("GE", GreaterOrEquals.class),
            Map.entry("LK", Like.class),
            Map.entry("SW", StartsWith.class),
            Map.entry("EW", EndsWith.class),
            Map.entry("IN", IsIn.class),
            Map.entry("BT", Between.class)
    );

    public FilterData resolve(DynamicOperationRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        if (!(request.value() instanceof Object[] rawValues)) {
            throw new DynamicFilterConfigurationException("Dynamic operation value must be an Object[]");
        }
        if (rawValues.length == 0 || !(rawValues[0] instanceof String rawCode)) {
            throw new DynamicFilterConfigurationException("Dynamic operation first value must be an operation code");
        }

        OperationCode operationCode = resolveOperationCode(rawCode);
        Object[] operationValues = Arrays.copyOfRange(rawValues, 1, rawValues.length);
        if (operationCode.operation().equals(IsIn.class)) {
            return isInFilter(request, operationCode, operationValues);
        }
        if (operationCode.operation().equals(Between.class)) {
            return betweenFilter(request, operationCode, operationValues);
        }
        return new FilterData(
                request.path(),
                new String[]{request.parameter()},
                request.targetType(),
                operationCode.operation(),
                operationCode.negate(),
                operationValues,
                request.modifiers(),
                request.description()
        );
    }

    private static OperationCode resolveOperationCode(String rawCode) {
        String code = rawCode.toUpperCase(Locale.ROOT);
        boolean negate = false;
        if (code.length() == 3 && code.startsWith("N")) {
            negate = true;
            code = code.substring(1);
        } else if (code.length() != 2) {
            throw new DynamicFilterConfigurationException("Invalid dynamic operation code: " + rawCode);
        }

        Class<? extends DefinedFilterOperation> operation = OPERATIONS.get(code);
        if (operation == null) {
            throw new DynamicFilterConfigurationException("Unknown dynamic operation code: " + rawCode);
        }
        return new OperationCode(operation, negate);
    }

    private static FilterData isInFilter(
            DynamicOperationRequest request,
            OperationCode operationCode,
            Object[] operationValues
    ) {
        Object value = operationValues.length == 1 ? operationValues[0] : operationValues;
        return new FilterData(
                request.path(),
                new String[]{request.parameter()},
                request.targetType(),
                operationCode.operation(),
                operationCode.negate(),
                new Object[]{value},
                request.modifiers(),
                request.description()
        );
    }

    private static FilterData betweenFilter(
            DynamicOperationRequest request,
            OperationCode operationCode,
            Object[] operationValues
    ) {
        if (operationValues.length != 2) {
            throw new DynamicFilterConfigurationException("Dynamic BT operation requires exactly two values");
        }
        return new FilterData(
                request.path(),
                new String[]{request.parameter() + "From", request.parameter() + "To"},
                request.targetType(),
                operationCode.operation(),
                operationCode.negate(),
                operationValues,
                request.modifiers(),
                request.description()
        );
    }

    private record OperationCode(Class<? extends DefinedFilterOperation> operation, boolean negate) {
    }
}
