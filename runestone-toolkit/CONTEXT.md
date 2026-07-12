# Runestone Toolkit Context

Runestone Toolkit provides shared infrastructure concepts used by the other Runestone modules.

## Language

**Foldable Conversion**:
A pure conversion that can be computed during compilation when all inputs are constants, replacing the conversion call with a constant value.
_Avoid_: Runtime conversion, operational conversion

**Foldable Converter**:
A conversion rule with a stable identity and explicit context whose result is safe to embed in a compiled plan.
_Avoid_: Runtime converter, shared conversion rule

**Foldable Value**:
A constant value that can be embedded in a compiled plan without sharing mutable state with later readers.
_Avoid_: Shared mutable constant

**Runtime Conversion Result**:
The immediate result of an operational conversion, with no isolation guarantee. It may be the original instance, a view over the source, or a mutable value when that is the fastest useful runtime behavior.
_Avoid_: Foldable value, isolated value

**Runtime Converter**:
An operational conversion rule owned by the runtime conversion contract, optimized for runtime completeness or speed, without a folding-safety guarantee.
_Avoid_: Foldable converter, foldable converter adapter, shared conversion rule

**Runtime Conversion Boundary**:
The runtime conversion service boundary that owns public null handling and converter-result validation. Runtime standard converters may assume this boundary and avoid duplicate defensive checks in their hot path.
_Avoid_: Per-rule defensive boundary

**Shared Conversion Rule**:
A misleading model where one conversion rule is treated as both foldable and runtime just because it converts the same source and target types. Foldable and runtime converters are independent rules even when their type pairs match.
_Avoid_: Common converter, universal converter

**DataConversionService**:
A deterministic conversion service whose results are safe to compute during compilation when all inputs are constants.
_Avoid_: Runtime conversion service, operational conversion service

**RuntimeDataConversionService**:
An operational conversion service with no folding-safety guarantee.
_Avoid_: Foldable conversion service

**Runtime Standard Converter**:
A runtime converter included in the default runtime catalog because it is predictable enough for common runtime paths and does not rely on external operational resources such as DNS, filesystem providers, or application class loading.
_Avoid_: Complete runtime catalog

**Runtime Opt-In Converter**:
A runtime converter available for explicit registration when operational completeness is needed, but excluded from the default runtime catalog because it may depend on external resources, environment-specific providers, or application class loading.
_Avoid_: Foldable converter, runtime standard converter

**ConversionContext**:
The explicit locale and timezone context used by conversion services. In foldable conversions it is part of the conversion profile; in runtime conversions it controls operational behavior without creating a folding guarantee.
_Avoid_: Process defaults, system locale, system timezone

**Conversion Profile**:
The stable identity of a deterministic conversion service, including its context and ordered conversion rules. It has an audit-friendly canonical form and a hash form derived from that canonical form.
_Avoid_: Runtime converter registry
