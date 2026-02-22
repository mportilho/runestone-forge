# Calculator Memory Modes

`Calculator` now supports configurable memory strategies through `CalculatorOptions`.

## Usage

```java
Calculator calculator = new Calculator();
CalculatorOptions options = new CalculatorOptions(CalculatorMemoryMode.LAZY, 64);
List<CalculationMemory> memory = calculator.calculate(inputs, contextVariables, options);
```

If no options are provided, behavior remains backwards-compatible (`FULL` mode).

## Modes

- `FULL` (default): keeps full snapshots for `variables` and `contextVariables` on each step.
- `COMPACT`: keeps only `assignedVariables` (delta per step). `variables` and `contextVariables` are empty.
- `LAZY`: defers snapshot creation and reconstructs maps on first access, memoizing the result.

## Checkpoint Interval

`checkpointInterval` is used only by `LAZY` mode to rebuild `contextVariables` from periodic checkpoints + deltas.
A lower value uses more memory and less reconstruction work; a higher value reduces retained snapshots and increases reconstruction work.
