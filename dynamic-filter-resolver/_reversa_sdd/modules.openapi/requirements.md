# Modules OpenAPI

> Spec operacional gerada pelo Reversa Writer em 2026-05-16.
> Escala de confiança: 🟢 CONFIRMADO, 🟡 INFERIDO, 🔴 LACUNA.

## Visão Geral

🟢 **CONFIRMADO** — A unit `modules.openapi` integra filtros dinâmicos ao SpringDoc OpenAPI, removendo parâmetros técnicos e criando parâmetros documentáveis derivados de `FilterRequestData`.

## Responsabilidades

- 🟢 Implementar `OperationCustomizer` para handlers Spring MVC.
- 🟢 Detectar parâmetros anotados com filtros dinâmicos.
- 🟢 Remover o parâmetro técnico da operação OpenAPI.
- 🟢 Criar/atualizar parâmetros OpenAPI por filtro requisitável.
- 🟢 Omitir filtros com `constantValues`.
- 🟢 Modelar schemas especiais para `Dynamic`, `IsIn` e `IsNull`.
- 🟢 Inferir schema pelo field alvo e considerar `JsonView`.
- 🟢 Copiar Bean Validation para schemas numéricos, string e array.

## Regras de Negócio Técnico

- 🟢 Filtros com `constantValues` não devem ser documentados como entrada de usuário.
- 🟢 Operação `Dynamic` deve ser documentada como array de string com `minItems=2`.
- 🟢 Operação `Dynamic` com mais de um parâmetro deve falhar.
- 🟢 Operação `IsIn` deve ser documentada como array.
- 🟢 Operação `IsNull` deve ser documentada como booleano.
- 🟢 Parâmetro OpenAPI existente em `path` permanece `path` e é sempre `required=true`.
- 🟢 `defaultValues` só viram default de schema quando há exatamente um valor.
- 🟢 Constraints Bean Validation devem ser propagadas por tipo de schema.
- 🟢 A condição inicial verifica `Disjunction.class` duas vezes e não verifica `DisjunctionFrom.class`, então parâmetro anotado apenas com `@DisjunctionFrom` é ignorado pelo customizer (`DynaFilterOperationCustomizer.java:63-64`).

## Requisitos Funcionais

| ID | Requisito | Prioridade | Critério de Aceite |
|----|-----------|-----------|-------------------|
| RF-OAI-01 | 🟢 Customizar operações SpringDoc com filtros dinâmicos. | Must | Dado handler com parâmetro dinâmico, quando `customize` executar, então o parâmetro técnico deve ser removido e filtros devem virar parâmetros documentados. |
| RF-OAI-02 | 🟢 Omitir filtros constantes. | Must | Dado `FilterRequestData.constantValues`, quando customizar parâmetros, então nenhum parâmetro OpenAPI deve ser criado para esse filtro. |
| RF-OAI-03 | 🟢 Gerar schema especial para `Dynamic`. | Must | Dado operação `Dynamic`, então schema deve ser array de string com `minItems=2`. |
| RF-OAI-04 | 🟢 Gerar schema array para `IsIn`. | Must | Dado operação `IsIn`, então schema OpenAPI deve ser `ArraySchema`. |
| RF-OAI-05 | 🟢 Gerar boolean para `IsNull`. | Should | Dado operação `IsNull`, então schema deve ser booleano. |
| RF-OAI-06 | 🟢 Aplicar Bean Validation no schema. | Must | Dado field com constraints suportadas, então schema deve refletir limites/pattern/tamanho. |
| RF-OAI-07 | 🟢 Corrigir detecção de `@DisjunctionFrom` isolado. | Should | Dado parâmetro anotado só com `@DisjunctionFrom`, quando `customize` executar, então o filtro deve ser reconhecido e documentado no OpenAPI. |

## Critérios de Aceitação

```gherkin
Cenário: expandir filtro em parâmetros OpenAPI
Dado um handler com parâmetro técnico anotado com filtros
Quando DynaFilterOperationCustomizer.customize executar
Então o parâmetro técnico deve ser removido
E parâmetros de query devem ser criados para filtros requisitáveis

Cenário: omitir filtro constante
Dado um filtro com constantValues
Quando a operação OpenAPI for customizada
Então nenhum parâmetro deve ser criado para esse filtro

Cenário: aplicar Bean Validation
Dado field alvo com @Size e @Pattern
Quando SchemaValidationUtils.applyValidations executar
Então minLength, maxLength e pattern devem ser aplicados ao schema
```

## Rastreabilidade de Código

| Arquivo | Função / Classe | Cobertura |
|---------|-----------------|-----------|
| `modules/openapi/DynaFilterOperationCustomizer.java` | Customização de operação e parâmetros | 🟢 |
| `modules/openapi/SchemaValidationUtils.java` | Bean Validation para schemas | 🟢 |
| `modules/openapi/tools/ObjectValidations.java` | Fixture de validação | 🟢 |
| `modules/openapi/tools/ParticipantName.java` | Annotation composta de validação | 🟢 |
