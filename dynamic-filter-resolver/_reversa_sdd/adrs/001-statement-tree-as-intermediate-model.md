# ADR 001 — Usar Árvore De Statements Como Modelo Intermediário

Data inferida: 2024-02 a 2024-03

## Status

🟢 **CONFIRMADO** — Aceito e vigente.

## Contexto

🟢 **CONFIRMADO** — O histórico mostra refatoração do `StatementGenerator` em `245304f`, implementação de `ConditionalStatement` em `01f2999` e suporte a `ConjunctionFrom`/`DisjunctionFrom` em `06c7b8c`.

🟢 **CONFIRMADO** — O código atual separa annotations e metadados (`core`) da execução JPA (`modules.jpa`) por meio de `StatementWrapper`, `AbstractStatement`, `LogicalStatement`, `CompoundStatement`, `NegatedStatement` e `NoOpStatement`.

## Decisão

🟢 **CONFIRMADO** — Representar filtros declarados por annotations como uma árvore lógica intermediária antes de adaptar para tecnologias concretas como Spring Data JPA `Specification`.

## Consequências

🟢 **CONFIRMADO** — O core permanece independente da implementação concreta de filtro, permitindo que o JPA seja apenas um adaptador.

🟢 **CONFIRMADO** — O mesmo metadado alimenta resolução MVC, OpenAPI e repositories dinâmicos.

🟢 **CONFIRMADO** — O modelo suporta composição por AND/OR, negação e ausência de filtro aplicável sem acoplar diretamente ao Criteria API.

🟡 **INFERIDO** — A complexidade do core aumenta, mas em troca há um ponto estável para futuras integrações além de JPA.
