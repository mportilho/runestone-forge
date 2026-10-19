# AGENTS.md

## Project Shape
- Java 21 Maven reactor; there is no Maven wrapper, so use local `mvn`.
- Root modules are `runestone-toolkit`, `dynamic-filter-resolver`, and `expression-evaluator`.
- `runestone-toolkit` provides shared assertions, memoization, date utilities, and stable/runtime conversion services. Built-in converters are registered explicitly in `StandardConverters` and `RuntimeStandardConverters`; there is no converter SPI discovery.
- Keep `DataConverter` and `RuntimeDataConverter` implementations in their related implementation packages, such as `com.runestone.converters.impl.stable` for foldable converters and `com.runestone.converters.impl.runtime` for runtime converters; do not move concrete converter implementations into the public `com.runestone.converters` package.
- `dynamic-filter-resolver` depends on `runestone-toolkit`; Spring/JPA/WebMVC/Springdoc dependencies are `provided`, with H2/Spring Boot only in tests.
- `expression-evaluator` depends on `runestone-toolkit`; public entrypoints are in `com.runestone.expeval.api` and compiler/runtime internals are under `com.runestone.expeval.internal`. Follow `expression-evaluator/docs/planning/plano-implementacao-expression-evaluator-v2.md` and keep maximum performance as the implementation focus.
- **Documentation file creation**: Create new files under `docs/` only with explicit user permission or an explicit task instruction requiring those files. Otherwise, ask for permission before creating them; do not infer permission from a request to change code.

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
- `expression-evaluator/src/main/antlr4/.../ExpressionEvaluator.g4` is the grammar source.
- `expression-evaluator` uses the `antlr4-maven-plugin` (declared in `expression-evaluator/pom.xml`); its generated parser/lexer sources are produced at build time under `target/generated-sources` and are intentionally not committed.

## Dynamic Filter Notes
- Spring setup is opt-in through `@EnableDynamicFilterServletConfiguration`; there is no Spring Boot `AutoConfiguration.imports` or `spring.factories` resource.
- Built-in JPA operations are registered in `JpaFilterOperationService`; extension operations should go through `JpaFilterOperationContributor` rather than editing consumers.

## Instruction Sources
- `CLAUDE.md` only includes `@AGENTS.md`; keep this file as the canonical agent instruction source.
- Always load the `java-guidelines` skill before doing any work in this project.

## Agent skills

### Issue tracker

Issues and PRDs are tracked in GitHub Issues for `mportilho/runestone-forge`; external PRs are not a triage surface. See `docs/agents/issue-tracker.md`.

### Triage labels

Canonical triage roles map directly to same-named GitHub labels. See `docs/agents/triage-labels.md`.

### Domain docs

Multi-context repo: `CONTEXT-MAP.md` indexes module glossaries and their relationships. Read `docs/agents/domain.md` before domain exploration or decisions.
