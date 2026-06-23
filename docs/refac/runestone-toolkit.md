# Refactoring Analysis: runestone-toolkit

## Module Summary

`runestone-toolkit` is the foundation module used by the other modules in the repository. It contains general-purpose utilities, mainly:

- Assertions and validation helpers.
- Data conversion contracts and implementations.
- Memoization utilities.
- Date utility functions.

The module is not organized around business features. Its dominant style is package-by-capability, which is appropriate for a toolkit module. The main maintainability concern is not boundary direction, but repeated converter implementations and a few packages that can grow into technical dumping grounds if not controlled.

## Current Structure

Approximate package concentration in `src/main/java`:

| Package | Approx. Java Files | Role |
|---|---:|---|
| `com.runestone.converters.impl.strings` | 39 | String-to-target converters. |
| `com.runestone.converters.impl.dates` | 13 | Date/time converters. |
| `com.runestone.converters.impl.numbers` | 9 | Number-to-target converters. |
| `com.runestone.memoization` | 6 | Memoized suppliers/functions and variants. |
| `com.runestone.converters.impl` | 5 | Conversion service implementation, loader, keys, collection/array support. |
| `com.runestone.converters` | 3 | Public conversion contracts. |
| `com.runestone.assertions` | 2 | Assertion APIs. |
| `com.runestone.utils` | 1 | General utility package. |

## Structure Assessment

- Organization: package-by-capability / technical toolkit.
- Clarity: mostly clear.
- Main drift: converter packages are large and repetitive, especially `strings`, `numbers`, and `dates`.
- Boundary issue: low. This module is intentionally generic and has no Spring/JPA dependency concerns in the sampled structure.
- Package risk: `com.runestone.utils` exists as a generic utility namespace. It is currently small, but should not become the default destination for unrelated helpers.

## Detailed Findings

| Severity | Issue | Evidence | Recommendation |
|---|---|---|---|
| Medium | Number converters repeat the same structure | `NumberToIntConverter` and `NumberToLongConverter` both switch on `null`, already-correct type, and generic `Number`, differing only by target type and `xxxValue()` call. Similar pattern exists across `NumberToShortConverter`, `NumberToByteConverter`, `NumberToFloatConverter`, `NumberToDoubleConverter`, `NumberToBigIntegerConverter`, `NumberToBigDecimalConverter`, `NumberToStringConverter`. | Introduce a package-private `NumberConversionSupport` or `AbstractNumberConverter<T>` to centralize null handling and conversion mechanics. Keep public converter classes as thin delegators if external registration depends on class names. |
| Medium | String converters are mostly one-method wrappers | `StringToUUIDConverter`, `StringToURIConverter`, `StringToPathConverter`, `StringToCurrencyConverter`, `StringToLocaleConverter`, numeric string converters, and other `StringTo*Converter` classes mostly call a parser/factory. | Introduce `SimpleStringConverter<T>` backed by `Function<String, T>` or register trivial converters through a factory in `DataConverterLoader`. Keep dedicated classes only where parsing rules, exception translation, charset handling, or formatting policy is non-trivial. |
| Medium | Date/time conversion logic is split by source and target type with likely repeated mechanics | Families include `UtilDateToLocalDate*`, `SqlDateToLocalDate*`, `TemporalToLocalDate*`, and temporal string converters. | Add `DateTemporalConversionSupport` for common `Date`, `java.sql.Date`, `Timestamp`, `Temporal`, and `ZoneId` conversion paths. |
| Low | `com.runestone.utils` is a generic package | `com.runestone.utils.DateUtils` is currently the only visible class under `utils`. | Prefer domain-specific packages such as `com.runestone.time` or keep date utilities close to converter implementation if not part of public API. Avoid adding unrelated utilities to this package. |
| Low | Converter implementation packages expose many public classes | Most converters appear to be implementation detail classes. | Make new implementation classes package-private by default unless they are intentionally part of the module API or loaded reflectively from outside the package. |

## Duplication Hotspots

### Number Converters

Representative pattern:

```java
public Integer convert(Number data) {
    return switch (data) {
        case Integer i -> i;
        case Number n -> n.intValue();
        case null -> throw new IllegalArgumentException("Cannot convert null to Integer");
    };
}
```

This repeats across numeric targets with only three variable pieces:

- Target type.
- Identity branch type.
- Conversion function.

Recommended minimal helper:

