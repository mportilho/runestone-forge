# Modules OpenAPI, Estratégia de Testes

> Spec operacional gerada pelo Reversa Writer em 2026-05-16.
> Escala de confiança: 🟢 CONFIRMADO, 🟡 INFERIDO, 🔴 LACUNA.

## Testes Legados Relevantes

| Teste | Cobertura | Confiança |
|---|---|---|
| `TestSchemaValidationUtils.testIntegerValidations` | `@Min` e `@Max`. | 🟢 |
| `TestSchemaValidationUtils.testIntegerPositiveOrZero` | `@PositiveOrZero`. | 🟢 |
| `TestSchemaValidationUtils.testCustomAnnotation` | Annotation composta com `@Size` e `@Pattern`. | 🟢 |
| `TestSchemaValidationUtils.testDecimalMinMax` | `@DecimalMin` e `@DecimalMax`. | 🟢 |
| `TestSchemaValidationUtils.testListParameter` | `@Size` em array. | 🟢 |

## Testes Necessários

| ID | Cenário | Confiança |
|---|---|---|
| TC-OAI-01 | Remover parâmetro técnico e criar filtros de query. | 🟡 |
| TC-OAI-02 | Omitir `constantValues`. | 🟡 |
| TC-OAI-03 | `Dynamic` como array string minItems 2. | 🟡 |
| TC-OAI-04 | `IsIn` como array e `IsNull` como boolean. | 🟡 |
| TC-OAI-05 | Preservar parâmetro path existente e `required=true`. | 🟡 |
| TC-OAI-06 | Reconhecer `@ConjunctionFrom` e `@DisjunctionFrom`, incluindo `@DisjunctionFrom` isolado no customizer. | 🟢 |
| TC-OAI-07 | Aplicar Bean Validation em schemas numéricos, string e array. | 🟢 |

## Comandos de Verificação

```shell
mvn clean test -pl dynamic-filter-resolver -Dtest=TestSchemaValidationUtils
```
