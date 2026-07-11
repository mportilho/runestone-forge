# Foldable Data Conversion Service

Runestone Toolkit will split deterministic conversion from operational conversion: `DataConversionService` is the foldable-safe service used by compilation, cache, and compiled plans, while `RuntimeDataConversionService` is the operational service used when completeness or runtime behavior matters more than folding guarantees. This separation avoids letting classpath order, process defaults, mutable return values, or runtime-oriented converters change the result of constant folding.

## Considered Options

- Keep one conversion service for both compilation and runtime: rejected because the current service uses implicit provider discovery, process defaults, and runtime fallback behavior that cannot safely define compiled constants.
- Keep `DataConversionService` as the generic runtime service and add a new foldable service: rejected because the compiler-facing name should carry the strongest deterministic contract and prevent accidental use of non-foldable converters.
- Split the services and make foldable conversion explicit: accepted because it gives compilation a stable conversion profile while allowing runtime conversion to remain fast and complete.

## Consequences

- `DataConversionService` exposes a `ConversionContext`, an audit-friendly `conversionProfileIdentity()`, a derived `conversionProfileHash()`, and `copyFoldableValue(...)` for isolating constants embedded in compiled plans.
- `DataConverter` rules are explicit foldable converters: they declare `sourceType()`, `targetType()`, and `ruleIdentity()`, receive `ConversionContext`, and must return non-null foldable-safe values for non-null sources.
- `RuntimeDataConversionService` and runtime converters are separate contracts. Runtime converters may have different implementations for the same source and target types, optimized for speed or operational completeness rather than folding safety.
- The foldable standard service does not use implicit `ServiceLoader` discovery. Its standard converters are explicit, deterministic, and identified in the conversion profile; runtime service discovery can remain operational.
- Conversion lookup is deterministic: exact converters win, assignable converters are selected by most specific source-type distance, ambiguous matches fail, and duplicate exact converters fail at service creation.
- Foldable conversion uses explicit context instead of process defaults. The deterministic standard context is UTC plus `Locale.ROOT`; runtime services may use a system context.
- Foldable values are safe to embed by immutability or defensive copying. Mutable values such as dates, arrays, collections, and maps may be foldable only when copied or isolated according to the service contract.

## Implementation Direction

- First, make `runestone-toolkit` break cleanly to the new foldable contracts: explicit standard converters, no foldable `ServiceLoader`, no `DelegateDataConversionService`, no `throwIfNull`, and no legacy public constructors on `DefaultDataConversionService`.
- Then introduce the runtime conversion service and migrate operational consumers such as `dynamic-filter-resolver` to `RuntimeDataConversionService`.
- Then migrate `exp-mk3` compilation and boundary coercion to receive `DataConversionService` directly and derive environment identity from `conversionProfileHash()`.
- Finally, reinforce tests around deterministic profile identity, hash derivation, converter ambiguity, temporal context, defensive copying, and non-foldable value rejection.
