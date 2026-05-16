# C4 Containers — dynamic-filter-resolver

> Gerado pelo Reversa Architect em 2026-05-16.
> Escala de confiança: 🟢 CONFIRMADO, 🟡 INFERIDO, 🔴 LACUNA.

## Diagrama

```mermaid
flowchart LR
    subgraph Consumer["Aplicação consumidora Spring MVC/JPA 🟡"]
        Controller["Controllers\nparâmetros ConditionalStatement / Specification"]
        Entities["Entidades JPA\nmodelo real da aplicação"]
        Repositories["Repositories Spring Data\nJpaRepository / DynamicFilterJpaRepository"]
        Security["Segurança da aplicação\nSpring Security ou equivalente"]
    end

    subgraph Lib["dynamic-filter-resolver 🟢"]
        Core["core\nannotations, metadata, statements, operations"]
        Jpa["modules.jpa\nSpecification, MVC, repository, fetch decorators"]
        OpenAPI["modules.openapi\nOperationCustomizer e schemas"]
        Perf["test-support.performance\nJMH e fixtures"]
    end

    subgraph External["Tecnologias externas 🟢"]
        SpringMVC["Spring MVC"]
        SpringData["Spring Data JPA"]
        Criteria["Jakarta Criteria API"]
        SpringDoc["SpringDoc OpenAPI"]
        Toolkit["runestone-toolkit\nDataConversionService"]
        Caffeine["Caffeine caches"]
        H2[("H2 test database")]
        DB[("Banco produtivo da aplicação 🟡")]
    end

    Controller -->|annotations e request params| Jpa
    Jpa -->|usa modelo intermediário| Core
    OpenAPI -->|lista filtros requisitáveis| Core
    Jpa -->|converte valores| Toolkit
    Core -->|cache de annotations| Caffeine
    Jpa -->|cache de paths parseados| Caffeine
    Jpa -->|registra argument resolver| SpringMVC
    Jpa -->|gera Specification| SpringData
    SpringData --> Criteria
    SpringData --> DB
    Repositories --> SpringData
    Repositories --> Jpa
    OpenAPI --> SpringDoc
    Controller --> OpenAPI
    Security -. responsabilidade externa .-> Controller
    Perf --> Core
    Perf --> Jpa
    Perf --> H2
```

## Containers Lógicos

| Container | Tecnologia | Responsabilidade | Deploy | Confiança |
|---|---|---|---|---|
| `core` | Java 21 | Modelo agnóstico de filtros, annotations, extração de metadados, geração de statements e abstrações de resolver/decorator. | Dentro do jar da biblioteca. | 🟢 |
| `modules.jpa` | Java 21, Spring MVC, Spring Data JPA | Resolver MVC, conversão de statements para `Specification`, operações Criteria, repositories dinâmicos e fetch decorators. | Dentro do jar da biblioteca; ativado pela aplicação consumidora. | 🟢 |
| `modules.openapi` | Java 21, SpringDoc OpenAPI | Customizar documentação OpenAPI com parâmetros derivados dos filtros. | Dentro do jar da biblioteca; usado quando SpringDoc estiver no classpath. | 🟢 |
| `test-support.performance` | Java 21, JMH, Spring Test, H2 | Benchmarks e fixtures de performance/integração. | Escopo de teste, não produção. | 🟢 |
| Aplicação consumidora | Spring Boot / Spring MVC / Spring Data JPA | Define endpoints, entidades, autorização, dados reais e habilita a biblioteca. | Fora deste módulo. | 🟡 |
| Banco produtivo | Banco compatível com JPA provider | Armazena entidades reais consultadas por filtros. | Fora deste módulo. | 🟡 |

## Comunicações

| Origem | Destino | Protocolo/API | Dados | Confiança |
|---|---|---|---|---|
| Controller consumidor | `SpecificationDynamicFilterArgumentResolver` | Spring MVC SPI | `MethodParameter`, `NativeWebRequest`, query/path params. | 🟢 |
| `AnnotationStatementGenerator` | `TypeAnnotationUtils` | Chamada Java | `AnnotationStatementInput`, annotations e metadados. | 🟢 |
| `SpecificationDynamicFilterResolver` | `SpecificationStatementAnalyser` | Visitor/analyser Java | `StatementWrapper`, `AbstractStatement`. | 🟢 |
| `Specification*` | Criteria API | JPA Criteria | `Root`, `Path`, `CriteriaBuilder`, `Predicate`. | 🟢 |
| `DynamicFilterJpaRepositoryImpl` | `SimpleJpaRepository` | Spring Data JPA API | `Specification`, `Pageable`, `Sort`, `EntityGraph`. | 🟢 |
| `DynaFilterOperationCustomizer` | SpringDoc | OpenAPI model API | `Operation`, `Parameter`, `Schema`. | 🟢 |
| Benchmarks | H2 / Spring Test | JDBC/JPA via Spring | Fixtures `Person`, `Produto` e relacionamentos. | 🟢 |

## Limites de Responsabilidade

🟢 **CONFIRMADO** — A biblioteca decide como transformar contratos de filtro em statements, `Specification` e documentação.

🟢 **CONFIRMADO** — A aplicação consumidora decide quais filtros existem, quais campos podem ser expostos, autenticação/autorização e transações de negócio.

🔴 **LACUNA** — Não há container de runtime autônomo, fila, cache distribuído, serviço externo HTTP consumido, CI/CD local ou deployment próprio detectado neste módulo.
