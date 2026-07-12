# Runtime Converters Are Native Runtime Rules

Runtime standard converters are native runtime rules, not adapters over foldable converters, even when they convert the same source and target types. The runtime catalog optimizes for predictable hot-path operational behavior and does not promise foldable identity, defensive isolation, or complete environment-dependent conversion coverage; converters that may depend on DNS, filesystem providers, application class loading, or similar operational resources are opt-in runtime converters instead of standard runtime converters.
