# AGENTS.md

This file provides guidance to AI agents (Gemini CLI, Claude Code, etc.) when working with code in this repository.

## Project Overview

**runestone-forge** is a multi-module Java development toolkit with four modules:

1. **runestone-toolkit** — Core utilities (memoization, caching, data conversion, assertions). No Spring dependency.
2. **dynamic-filter-resolver** — Dynamic filtering framework for Spring Data JPA repositories, with OpenAPI integration.
3. **expression-evaluator** — ANTLR 4-based mathematical/logical expression parser and evaluator.
4. **exp-eval-mk2** — Second iteration of the expression evaluator, fully implemented. Richer type system (scalar, vector, unknown), compilation pipeline with semantic resolution, and Caffeine-based expression cache.

## Build & Test Commands

```shell
# Build all modules
mvn clean install

# Run all tests
mvn clean test

# Run integration tests
mvn verify

# Run tests for a single module
mvn clean test -pl expression-evaluator

# Run a single test class
mvn clean test -pl expression-evaluator -Dtest=ExpressionCalculatorTest

# Run a single test method
mvn clean test -pl expression-evaluator -Dtest=ExpressionCalculatorTest#methodName

# Update project version
mvn versions:set -DnewVersion=1.2.0 versions:commit
```

> **JDK 21 test note**: Tests require `-XX:+EnableDynamicAgentLoading -Dnet.bytebuddy.experimental=true` for Mockito/ByteBuddy. These flags are already configured in each module's `pom.xml` under `maven-surefire-plugin`.
>
> **Sandbox note**: In restricted sandboxed environments, the full Maven test suite may still fail during Mockito initialization with ByteBuddy self-attach errors even with the Surefire flags configured. If that happens, rerun the same Maven test command outside the sandbox before treating it as a code regression.

## Architecture

### runestone-toolkit
Foundation library used by the other modules. Key packages:
- `com.runestone.memoization` — `MemoizedFunction`, `MemoizedSupplier` for caching function results.
- `com.runestone.converters` — Data type conversion service.
- `com.runestone.assertions` — `Asserts` and `Certify` for design-by-contract and validations.

Uses Caffeine for caching internal structures.
### dynamic-filter-resolver
Enables annotating REST controller parameters with filter definitions that are automatically resolved to JPA `Specification` objects.
- `com.runestone.dynafilter.core` — Framework-agnostic filter core: models, operations, statement generation, and the `DynamicFilterResolver` interface.
- `com.runestone.dynafilter.modules.jpa` — Spring Data JPA integration: `DynamicFilterJpaRepository`, auto-configuration, `ArgumentResolver`, `WebMvcConfigurer`.
- `com.runestone.dynafilter.modules.openapi` — SpringDoc OpenAPI integration.

Key pattern: `FilterOperation<R>` strategy interface with multiple JPA `Specification` implementations (Equals, Like, Between, Greater, etc.).

### expression-evaluator
Parses and evaluates expressions using an ANTLR 4 grammar (`ExpressionEvaluator.g4`).
- `com.runestone.expeval.expression.calculator` — `ExpressionCalculator` is the main entry point.
- `com.runestone.expeval.operation` — Composite tree of `AbstractOperation` nodes: `values` (constants/variables), `math` (log, trig, etc.), `logic` (boolean), `datetime`.
- Supports arbitrary-precision arithmetic via Big-Math 2.3.2.

Key pattern: `AbstractOperation` forms a composite tree; each node computes its result by recursively evaluating children. Variable values are supplied via context maps at evaluation time.

### exp-eval-mk2
A redesigned expression evaluator with a multi-phase compilation pipeline and richer type system.

Public API (package `com.runestone.expeval2.api`, `environment` and `catalog`):
- `ExpressionEnvironment` — built via `ExpressionEnvironmentBuilder`; configures the runtime (function catalog, external symbols, data conversion). Scoped by `ExpressionEnvironmentId` for cache keying.
- `MathExpression`, `LogicalExpression` — compiled, reusable expression objects.
- `FunctionCatalog`, `ExternalSymbolCatalog` — central repositories for available functions and external symbols.

