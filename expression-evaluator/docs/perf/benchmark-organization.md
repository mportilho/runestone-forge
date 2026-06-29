# Benchmark Organization

This document defines the package layout and JMH commands for `expression-evaluator` performance tests.

The benchmarks live under `src/test/java/com/runestone/expeval/perf/jmh`. Shared fixtures and data builders remain in `src/test/java/com/runestone/expeval/perf`. Internal test supports that need package-private production access remain under the matching `internal` test package.

## Groups

| Group | Package | Purpose |
|---|---|---|
| Startup / compilation | `com.runestone.expeval.perf.jmh.startup` | Cost paid before steady-state evaluation: parser strategy, parser warm-up, compile cache, cache miss allocation |
| Planning | `com.runestone.expeval.perf.jmh.planning` | End-to-end plan construction and plan-related compile baselines |
| Evaluation hot path | `com.runestone.expeval.perf.jmh.evaluation` | `compute(...)` cost for already compiled expressions |
| Navigation and collections | `com.runestone.expeval.perf.jmh.navigation` | Object navigation, collection navigation, vector transforms, membership operators |
| Runtime services | `com.runestone.expeval.perf.jmh.runtime` | Bindings, audit, coercion, direct function helpers, regex cache behavior |

## Package Tree

```text
com.runestone.expeval.perf.jmh
├── startup
│   ├── parsing
│   │   ├── ExpressionEvaluatorParsingBenchmark
│   │   └── ExpressionEvaluatorWarmupBenchmark
│   └── compilation
│       ├── CompilePathAllocationBenchmark
│       └── CompilePathAllocationBenchmarkRunner
├── planning
│   └── ExpressionEvaluatorExecutionPlanBenchmark
├── evaluation
│   ├── core
│   │   ├── BooleanValueBenchmark
│   │   ├── ObjectEvaluatorBenchmark
│   │   └── ObjectEvaluatorBenchmarkRunner
│   └── assignment
│       ├── DestructuringAssignmentBenchmark
│       └── DestructuringAssignmentBenchmarkRunner
├── navigation
│   ├── object
│   │   ├── ObjectNavigationBenchmark
│   │   └── ObjectNavigationBenchmarkRunner
│   └── collection
│       ├── CollectionNavigationBenchmark
│       ├── CollectionNavigationBenchmarkRunner
│       ├── MembershipBenchmark
│       ├── ScalarAggregationBenchmark
│       ├── VectorMapTransformBenchmark
│       └── VectorMapTransformBenchmarkRunner
└── runtime
    ├── audit
    │   └── AuditOverheadBenchmark
    ├── bindings
    │   ├── AssignmentExpressionBindingsBenchmark
    │   └── BindingsOverlayBenchmark
    ├── coercion
    │   └── ArrayCoercionBenchmark
    └── functions
        ├── MathFunctionsBenchmark
        └── StringFunctionsRegexBenchmark
```

## Commands

Run commands from the repository root unless noted otherwise. The preferred path is the shared JMH helper from the performance skill because it creates the output directory and falls back to direct `org.openjdk.jmh.Main` execution when Maven exec is not configured.

```bash
SKILL_SCRIPTS=/home/marcelo/.agents/skills/performance-benchmark/scripts
```

### Startup / Compilation

```bash
$SKILL_SCRIPTS/run-jmh.sh expression-evaluator \
  "com.runestone.expeval.perf.jmh.startup.parsing.ExpressionEvaluatorParsingBenchmark" \
  "/tmp/performance-benchmark/expeval-startup-parsing.json"

$SKILL_SCRIPTS/run-jmh.sh expression-evaluator \
  "com.runestone.expeval.perf.jmh.startup.parsing.ExpressionEvaluatorWarmupBenchmark" \
  "/tmp/performance-benchmark/expeval-startup-warmup.json"

$SKILL_SCRIPTS/run-jmh.sh expression-evaluator \
  "com.runestone.expeval.perf.jmh.startup.compilation.CompilePathAllocationBenchmark" \
  "/tmp/performance-benchmark/expeval-startup-compilation.json"
```

### Planning

```bash
$SKILL_SCRIPTS/run-jmh.sh expression-evaluator \
  "com.runestone.expeval.perf.jmh.planning.ExpressionEvaluatorExecutionPlanBenchmark" \
  "/tmp/performance-benchmark/expeval-planning.json"
```

`planning` is still mostly end-to-end. Add dedicated JMH classes here when `ExecutionPlanBuilder`, constant folding, symbol slot assignment, or navigation plan building need isolated measurement.

### Evaluation Hot Path

```bash
$SKILL_SCRIPTS/run-jmh.sh expression-evaluator \
  "com.runestone.expeval.perf.jmh.evaluation.core.ObjectEvaluatorBenchmark" \
  "/tmp/performance-benchmark/expeval-evaluation-core.json"

$SKILL_SCRIPTS/run-jmh.sh expression-evaluator \
  "com.runestone.expeval.perf.jmh.evaluation.core.BooleanValueBenchmark" \
  "/tmp/performance-benchmark/expeval-evaluation-boolean.json"

$SKILL_SCRIPTS/run-jmh.sh expression-evaluator \
  "com.runestone.expeval.perf.jmh.evaluation.assignment.DestructuringAssignmentBenchmark" \
  "/tmp/performance-benchmark/expeval-evaluation-assignment.json"
```

