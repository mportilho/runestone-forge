# AGENTS.md

## Project Shape
- Java 21 Maven reactor; there is no Maven wrapper, so use local `mvn`.
- Root modules are `runestone-toolkit`, `dynamic-filter-resolver`, `expression-evaluator`, and `exp-mk3`.
- `runestone-toolkit` provides shared assertions, memoization, date utilities, and `DataConversionService`; converter implementations are discovered through `src/main/resources/META-INF/services/com.runestone.converters.DataConverter`, so add new converters there.
- `dynamic-filter-resolver` depends on `runestone-toolkit`; Spring/JPA/WebMVC/Springdoc dependencies are `provided`, with H2/Spring Boot only in tests.
- `expression-evaluator` depends on `runestone-toolkit`; public entrypoints are in `com.runestone.expeval.api` and compiler/runtime internals are under `com.runestone.expeval.internal`.
- `exp-mk3` is a rebuild of `expression-evaluator` and is now under active development; when working on it, follow `exp-mk3/docs/planning/plano-implementacao-expression-evaluator-v2.md`, keep maximum performance as the implementation focus, and do not consult or read `expression-evaluator` under any circumstances. Build `exp-mk3` only from its own module contents and its dependencies.

## Commands
- Full verification: `mvn test`.
- Test one module and its reactor dependencies: `mvn -pl <module> -am test`.
- Test one class in a module with upstream dependencies: `mvn -pl <module> -am -Dtest=<TestClass> -Dsurefire.failIfNoSpecifiedTests=false test`.
- Test one class in `runestone-toolkit` only: `mvn -pl runestone-toolkit -Dtest=<TestClass> test`.
- Update project version: `mvn versions:set -DnewVersion=<version> versions:commit`.

## Build And Test Gotchas
- The root Surefire config already sets `-XX:+EnableDynamicAgentLoading -Dnet.bytebuddy.experimental=true`; keep it when touching test/plugin config because Mockito/ByteBuddy tests rely on it.
- `-am` plus `-Dtest=...` fails in upstream modules without `-Dsurefire.failIfNoSpecifiedTests=false` if those modules do not contain the named test.
- `mvn -q test` still emits Spring/Hibernate SQL logs from `dynamic-filter-resolver` integration tests; this is normal.
- `runestone-toolkit/src/test/resources/junit-platform.properties` enables JUnit parallel execution for that module.
- JMH benchmark/profiling classes live under `src/test/java`, but their `*Benchmark` names are not selected by Surefire defaults unless explicitly targeted.

## Generated Grammar Files
- `expression-evaluator/src/main/antlr4/.../ExpressionEvaluator.g4` and `exp-mk3/src/main/antlr4/.../ExpressionEvaluator.g4` are grammar sources.
- There is no ANTLR Maven plugin; generated parser/lexer files for `expression-evaluator` are committed under `src/main/java/com/runestone/expeval/internal/grammar`.
- If the grammar changes, regenerate and review the committed parser/lexer/token/interp files rather than assuming Maven will generate them during `test`.

## Dynamic Filter Notes
- Spring setup is opt-in through `@EnableDynamicFilterServletConfiguration`; there is no Spring Boot `AutoConfiguration.imports` or `spring.factories` resource.
- Built-in JPA operations are registered in `JpaFilterOperationService`; extension operations should go through `JpaFilterOperationContributor` rather than editing consumers.

## Instruction Sources
- `CLAUDE.md` only includes `@AGENTS.md`; keep this file as the canonical agent instruction source.
- Always load the `java-guidelines` skill before doing any work in this project.
