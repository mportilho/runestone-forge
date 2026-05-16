# Legacy Mapping — Módulo `modules.openapi`

> Gerado pelo Reversa Archaeologist em 2026-05-16.
> Escala de confiança: 🟢 CONFIRMADO, 🟡 INFERIDO, 🔴 LACUNA.

## Pacotes e Responsabilidades

🟢 CONFIRMADO

| Área | Arquivos | Responsabilidade |
|---|---|---|
| SpringDoc customization | `src/main/java/com/runestone/dynafilter/modules/openapi/DynaFilterOperationCustomizer.java` | Transformar parâmetros técnicos de filtro dinâmico em parâmetros OpenAPI documentáveis. |
| Schema validation | `src/main/java/com/runestone/dynafilter/modules/openapi/SchemaValidationUtils.java` | Copiar constraints Jakarta Bean Validation de fields para schemas OpenAPI. |

## Arquivos de Produção Analisados

🟢 CONFIRMADO

| Arquivo | Papel |
|---|---|
| `src/main/java/com/runestone/dynafilter/modules/openapi/DynaFilterOperationCustomizer.java` | Implementa `OperationCustomizer`; lista filtros, remove parâmetro técnico e cria parâmetros OpenAPI por filtro. |
| `src/main/java/com/runestone/dynafilter/modules/openapi/SchemaValidationUtils.java` | Aplica `@Min`, `@Max`, `@DecimalMin`, `@DecimalMax`, `@PositiveOrZero`, `@Size` e `@Pattern` em `Schema<?>`. |

## Testes Usados Como Evidência

🟢 CONFIRMADO

| Teste | Evidência |
|---|---|
| `src/test/java/com/runestone/dynafilter/modules/openapi/TestSchemaValidationUtils.java` | Valida cópia de constraints numéricas, string, array e annotation composta para schemas OpenAPI. |
| `src/test/java/com/runestone/dynafilter/modules/openapi/tools/ObjectValidations.java` | Fixture com fields anotados para testes de validation schema. |
| `src/test/java/com/runestone/dynafilter/modules/openapi/tools/ParticipantName.java` | Annotation composta que confirma descoberta de constraints por `AnnotationUtils.findAnnotation`. |

## Lacunas de Cobertura

🟡 INFERIDO

| Lacuna | Evidência |
|---|---|
| Não há teste direto para `DynaFilterOperationCustomizer`. | A busca no pacote de testes `modules.openapi` encontrou apenas `TestSchemaValidationUtils`. |
| Possível ausência de suporte a `@DisjunctionFrom` isolado na condição inicial. | `DynaFilterOperationCustomizer.java:63-64` verifica `Disjunction.class` duas vezes. |
