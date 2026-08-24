# Dynamic Filtering

Dynamic Filtering turns declarative filter definitions and caller-supplied values into executable query conditions while keeping input interpretation separate from comparison semantics.

## Language

**Filter value**:
The input selected for a filter after resolving constant, request, and default values, and before conversion to the target attribute type.

**Filter value transformer**:
An ordered, filter-scoped rule that interprets one non-null filter value without defining how that value is compared.
_Avoid_: Filter operation, global converter
