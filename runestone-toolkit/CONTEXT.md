# Runestone Toolkit Context

Runestone Toolkit provides shared infrastructure concepts used by the other Runestone modules.

## Language

**Foldable Conversion**:
A pure conversion that can be computed during compilation when all inputs are constants, replacing the conversion call with a constant value.
_Avoid_: Runtime conversion, operational conversion

**Foldable Converter**:
A conversion rule with a stable identity and explicit context whose result is safe to embed in a compiled plan.
_Avoid_: Runtime converter

**Foldable Value**:
A constant value that can be embedded in a compiled plan without sharing mutable state with later readers.
_Avoid_: Shared mutable constant

**Runtime Converter**:
An operational conversion rule optimized for runtime completeness or speed, without a folding-safety guarantee.
_Avoid_: Foldable converter

**DataConversionService**:
A deterministic conversion service whose results are safe to compute during compilation when all inputs are constants.
_Avoid_: Runtime conversion service, operational conversion service

**RuntimeDataConversionService**:
An operational conversion service with no folding-safety guarantee.
_Avoid_: Foldable conversion service

**ConversionContext**:
The explicit locale and timezone context used by conversion services. In foldable conversions it is part of the conversion profile; in runtime conversions it controls operational behavior without creating a folding guarantee.
_Avoid_: Process defaults, system locale, system timezone

**Conversion Profile**:
The stable identity of a deterministic conversion service, including its context and ordered conversion rules. It has an audit-friendly canonical form and a hash form derived from that canonical form.
_Avoid_: Runtime converter registry
