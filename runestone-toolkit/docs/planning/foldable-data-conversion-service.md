# Foldable Data Conversion Service Implementation Plan

## Goal

Split deterministic, foldable-safe conversion from operational runtime conversion. `DataConversionService` becomes the compiler/cache-safe service; `RuntimeDataConversionService` becomes the operational service for runtime coercion.

## Fixed Decisions

- `DataConversionService` is deterministic and foldable-safe by contract.
- `RuntimeDataConversionService` has no folding guarantee and may optimize for speed or operational completeness.
- `ConversionContext.standard()` is deterministic: UTC plus `Locale.ROOT`.
- `ConversionContext.system()` is operational: `ZoneId.systemDefault()` plus `Locale.getDefault()`.
- `DataConversionService` exposes `conversionContext()`, `conversionProfileIdentity()`, `conversionProfileHash()`, `canConvert(...)`, `convert(...)`, and `copyFoldableValue(...)`.
- `conversionProfileIdentity()` returns a canonical audit string.
- `conversionProfileHash()` returns SHA-256 hex lowercase over UTF-8 bytes of the canonical identity.
- Foldable `DataConverter` rules declare `sourceType()`, `targetType()`, `ruleIdentity()`, and `convert(source, context)`.
- `ruleIdentity()` is mandatory and must match `[a-z0-9][a-z0-9._:-]*`.
- Runtime converters use a separate `RuntimeDataConverter` contract.
- Foldable standard converters are explicit through public `StandardConverters.all()`.
- Runtime standard converters are explicit through public `RuntimeStandardConverters.all()` and may also support `ServiceLoader` for runtime extension.
- The old `META-INF/services/com.runestone.converters.DataConverter` descriptor is removed; if runtime discovery is needed, use `META-INF/services/com.runestone.converters.RuntimeDataConverter`.
- `DelegateDataConversionService`, legacy public constructors, and `throwIfNull` are removed.

## Lookup Rules

- Validate converter definitions at service creation.
- Reject null `sourceType`, `targetType`, or `ruleIdentity`.
- Reject invalid `ruleIdentity` format.
- Reject duplicate exact `sourceType -> targetType` converters, even if their `ruleIdentity()` values match.
- Exact `sourceType -> targetType` wins.
- Assignable lookup requires exact requested `targetType` and compatible declared source type.
- Choose the assignable converter with the shortest source-type distance from the actual source type.
- Type distance uses the shortest path across superclass and interface edges.
- Equal shortest distance is ambiguity and throws a configuration exception in `canConvert(...)` and `convert(...)`.
- Registration order has no semantic effect and does not affect profile identity.

## Foldable Value Rules

- `convert(null, targetType)` returns `null`.
- `copyFoldableValue(null)` returns `null`.
- For non-null source values, converters must return non-null results or throw.
- `convert(...)` isolates/copies the result before returning.
- Unknown mutable values are rejected by `copyFoldableValue(...)`.
- Known immutable values may be returned directly.
- Dates, timestamps, arrays, collections, sets, and maps must be copied or isolated.
- Collections, sets, and maps reject null elements, keys, and values.
- Maps and sets preserve observable iteration order when copied.
- Arrays of reference types preserve null elements; arrays of primitive target type reject null source elements.

## Built-In Foldable Capabilities

- Safe identity for known immutable or defensively-copyable values.
- Enum conversion as identified built-ins, not materialized per enum class.
- Collection to array conversion as an identified built-in.
- Array to list conversion returning an immutable list and rejecting null elements.
- Array to array conversion with component conversion/copying and a new array every time.
- Numeric conversions are explicit identified converters, not special unprofiled service logic.

## Temporal Rules

- No foldable conversion uses process timezone or locale defaults.
- `LocalDateTime -> ZonedDateTime` uses `context.zoneId()`.
- `LocalDate -> ZonedDateTime` uses start of day in `context.zoneId()`.
- `OffsetDateTime -> ZonedDateTime` uses `atZoneSameInstant(context.zoneId())`.
- `String -> ZonedDateTime` may accept strings without zone/offset and fill the missing zone from `context.zoneId()`.
- Existing parsing should remain non-localized unless a converter explicitly uses `context.locale()`.

## Phase 1: Runestone Toolkit Foldable Contract

- Add `ConversionContext`.
- Update `DataConversionService` API.
- Update `DataConverter` API.
- Add `DataConverterConfigurationException`.
- Add `NonFoldableValueException`.
- Replace `DefaultDataConversionService` construction with `standard()` and `withConverters(context, converters)` factories.
- Add canonical identity and SHA-256 hash derivation.
- Add deterministic converter registry and lookup.
- Add foldable value copier/isolation logic.
- Adapt existing converter classes where semantics remain foldable-safe.
- Create new foldable implementations where runtime behavior must diverge.
- Remove `DelegateDataConversionService`.
- Remove foldable `ServiceLoader` usage and descriptor.
- Update toolkit tests and benchmarks.

## Phase 2: Runtime Conversion Service

- Add `RuntimeDataConversionService`.
- Add `RuntimeDataConverter`.
- Add `DefaultRuntimeDataConversionService.standard()` and `withConverters(context, converters)`.
- Add `RuntimeStandardConverters.all()`.
- Permit runtime `ServiceLoader` extension if useful through `META-INF/services/com.runestone.converters.RuntimeDataConverter`.
- Runtime identity conversion returns the same instance.
- Runtime converters also must not return null for non-null source values.

## Phase 3: Dynamic Filter Resolver Migration

- Change dynamic-filter operational APIs to depend on `RuntimeDataConversionService`.
- Replace `new DefaultDataConversionService()` with `DefaultRuntimeDataConversionService.standard()` or explicit runtime service construction.
- Update Spring configuration and tests.

## Phase 4: exp-mk3 Migration

- Keep compiler/boundary coercion on `DataConversionService`.
- Replace environment builder APIs that accept a manual conversion profile string with APIs that accept `DataConversionService` directly.
- Derive environment identity from `conversionProfileHash()`.
- Use `conversionProfileIdentity()` for audit/debug/test diagnostics.

## Phase 5: Verification

- Test deterministic canonical identity independent of converter registration order.
- Test hash derivation with SHA-256 lowercase hex.
- Test exact duplicate rejection.
- Test assignable specificity and ambiguity failures.
- Test UTC/`Locale.ROOT` standard context and system runtime context.
- Test temporal conversions with explicit context.
- Test defensive copies for dates, timestamps, arrays, collections, sets, and maps.
- Test null policies for converters, arrays, lists, collections, and maps.
- Run `mvn -pl runestone-toolkit test`, then module migrations with `mvn -pl <module> -am test`, then full `mvn test`.
