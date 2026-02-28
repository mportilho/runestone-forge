# GEMINI.md - Runestone Forge Context

## Project Overview
**Runestone Forge** is a modular Java development toolkit (JDK 21+) focused on expression evaluation, dynamic filtering, and common development utilities.

### Core Modules:
- **`runestone-toolkit`**: Foundational utilities including assertions (`Certify`), data conversion services, and thread-safe memoization suppliers/functions.
- **`expression-evaluator`**: A robust expression evaluation engine. Supports parsing text expressions into operation trees, sequential evaluation via `Calculator`, and detailed calculation memory tracking. Uses ANTLR4 for grammar.
- **`dynamic-filter-resolver`**: A system for generating and resolving dynamic filters (e.g., JPA Specifications) from statements. Integrates with Spring Data JPA and Spring WebMVC.

## Building and Running
The project uses Maven as its build tool.

### Key Commands:
- **Build All Modules**: `mvn clean install`
- **Run Tests**: `mvn test`
- **Run Specific Module Tests**: `mvn test -pl <module-name>` (e.g., `mvn test -pl expression-evaluator`)
- **Update Version**: `mvn versions:set -DnewVersion=<version> versions:commit`

### Test Requirements:
Unit tests require specific VM parameters on JDK 21+ to enable Mockito/ByteBuddy agent loading:
`-XX:+EnableDynamicAgentLoading -Dnet.bytebuddy.experimental=true`

## Development Conventions
- **Language**: Java 21 (uses modern features like Records and Switch Expressions).
- **Architecture**: Modular and interface-driven. High emphasis on extensibility (Visitors, Decorators, Providers).
- **Coding Style**:
  - Follows standard Java naming conventions.
  - Uses `Certify` (from toolkit) for input validation and defensive programming.
  - MIT Licensed.
- **Testing**:
  - JUnit 5 and AssertJ are the primary testing libraries.
  - Mockito is used for mocking.
  - Performance benchmarks are managed with JMH (found in `test` source sets of relevant modules).
- **Documentation**: Performance histories and technical reports are maintained in `docs/` directories within modules.
