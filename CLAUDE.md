# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**runestone-forge** is a multi-module Java development toolkit with three modules:

1. **runestone-toolkit** — Core utilities (memoization, caching, data conversion, assertions). No Spring dependency.
2. **dynamic-filter-resolver** — Dynamic filtering framework for Spring Data JPA repositories, with OpenAPI integration.
3. **expression-evaluator** — ANTLR 4-based mathematical/logical expression parser and evaluator.

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

# Update project version
mvn versions:set -DnewVersion=1.0.0 versions:commit
```

> **JDK 21 test note**: Tests require `-XX:+EnableDynamicAgentLoading -Dnet.bytebuddy.experimental=true` for Mockito/ByteBuddy. These flags are already configured in each module's `pom.xml` under `maven-surefire-plugin`.

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