```java
final class NumberConversionSupport {
    private NumberConversionSupport() {
    }

    static <T> T convert(Number source, Class<T> targetType, Function<Number, T> converter) {
        if (source == null) {
            throw new IllegalArgumentException("Cannot convert null to " + targetType.getSimpleName());
        }
        if (targetType.isInstance(source)) {
            return targetType.cast(source);
        }
        return converter.apply(source);
    }
}
```

Then individual classes remain small and explicit:

```java
public Integer convert(Number data) {
    return NumberConversionSupport.convert(data, Integer.class, Number::intValue);
}
```

This is a small, low-risk refactor because it preserves public converter classes and behavior while centralizing repeated null/error handling.

### String Converters

The string converter package is large but coherent. The issue is not that `StringToX` classes exist; it is that many of them carry no unique logic.

Candidates for factory/delegation:

- `StringToUUIDConverter`
- `StringToURIConverter`
- `StringToURLConverter`
- `StringToPathConverter`
- `StringToPatternConverter`
- `StringToBigIntegerConverter`
- `StringToBigDecimalConverter`
- `StringToIntegerConverter`
- `StringToLongConverter`
- `StringToShortConverter`
- `StringToByteConverter`
- `StringToFloatConverter`
- `StringToDoubleConverter`

Keep dedicated implementations for cases that may need custom policy:

- Temporal parsing with explicit formatters.
- Charset handling.
- Locale/currency edge cases.
- Network/address parsing.
- Class loading.

### Date/Temporal Converters

The date converter packages are a good candidate for a support class because the conversion matrix creates repeated boilerplate. The target helper should avoid hiding behavior behind a large generic abstraction. Prefer explicit methods:

```java
final class DateTemporalConversionSupport {
    static LocalDate toLocalDate(Date value) { ... }
    static LocalDateTime toLocalDateTime(Date value) { ... }
    static LocalTime toLocalTime(Date value) { ... }
    static ZonedDateTime toZonedDateTime(Date value, ZoneId zoneId) { ... }
}
```

Avoid a generic `convert(Object, Class<?>)` helper in this module unless the public `DataConversionService` owns that responsibility. A too-generic helper would duplicate the service itself.

## Package Reorganization Proposal

Current structure is acceptable, but the converter implementation area can be made clearer:

```text
com.runestone.converters
  DataConverter
  DataConversionService

com.runestone.converters.impl
  DefaultDataConversionService
  DelegateDataConversionService
  DataConverterLoader
  ConverterPairKey
  CollectionArrayConversionSupport

com.runestone.converters.impl.number
  NumberConversionSupport
  NumberToIntConverter
  NumberToLongConverter
  ...

com.runestone.converters.impl.string
  SimpleStringConverter
  StringConversionSupport
  StringToUuidConverter
  ...

com.runestone.converters.impl.temporal
  DateTemporalConversionSupport
  StringTemporalConversionSupport
  UtilDateToLocalDateConverter
  TemporalToLocalDateConverter
  ...
```

Package renames from plural to singular are optional and probably not worth doing unless a broader package cleanup is planned. The first useful change is adding support classes inside existing packages.

## Recommended Refactoring Order

1. Add `NumberConversionSupport` and refactor `NumberTo*Converter` classes.
2. Add tests or parameterized tests around the numeric conversion matrix before changing all classes.
3. Add a small string converter factory/helper for parser-based converters.
4. Refactor only the trivial `StringTo*Converter` classes first; leave special parsing classes untouched.
5. Add `DateTemporalConversionSupport` for `Date`, `java.sql.Date`, `Timestamp`, and `Temporal` conversions.
6. Revisit `com.runestone.utils.DateUtils`; either keep it as intentional public utility or move date formatting support closer to converters.

## Suggested Tests

- Parameterized tests for `Number -> target` conversions, including `null`, identity type, wider/narrower numeric conversion, and `BigInteger`/`BigDecimal` behavior.
- Parameterized tests for simple `String -> target` converters, verifying exception behavior remains unchanged.
- Temporal conversion tests around timezone assumptions, SQL date semantics, leap day, and midnight/time-only conversions.

## Risk Notes

- Converter classes may be loaded reflectively by `DataConverterLoader`. Before deleting or making classes package-private, verify loader behavior and public compatibility expectations.
- Numeric conversions may intentionally preserve Java narrowing behavior. A helper must not add range validation unless explicitly desired.
- Date/time conversions often have hidden timezone assumptions. Centralization is valuable, but only after tests lock existing behavior.
