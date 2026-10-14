# Context Map

## Contexts

- [Dynamic Filtering](./dynamic-filter-resolver/CONTEXT.md) - resolves declarative filter input into executable query conditions.
- [Runestone Toolkit](./runestone-toolkit/CONTEXT.md) - shared toolkit infrastructure and stable/runtime value conversion.
- [Expression Evaluator](./expression-evaluator/CONTEXT.md) - typed expression compiler and runtime.

## Relationships

- **Dynamic Filtering -> Runestone Toolkit**: Uses shared value conversion, assertions, memoization, and caching utilities.
- **Expression Evaluator -> Runestone Toolkit**: Uses stable value conversion during compilation and runtime conversion during evaluation.
