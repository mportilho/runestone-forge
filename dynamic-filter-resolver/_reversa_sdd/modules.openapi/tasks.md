# Modules OpenAPI, Tarefas de Implementação

> Spec operacional gerada pelo Reversa Writer em 2026-05-16.
> Escala de confiança: 🟢 CONFIRMADO, 🟡 INFERIDO, 🔴 LACUNA.

## Pré-requisitos

- [ ] 🟢 `core` implementado.
- [ ] 🟢 SpringDoc OpenAPI e Jakarta Bean Validation disponíveis.
- [ ] 🟢 Fixtures de validação para schemas disponíveis.

## Tarefas

- [ ] T-OAI-01, Implementar `DynaFilterOperationCustomizer.customize`.
  - Origem no legado: `src/main/java/com/runestone/dynafilter/modules/openapi/DynaFilterOperationCustomizer.java:52-84`
  - Critério de pronto: percorre parâmetros, detecta annotations dinâmicas, remove parâmetro técnico e customiza filtros.
  - Confiança: 🟢

- [ ] T-OAI-02, Implementar `customizeParameter`.
  - Origem no legado: `DynaFilterOperationCustomizer.java:91-137`
  - Critério de pronto: omite constantes, trata `Dynamic`, `IsIn`, `IsNull` e parâmetros existentes/novos.
  - Confiança: 🟢

- [ ] T-OAI-03, Implementar schema comum.
  - Origem no legado: `DynaFilterOperationCustomizer.java:139-180`
  - Critério de pronto: resolve schema por field/JsonView, aplica descrição/default e validações.
  - Confiança: 🟢

- [ ] T-OAI-04, Implementar descoberta de nome do parâmetro técnico.
  - Origem no legado: `DynaFilterOperationCustomizer.java:185-193`
  - Critério de pronto: prioriza `@Parameter.name`, depois `ParameterNameDiscoverer`, depois reflection parameter name.
  - Confiança: 🟢

- [ ] T-OAI-05, Implementar `SchemaValidationUtils`.
  - Origem no legado: `src/main/java/com/runestone/dynafilter/modules/openapi/SchemaValidationUtils.java:40-92`
  - Critério de pronto: aplica constraints numéricas, string e array, incluindo annotations compostas.
  - Confiança: 🟢

## Tarefas de Teste

- [ ] TT-OAI-01, Testar validações numéricas, string, array e annotation composta. 🟢
- [ ] TT-OAI-02, Criar teste direto para `DynaFilterOperationCustomizer` com `@Conjunction`. 🟡
- [ ] TT-OAI-03, Criar teste para filtro com `constantValues`. 🟡
- [ ] TT-OAI-04, Criar teste para `Dynamic`, `IsIn` e `IsNull`. 🟡
- [ ] TT-OAI-05, Criar teste para `@DisjunctionFrom` isolado documentado no OpenAPI. 🟢

## Lacunas Pendentes

- 🟡 Falta cobertura direta do customizer principal.
- 🟢 Corrigir a duplicidade de `Disjunction.class` na condição inicial e incluir `DisjunctionFrom.class`, conforme confirmação do usuário.