Compilation pipeline (all internal, coordinated by `ExpressionCompiler`):
1. **Parse** — `ExpressionEvaluatorV2ParserFacade` wraps the ANTLR grammar; supports `SLL` with `LL` fallback via `PredictionStrategy`.
2. **AST** — `SemanticAstBuilder` maps the ANTLR parse tree to typed `Node` subclasses (`BinaryOperationNode`, `FunctionCallNode`, `ConditionalNode`, `VectorLiteralNode`, etc.) in `internal.ast`.
3. **Semantic resolution** — `SemanticResolver` walks the AST against `FunctionCatalog` and `ExternalSymbolCatalog`, producing a `SemanticModel` with `SymbolRef` bindings and typed `ResolvedType` annotations.
4. **Execution plan** — `ExecutionPlanBuilder` converts the resolved AST to an `ExecutionPlan` of `ExecutableNode` objects (e.g., `ExecutableBinaryOp`, `ExecutableFunctionCall`, `ExecutableConditional`).
5. **Evaluation** — `MathEvaluator` / `LogicalEvaluator` walk the `ExecutionPlan` within an `ExecutionScope`, returning `RuntimeValue`.

Type system: `ResolvedType` hierarchy with `ScalarType`, `VectorType`, and `UnknownType`; runtime values are `RuntimeValue` objects coerced via `RuntimeCoercionService`.

Compiled expressions are cached in `ExpressionCompiler` by `(source, environmentId, resultType)` using Caffeine (max 1 024 entries).

## Tech Stack

- **Java 21**, **Maven 3.9+**
- **Spring Boot 4.0.2** (dynamic-filter-resolver only)
- **SpringDoc OpenAPI 3.0.1** (dynamic-filter-resolver only)
- **ANTLR 4.13.1**
- **JUnit 5**, **AssertJ**, **Mockito 5** — testing
- **JMH 1.37** — microbenchmarks (in `benchmark/` and `perf/` test packages)

## Key Reference Documents

- **`exp-eval-mk2/docs/runtime-internals.md`** — Verified findings about the exp-eval-mk2 runtime: compilation pipeline, type system, `RuntimeValue` variants, `RuntimeCoercionService` coercion order, array-parameter coercion fix, overload disambiguation rules, `RuntimeValueFactory` wrapping logic, grammar syntax for date/datetime literals and type-hinted variables, and `ExpressionEnvironmentBuilder` convenience methods. Read this before exploring the exp-eval-mk2 internals from scratch.

## Agent Skills

- ALWAYS load the java-guidelines skill if present when working with Java files on this project.

## Regenerating the ANTLR Grammar

> **Important:** The jar at `~/dev/git/temp/antlr4-4.13.1.jar` is **not** a self-contained uber-jar and
> **does not work** directly — even with the sibling jars from `~/dev/git/temp/antlr-lib/` on the
> classpath, the JVM cannot initialize `org.antlr.v4.Tool`.  Use the Maven local cache instead.

### Step 1 — ensure the jars are present in the Maven cache

```shell
mvn dependency:get -Dartifact=org.antlr:antlr4:4.13.1
```

This is a no-op if the jar is already cached.

### Step 2 — regenerate

> **Why temp dir:** ANTLR always emits `.tokens` and `.interp` alongside the `.java` files.
> Generating directly into `src/main/java` would pollute the source tree with those artifacts.
> The command below generates into a temp directory and copies only the `.java` files across.

```shell
ANTLR_TOOL=~/.m2/repository/org/antlr/antlr4/4.13.1/antlr4-4.13.1.jar
ANTLR_RT=~/.m2/repository/org/antlr/antlr4-runtime/4.13.1/antlr4-runtime-4.13.1.jar
ANTLR3_RT=~/.m2/repository/org/antlr/antlr-runtime/3.5.3/antlr-runtime-3.5.3.jar
ST4=~/.m2/repository/org/antlr/ST4/4.3.4/ST4-4.3.4.jar
ICU4J=$(find ~/.m2/repository -name "icu4j-*.jar" | head -1)
GRAMMAR_OUT=exp-eval-mk2/src/main/java/com/runestone/expeval2/internal/grammar
TMPDIR=$(mktemp -d)

java -cp "${ANTLR_TOOL}:${ANTLR_RT}:${ANTLR3_RT}:${ST4}:${ICU4J}" \
  org.antlr.v4.Tool \
  -Dlanguage=Java \
  -visitor \
  -listener \
  -Xexact-output-dir \
  -o "${TMPDIR}" \
  exp-eval-mk2/src/main/antlr4/com/runestone/expeval2/internal/grammar/ExpressionEvaluatorV2.g4

cp "${TMPDIR}"/*.java "${GRAMMAR_OUT}/"
rm -rf "${TMPDIR}"
```

For diagnostics such as `-Xlog`, omit the `cp`/`rm` steps and inspect `${TMPDIR}` directly.
