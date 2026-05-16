# C4 Contexto — dynamic-filter-resolver

> Gerado pelo Reversa Architect em 2026-05-16.
> Escala de confiança: 🟢 CONFIRMADO, 🟡 INFERIDO, 🔴 LACUNA.

## Diagrama

```mermaid
flowchart TB
    Dev["Desenvolvedor de API Spring/JPA\n🟡 usa a biblioteca para declarar filtros"]
    Caller["Chamador HTTP\n🟡 envia parâmetros expostos pela aplicação consumidora"]
    App["Aplicação consumidora Spring MVC/JPA\n🟡 define controllers, entidades e repositories"]
    Lib["dynamic-filter-resolver\n🟢 biblioteca Java/Maven"]
    SpringData["Spring Data JPA\n🟢 Specification e repositories"]
    SpringMVC["Spring MVC\n🟢 argument resolver"]
    SpringDoc["SpringDoc OpenAPI\n🟢 documentação de parâmetros"]
    DB[("Banco da aplicação consumidora\n🟡 schema externo ao módulo")]
    H2[("H2 de testes\n🟢 fixtures JPA")]
    JMH["JMH\n🟢 benchmarks"]

    Dev -->|declara @Filter, @Conjunction, @Disjunction, @FilterTarget| App
    Dev -->|habilita @EnableDynamicFilterServletConfiguration| App
    Caller -->|query parameters e path variables| App
    App -->|parâmetros técnicos ConditionalStatement / Specification| Lib
    Lib -->|registra HandlerMethodArgumentResolver| SpringMVC
    Lib -->|gera Specification e predicados Criteria| SpringData
    SpringData -->|consulta entidades| DB
    Lib -->|customiza Operation e Parameter| SpringDoc
    App -->|expõe contrato documentado| SpringDoc
    Lib -. testes .-> H2
    Lib -. benchmarks .-> JMH
```

## Pessoas e Sistemas

| Elemento | Tipo | Relação com o sistema | Confiança |
|---|---|---|---|
| Desenvolvedor de API Spring/JPA | Pessoa | Declara filtros, decorators, fetches e entidade alvo. | 🟡 |
| Chamador HTTP | Pessoa/sistema externo | Envia query/path parameters documentados pela aplicação consumidora. | 🟡 |
| Aplicação consumidora | Sistema externo | Hospeda controllers, repositories, entidades, segurança e banco produtivo. | 🟡 |
| dynamic-filter-resolver | Sistema analisado | Fornece annotations, resolvers, adapters JPA e customização OpenAPI. | 🟢 |
| Spring Data JPA | Sistema/biblioteca externa | Executa `Specification<?>` e Criteria API. | 🟢 |
| Spring MVC | Sistema/biblioteca externa | Permite resolver parâmetros de controller. | 🟢 |
| SpringDoc OpenAPI | Sistema/biblioteca externa | Recebe parâmetros OpenAPI derivados dos filtros. | 🟢 |
| Banco da aplicação consumidora | Sistema externo | Armazena dados reais filtrados pela aplicação. | 🟡 |
| H2 de testes | Sistema externo de teste | Executa integração JPA no módulo. | 🟢 |
| JMH | Ferramenta externa | Mede hotspots da biblioteca. | 🟢 |

## Observações

🟢 **CONFIRMADO** — O módulo não expõe endpoints HTTP próprios de produção.

🟢 **CONFIRMADO** — Spring e SpringDoc estão em escopo `provided`, coerente com biblioteca integrada por aplicações consumidoras.

🔴 **LACUNA** — Segurança, banco produtivo, deployment e observabilidade ficam fora do escopo local deste módulo.
