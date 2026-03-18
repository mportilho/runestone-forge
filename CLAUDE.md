# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**runestone-forge** is a multi-module Java development toolkit with four modules:

1. **runestone-toolkit** — Core utilities (memoization, caching, data conversion, assertions). No Spring dependency.
2. **dynamic-filter-resolver** — Dynamic filtering framework for Spring Data JPA repositories, with OpenAPI integration.
3. **expression-evaluator** — ANTLR 4-based mathematical/logical expression parser and evaluator.
4. **exp-eval-mk2** — Work-in-progress second iteration of the expression evaluator (currently scaffolded, no source yet). Uses Caffeine for caching.

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
mvn versions:set -DnewVersion=1.0.0 versions:commit
```

> **JDK 21 test note**: Tests require `-XX:+EnableDynamicAgentLoading -Dnet.bytebuddy.experimental=true` for Mockito/ByteBuddy. These flags are already configured in each module's `pom.xml` under `maven-surefire-plugin`.
>
> **Sandbox note**: In restricted sandboxed environments, the full Maven test suite may still fail during Mockito initialization with ByteBuddy self-attach errors even with the Surefire flags configured. If that happens, rerun the same Maven test command outside the sandbox before treating it as a code regression.

## Architecture

### runestone-toolkit
Foundation library used by the other two modules. Key packages:
- `com.runestone.memoization` — `MemoizedFunction`, `MemoizedSupplier` for caching function results.
- `com.runestone.utils.cache` — `LruCache`, `BoundedCache` implementations.
- `com.runestone.converters` — Data type conversion service.

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

## Tech Stack

- **Java 21**, **Maven 3.9+**
- **Spring Boot 4.0.2** (dynamic-filter-resolver only)
- **ANTLR 4.13.1** (expression-evaluator)
- **JUnit 5**, **AssertJ**, **Mockito 5** — testing
- **JMH 1.37** — microbenchmarks (in `benchmark/` and `perf/` test packages)

## Agent Skills

- ALWAYS load the java-guidelines skill if present when working with Java files on this project.

## Local ANTLR Tool Jar

To avoid downloading the ANTLR tool repeatedly, reuse the local jar at:

`~/dev/git/temp/antlr4-4.13.1.jar`

Tool dependencies cached locally:

- `~/dev/git/temp/antlr-lib/antlr-runtime-3.5.3.jar`
- `~/dev/git/temp/antlr-lib/ST4-4.3.4.jar`
- `~/dev/git/temp/antlr-lib/antlr4-runtime-4.13.1.jar`
- `~/dev/git/temp/antlr-lib/icu4j-72.1.jar`

ANTLR regeneration command:

```shell
java -cp ~/dev/git/temp/antlr4-4.13.1.jar:~/dev/git/temp/antlr-lib/antlr-runtime-3.5.3.jar:~/dev/git/temp/antlr-lib/ST4-4.3.4.jar:~/dev/git/temp/antlr-lib/antlr4-runtime-4.13.1.jar:~/dev/git/temp/antlr-lib/icu4j-72.1.jar \
  org.antlr.v4.Tool \
  -Dlanguage=Java \
  -visitor \
  -listener \
  -Xexact-output-dir \
  -o /home/marcelo/dev/git/runestone-forge/exp-eval-mk2/src/main/java/com/runestone/expeval2/grammar/language \
  /home/marcelo/dev/git/runestone-forge/exp-eval-mk2/src/main/antlr4/com/runestone/expeval2/grammar/language/ExpressionEvaluatorV2.g4
```

For diagnostics such as `-Xlog`, also pass `-o /tmp/antlr-diagnostics` (or another scratch directory) to avoid generating parser artifacts under the ANTLR source tree.
