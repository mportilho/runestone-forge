# Dependencias — dynamic-filter-resolver

Gerado em: 2026-05-16T16:40:42Z

## Gerenciador de Pacotes

- 🟢 **CONFIRMADO** — Maven via `pom.xml`.
- 🟢 **CONFIRMADO** — Empacotamento do modulo: `jar`.
- 🟢 **CONFIRMADO** — Parent Maven: `io.github.runestone-forge:runestone-forge:1.1.0.1-SNAPSHOT`.

## Plataforma

- 🟢 **CONFIRMADO** — Java 21 configurado no `pom.xml` raiz (`java.version`, `maven.compiler.source`, `maven.compiler.target`).
- 🟢 **CONFIRMADO** — Maven Compiler Plugin `3.15.0` configurado no parent.
- 🟢 **CONFIRMADO** — Maven Surefire Plugin `3.5.4` e Failsafe Plugin `3.5.4` configurados no parent.

## Propriedades do Modulo

| Propriedade | Valor | Fonte |
|---|---:|---|
| `spring.boot.version` | `4.0.2` | `dynamic-filter-resolver/pom.xml` |
| `springdocs.version` | `3.0.1` | `dynamic-filter-resolver/pom.xml` |
| `jmh.version` | `1.37` | `dynamic-filter-resolver/pom.xml` |

## Dependencias de Producao/Provided

| Dependencia | Versao | Escopo | Uso observado |
|---|---:|---|---|
| `io.github.runestone-forge:runestone-toolkit` | `${project.version}` via parent | compile | Conversao de dados (`DataConversionService`, `DefaultDataConversionService`). |
| `org.springframework.boot:spring-boot-starter-data-jpa` | gerenciada por Spring Boot `4.0.2` | provided | API JPA, repositories, `Specification`. |
| `org.springframework:spring-webmvc` | gerenciada por Spring Boot `4.0.2` | provided | `HandlerMethodArgumentResolver`, `WebMvcConfigurer`, contexto MVC. |
| `jakarta.servlet:jakarta.servlet-api` | gerenciada por Spring Boot `4.0.2` | provided | Acesso a `HttpServletRequest` no resolver MVC. |
| `org.springdoc:springdoc-openapi-starter-webmvc-api` | `3.0.1` | provided | `OperationCustomizer` e modelos OpenAPI. |

## Dependencias de Teste e Performance

| Dependencia | Versao | Escopo | Uso observado |
|---|---:|---|---|
| `com.h2database:h2` | gerenciada por Spring Boot `4.0.2` | test | Banco em memoria para testes JPA. |
| `org.springframework.boot:spring-boot-starter-test` | gerenciada por Spring Boot `4.0.2` | test | JUnit Jupiter, AssertJ, Mockito e utilitarios Spring Test. |
| `org.springframework.boot:spring-boot-data-jpa-test` | gerenciada por Spring Boot `4.0.2` | test | Testes com `@DataJpaTest`. |
| `org.openjdk.jmh:jmh-core` | `1.37` | test | Benchmarks JMH. |
| `org.openjdk.jmh:jmh-generator-annprocess` | `1.37` | test | Annotation processor do JMH. |

## Frameworks Detectados

- 🟢 **CONFIRMADO** — Spring Boot dependency management `4.0.2`.
- 🟢 **CONFIRMADO** — Spring Data JPA.
- 🟢 **CONFIRMADO** — Spring MVC.
- 🟢 **CONFIRMADO** — SpringDoc OpenAPI `3.0.1`.
- 🟢 **CONFIRMADO** — Jakarta Servlet API.
- 🟢 **CONFIRMADO** — H2 para testes.
- 🟢 **CONFIRMADO** — JUnit Jupiter, AssertJ e Mockito via `spring-boot-starter-test`.
- 🟢 **CONFIRMADO** — JMH `1.37` para microbenchmarks.

## Observacoes

- 🟢 **CONFIRMADO** — As dependencias Spring e SpringDoc principais estao em escopo `provided`, coerente com biblioteca que integra aplicacoes consumidoras.
- 🟡 **INFERIDO** — A runtime application real fica fora deste modulo; este projeto fornece infraestrutura reusable para consumidores Spring/JPA.
