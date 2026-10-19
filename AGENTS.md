# AGENTS.md

## Instructions
- Keep `AGENTS.md` as the canonical agent instruction source. Load the `java-guidelines` skill before any work in this project.
- Create new files under any `docs/` directory only with explicit user permission or a task instruction requiring those files. A request to change code does not grant that permission.
- Before domain exploration or decisions, read `docs/agents/domain.md`; `CONTEXT-MAP.md` indexes the module glossaries and relationships.
- For issues and PRDs, use GitHub Issues in `mportilho/runestone-forge` through `gh`; read `docs/agents/issue-tracker.md`. Triage applies to issues, not PRs; read `docs/agents/triage-labels.md` before changing triage labels.

## Build and tests
- Use local `mvn` with JDK 21; there is no Maven wrapper. Run these commands from the repository root.
- Run the everyday suite: `mvn test`.
- Test one module and its reactor dependencies: `mvn -pl <module> -am test`.
- Test one class: `mvn -pl <module> -am -Dtest=<TestClass> -Dsurefire.failIfNoSpecifiedTests=false test`. The final property prevents upstream modules without that class from failing.
- Verify expression API changes: `mvn -pl expression-evaluator -am verify`. Unlike `test`, this also runs public API Javadoc with doclint.
- Update project version: `mvn versions:set -DnewVersion=<version> versions:commit`.
- Preserve Surefire's `-XX:+EnableDynamicAgentLoading -Dnet.bytebuddy.experimental=true` flags; Mockito/ByteBuddy tests rely on them.
- JPA integration tests run in the `test` phase with embedded H2. Spring/Hibernate SQL output remains visible even with `mvn -q test`.
- `runestone-toolkit/src/test/resources/junit-platform.properties` enables JUnit parallel execution for that module.
- JMH benchmarks live under `src/test/java` but do not run as part of the everyday suite. Expression stress and performance checks are opt-in; see below.

## Module boundaries
- The Maven reactor contains `runestone-toolkit`, `dynamic-filter-resolver`, and `expression-evaluator`. Both feature modules depend on the toolkit, not on each other.
- Toolkit foldable and runtime conversion are independent contracts. Register built-ins explicitly in `StandardConverters` or `RuntimeStandardConverters`; there is no converter SPI discovery.
- Keep concrete converters under `com.runestone.converters.impl.stable` or `.impl.runtime` and their related subpackages, not in the public `com.runestone.converters` package.
- Dynamic filter Spring/JPA/MVC/Servlet/Springdoc dependencies are `provided`; H2 and Spring Boot test support are test-scoped.
- Servlet integration is opt-in through `@EnableDynamicFilterServletConfiguration`, not Spring Boot autodiscovery.
- `JpaFilterOperationService` registers built-in operations. Add extension operations through `JpaFilterOperationContributor` rather than editing consumers.

## Expression evaluator
- `ExpressionEngine` is the compilation entrypoint in `com.runestone.expeval.api`; compiler/runtime internals belong under `com.runestone.expeval.internal`.
- Follow `expression-evaluator/docs/planning/plano-implementacao-expression-evaluator-v2.md`; performance is an acceptance criterion, not a final cleanup step.
- The grammar source is `expression-evaluator/src/main/antlr4/com/runestone/expeval/internal/grammar/ExpressionEvaluator.g4`. Maven generates the lexer/parser under `target/generated-sources`; keep generated sources uncommitted.
- Documentation is test input: marked `runestone` examples execute, and tests check documented precedence and the diagnostic registry. Update their reference docs alongside behavior changes.
- Run stress checks with `mvn -pl expression-evaluator -am -Pstage12-stress verify`; this profile requires Maven to run on Eclipse Temurin 21.
- Before performance work, read `expression-evaluator/docs/perf/stage12-gates.md` for the JMH smoke command and full gate runner. The full runner requires Temurin 21 and Python 3, replaces prior artifacts in its output directory, and writes results under `expression-evaluator/target/stage12` by default.
