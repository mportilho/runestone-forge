# Modules OpenAPI, Contratos Externos

> Spec operacional gerada pelo Reversa Writer em 2026-05-16.
> Escala de confiança: 🟢 CONFIRMADO, 🟡 INFERIDO, 🔴 LACUNA.

## Contrato SpringDoc

| Contrato | Entrada | Saída | Confiança |
|---|---|---|---|
| `OperationCustomizer` | `Operation`, `HandlerMethod` | `Operation` customizada | 🟢 |
| Parâmetro de filtro comum | `FilterRequestData.parameters` | `Parameter` query/path | 🟢 |
| Filtro `Dynamic` | Um parâmetro | `ArraySchema<String>` com `minItems=2` | 🟢 |
| Filtro `IsIn` | Um ou mais valores | `ArraySchema` | 🟢 |
| Filtro `IsNull` | Boolean lógico | `BooleanSchema` | 🟢 |
| Bean Validation | `AnnotatedElement` do field | Campos de validação no `Schema<?>` | 🟢 |

## Campos OpenAPI Gerados

- 🟢 `name`: nome do parâmetro de filtro.
- 🟢 `in`: `query`, exceto quando parâmetro existente é `path`.
- 🟢 `required`: vem de `FilterRequestData.required`; path parameter força `true`.
- 🟢 `description`: vem de `FilterRequestData.description`.
- 🟢 `schema.default`: aplicado quando existe exatamente um default value.

## Não Contratos

- 🟢 O módulo não gera arquivo OpenAPI completo por si só.
- 🟢 O módulo não define endpoints de produção.
- 🟡 A fidelidade final depende do handler real da aplicação consumidora e do SpringDoc runtime.
