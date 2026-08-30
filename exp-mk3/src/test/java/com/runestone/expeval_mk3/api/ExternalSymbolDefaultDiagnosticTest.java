package com.runestone.expeval_mk3.api;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

class ExternalSymbolDefaultDiagnosticTest {

    @Test
    void preservesScalarConversionDiagnostic() {
        Throwable failure = catchThrowable(() -> ExternalSymbol.withDefault(
                "amount", ScalarType.NUMBER, "not-a-number", ExternalSymbolOverwritePolicy.FIXED));

        assertFailure(
                failure,
                "external symbol 'amount' default cannot be converted to NUMBER: "
                        + "Character n is neither a decimal digit number, decimal point, nor \"e\" notation exponential mark.",
                NumberFormatException.class,
                "Character n is neither a decimal digit number, decimal point, nor \"e\" notation exponential mark.");
    }

    @Test
    void preservesObjectConversionDiagnostic() {
        Throwable failure = catchThrowable(() -> ExternalSymbol.withDefault(
                "customer",
                new ObjectType(String.class.getName()),
                7,
                ExternalSymbolOverwritePolicy.FIXED));

        assertFailure(
                failure,
                "external symbol 'customer' default cannot be converted to ObjectType[name=java.lang.String]: "
                        + "external symbol 'customer' default must be an instance of java.lang.String",
                IllegalArgumentException.class,
                "external symbol 'customer' default must be an instance of java.lang.String");
    }

    @Test
    void preservesCollectionAndMapConversionDiagnostics() {
        Throwable collectionFailure = catchThrowable(() -> ExternalSymbol.withDefault(
                "values",
                new CollectionType(ScalarType.STRING),
                Arrays.asList("valid", null),
                ExternalSymbolOverwritePolicy.FIXED));
        Throwable mapFailure = catchThrowable(() -> ExternalSymbol.withDefault(
                "labels",
                new MapType(ScalarType.STRING),
                Map.of(7, "seven"),
                ExternalSymbolOverwritePolicy.FIXED));

        assertFailure(
                collectionFailure,
                "external symbol 'values' default cannot be converted to CollectionType[elementType=STRING]: "
                        + "scalar target requires a non-null value",
                IllegalArgumentException.class,
                "scalar target requires a non-null value");
        assertFailure(
                mapFailure,
                "external symbol 'labels' default cannot be converted to MapType[valueType=STRING]: "
                        + "MapType values must be text-keyed",
                IllegalArgumentException.class,
                "MapType values must be text-keyed");
    }

    @Test
    void preservesMaterializationLimitDiagnostic() {
        Throwable failure = catchThrowable(() -> ExpressionEnvironment.builder()
                .maxMaterializedSize(1)
                .externalSymbol(
                        "values",
                        new CollectionType(ScalarType.STRING),
                        List.of("one", "two"),
                        ExternalSymbolOverwritePolicy.FIXED)
                .build());

        assertFailure(
                failure,
                "external symbol 'values' default cannot be converted to CollectionType[elementType=STRING]: "
                        + "external symbol 'values' default exceeds maxMaterializedSize 1",
                IllegalArgumentException.class,
                "external symbol 'values' default exceeds maxMaterializedSize 1");
    }

    private static void assertFailure(
            Throwable failure,
            String message,
            Class<? extends Throwable> causeType,
            String causeMessage) {
        assertThat(failure)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(message);
        assertThat(failure.getClass().getName())
                .isEqualTo("com.runestone.expeval_mk3.api.BoundaryCoercion$BoundaryCoercionFailure");
        assertThat(failure.getCause())
                .isExactlyInstanceOf(causeType)
                .hasMessage(causeMessage);
    }
}
