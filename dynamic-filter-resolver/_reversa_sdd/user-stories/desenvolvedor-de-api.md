# User Story — Desenvolvedor de API Spring/JPA

> Spec operacional gerada pelo Reversa Writer em 2026-05-16.
> Escala de confiança: 🟢 CONFIRMADO, 🟡 INFERIDO, 🔴 LACUNA.

## História

🟡 **INFERIDO** — Como desenvolvedor de APIs Spring/JPA, quero declarar filtros reutilizáveis por annotations e receber `Specification<?>` ou `ConditionalStatement` prontos no controller/repository, para evitar duplicação de Criteria API e manter a documentação OpenAPI alinhada com os filtros aceitos.

## Fluxo Narrativo

1. 🟢 O desenvolvedor declara filtros no parâmetro do controller ou em contrato externo com `@Conjunction`, `@Disjunction`, `@ConjunctionFrom`, `@DisjunctionFrom` e `@Filter`.
2. 🟢 O desenvolvedor habilita a configuração servlet dinâmica com a annotation de ativação do módulo JPA.
3. 🟢 Em runtime, Spring MVC chama `SpecificationDynamicFilterArgumentResolver`.
4. 🟢 O resolver usa o `core` para gerar `StatementWrapper` a partir de annotations e parâmetros HTTP.
5. 🟢 O módulo JPA converte a árvore em `Specification<?>` e aplica decorators, como fetching.
6. 🟢 O repository executa a consulta usando Spring Data JPA.
7. 🟢 O módulo OpenAPI remove o parâmetro técnico e documenta os parâmetros reais de filtro.

## Critérios de Aceitação

```gherkin
Cenário: consumir filtros dinâmicos em controller
Dado um controller Spring com parâmetro Specification anotado com filtros dinâmicos
Quando uma request enviar parâmetros válidos
Então o método deve receber uma Specification pronta para execução

Cenário: documentar filtros no OpenAPI
Dado o mesmo controller anotado
Quando SpringDoc gerar a operação OpenAPI
Então o parâmetro técnico deve ser removido
E os filtros requisitáveis devem aparecer como parâmetros documentados

Cenário: impor filtro constante
Dado um filtro com constantValues
Quando o usuário enviar request
Então o valor constante deve prevalecer sobre a entrada do usuário
E o parâmetro não deve aparecer como entrada no OpenAPI
```

## Units Envolvidas

| Unit | Papel na história | Confiança |
|---|---|---|
| `core` | Declarações, metadata, valores e statement tree. | 🟢 |
| `modules.jpa` | MVC resolver, Specification, repositories e fetch decorators. | 🟢 |
| `modules.openapi` | Documentação SpringDoc dos parâmetros filtráveis. | 🟢 |
| `test-support.performance` | Evidência de regressão/performance para mudanças nos fluxos críticos. | 🟢 |

## Lacunas

- 🔴 Segurança/autorização real pertence à aplicação consumidora, não a esta biblioteca.
- 🔴 Não há domínio final nem endpoint produtivo próprio no módulo analisado.
