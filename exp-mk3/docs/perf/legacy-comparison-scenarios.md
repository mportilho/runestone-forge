# Legacy/MK3 Performance Comparison Scenarios

This document is the short catalog of paired runtime scenarios comparing `expression-evaluator`
with `exp-mk3`. Measured results and investigation notes belong in
[`performance-history.md`](performance-history.md).

The benchmark implementation is
`com.runestone.expeval_mk3.perf.jmh.LegacyComparisonBenchmark`. Every scenario uses equivalent
environments and inputs for both engines. Environment construction, expression compilation, input
map construction, and equivalence checks run in trial setup. Each invocation measures selection of
one prepared input map followed by `compute(...)`; both engines use the same selection path.

## Scenarios

| Scenario | Expression or operation | Dynamic workload |
|---|---|---|
| Multiple variables | Arithmetic expression over 12 external numbers | Rotates prepared maps with changing numeric values |
| Function calls | Four calls to the pure three-argument `weighted` function | Uses the same 12-number maps as the arithmetic scenario |
| Object navigation | `customer.address.district.code = "D-100"` | Alternates registered object graphs that match or miss |
| Literal membership | `needle in [1, 2, 3, 4, 5, 6, 7]` | Alternates a late hit (`7`) and a miss (`99`) |
| Collection filter and index | `prices[?(@ > threshold)][0]` | Alternates thresholds whose first matching element differs |
| Dynamic conditional | `if enabled then gross * rate else fallback endif` | Alternates the selected branch and numeric inputs |
| Dynamic collection index | `prices[2]` | Alternates two externally supplied four-element number lists |
| Anchored regex | `text =~ "^[A-Z]{3}-\\d{4}$"` | Alternates matching and nonmatching text |
| Numeric comparison chain | Four comparisons joined by `xor` | Changes all four numbers while forcing every comparison to execute |

## Override Controls

The multiple-variable and function-call scenarios also have two controls:

- `WithoutOverrides`: calls `compute()` and uses environment defaults.
- `WithDefaultOverrides`: supplies all 12 defaults through the override map.

These controls isolate runtime input preparation from expression execution. Scenarios that pass an
override map intentionally include public input validation and boundary coercion in the measured
runtime cost.

When adding or changing a scenario, keep the paired methods on the same JMH state, validate result
equivalence during setup, and record the final protocol and measurements in `performance-history.md`.