### Navigation And Collections

```bash
$SKILL_SCRIPTS/run-jmh.sh expression-evaluator \
  "com.runestone.expeval.perf.jmh.navigation.object.ObjectNavigationBenchmark" \
  "/tmp/performance-benchmark/expeval-navigation-object.json"

$SKILL_SCRIPTS/run-jmh.sh expression-evaluator \
  "com.runestone.expeval.perf.jmh.navigation.collection.CollectionNavigationBenchmark" \
  "/tmp/performance-benchmark/expeval-navigation-collection.json"

$SKILL_SCRIPTS/run-jmh.sh expression-evaluator \
  "com.runestone.expeval.perf.jmh.navigation.collection.MembershipBenchmark" \
  "/tmp/performance-benchmark/expeval-navigation-membership.json"

$SKILL_SCRIPTS/run-jmh.sh expression-evaluator \
  "com.runestone.expeval.perf.jmh.navigation.collection.ScalarAggregationBenchmark" \
  "/tmp/performance-benchmark/expeval-navigation-scalar-aggregation.json"

$SKILL_SCRIPTS/run-jmh.sh expression-evaluator \
  "com.runestone.expeval.perf.jmh.navigation.collection.VectorMapTransformBenchmark" \
  "/tmp/performance-benchmark/expeval-navigation-vector-map.json"
```

### Runtime Services

```bash
$SKILL_SCRIPTS/run-jmh.sh expression-evaluator \
  "com.runestone.expeval.perf.jmh.runtime.audit.AuditOverheadBenchmark" \
  "/tmp/performance-benchmark/expeval-runtime-audit.json"

$SKILL_SCRIPTS/run-jmh.sh expression-evaluator \
  "com.runestone.expeval.perf.jmh.runtime.bindings.BindingsOverlayBenchmark" \
  "/tmp/performance-benchmark/expeval-runtime-bindings-overlay.json"

$SKILL_SCRIPTS/run-jmh.sh expression-evaluator \
  "com.runestone.expeval.perf.jmh.runtime.bindings.AssignmentExpressionBindingsBenchmark" \
  "/tmp/performance-benchmark/expeval-runtime-assignment-bindings.json"

$SKILL_SCRIPTS/run-jmh.sh expression-evaluator \
  "com.runestone.expeval.perf.jmh.runtime.coercion.ArrayCoercionBenchmark" \
  "/tmp/performance-benchmark/expeval-runtime-coercion.json"

$SKILL_SCRIPTS/run-jmh.sh expression-evaluator \
  "com.runestone.expeval.perf.jmh.runtime.functions.MathFunctionsBenchmark" \
  "/tmp/performance-benchmark/expeval-runtime-math-functions.json"

$SKILL_SCRIPTS/run-jmh.sh expression-evaluator \
  "com.runestone.expeval.perf.jmh.runtime.functions.StringFunctionsRegexBenchmark" \
  "/tmp/performance-benchmark/expeval-runtime-string-functions.json"
```

## Local Runner Commands

Some benchmarks include small local runners for IDE or quick CLI feedback. These runners intentionally use reduced iterations or in-process execution and are not substitutes for release-quality JMH runs.

```bash
mvn test-compile -pl expression-evaluator

mvn exec:java -pl expression-evaluator \
  -Dexec.mainClass=com.runestone.expeval.perf.jmh.startup.compilation.CompilePathAllocationBenchmarkRunner \
  -Dexec.classpathScope=test

mvn exec:java -pl expression-evaluator \
  -Dexec.mainClass=com.runestone.expeval.perf.jmh.evaluation.core.ObjectEvaluatorBenchmarkRunner \
  -Dexec.classpathScope=test

mvn exec:java -pl expression-evaluator \
  -Dexec.mainClass=com.runestone.expeval.perf.jmh.evaluation.assignment.DestructuringAssignmentBenchmarkRunner \
  -Dexec.classpathScope=test

mvn exec:java -pl expression-evaluator \
  -Dexec.mainClass=com.runestone.expeval.perf.jmh.navigation.object.ObjectNavigationBenchmarkRunner \
  -Dexec.classpathScope=test

mvn exec:java -pl expression-evaluator \
  -Dexec.mainClass=com.runestone.expeval.perf.jmh.navigation.collection.CollectionNavigationBenchmarkRunner \
  -Dexec.classpathScope=test

mvn exec:java -pl expression-evaluator \
  -Dexec.mainClass=com.runestone.expeval.perf.jmh.navigation.collection.VectorMapTransformBenchmarkRunner \
  -Dexec.classpathScope=test
```

## History Notes

`performance-history.md` records the benchmark names emitted at the time of each historical run. Do not rewrite older benchmark labels only because packages moved; add a note in the new entry if a run crosses a package reorganization.
