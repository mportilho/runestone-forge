# Core, Estratégia de Testes

> Spec operacional gerada pelo Reversa Writer em 2026-05-16.
> Escala de confiança: 🟢 CONFIRMADO, 🟡 INFERIDO, 🔴 LACUNA.

## Testes Legados Relevantes

| Teste | Cobertura | Confiança |
|---|---|---|
| `TestDefaultStatementGenerator` | Precedência de valores, defaults, constantes, expressão, validações de tamanho/nome. | 🟢 |
| `TestAnnotationStatementGenerator` | `NoOpStatement`, constantes, required, expressão, negação e filtros decorados. | 🟢 |
| `TestStatementGeneratorWithDynamicFilters` | Operações dinâmicas positivas, negadas, `IN`, `BT` e erros de formato. | 🟢 |
| `TestTypeAnnotationUtils` | Extração por annotation/type/interface, cache, eviction e cópia defensiva. | 🟢 |

## Suíte Mínima para Reimplementação

| ID | Cenário | Deve validar | Confiança |
|---|---|---|---|
| TC-CORE-01 | Metadata por annotation direta | `FilterAnnotationData` é extraído e validado. | 🟢 |
| TC-CORE-02 | Metadata por interface/superclasse/meta-annotation | Descoberta recursiva preserva contrato legado. | 🟢 |
| TC-CORE-03 | Cache de metadata | Entradas equivalentes fazem hit e array externo mutado não altera chave. | 🟢 |
| TC-CORE-04 | `NoOpStatement` | Ausência de filtros aplicáveis não retorna `null`. | 🟢 |
| TC-CORE-05 | Required ausente | Geração falha explicitamente. | 🟢 |
| TC-CORE-06 | Precedência de valores | `constantValues > request > defaultValues`. | 🟢 |
| TC-CORE-07 | `ValueExpressionResolver` | Strings e arrays de string são resolvidos; erro é encapsulado. | 🟢 |
| TC-CORE-08 | Operação dinâmica | Códigos suportados, negação, `IN`, `BT` e formatos inválidos. | 🟢 |
| TC-CORE-09 | Árvore lógica | AND/OR, sub-statements e negação preservados. | 🟢 |
| TC-CORE-10 | Decorators | Composição em ordem e falha em retorno nulo. | 🟢 |
| TC-CORE-11 | `findFilterField` com generics não triviais | Deve falhar com `DynamicFilterConfigurationException` explícita. | 🟢 |
| TC-CORE-12 | Sub-statement vazio negado | Comportamento precisa ser caracterizado antes de refatorar. | 🟡 |

## Comandos de Verificação

```shell
mvn clean test -pl dynamic-filter-resolver -Dtest=TestDefaultStatementGenerator
mvn clean test -pl dynamic-filter-resolver -Dtest=TestAnnotationStatementGenerator
mvn clean test -pl dynamic-filter-resolver -Dtest=TestStatementGeneratorWithDynamicFilters
mvn clean test -pl dynamic-filter-resolver -Dtest=TestTypeAnnotationUtils
```

## Riscos de Teste

- 🟢 **CONFIRMADO PELO USUÁRIO** — A reconstrução deve adicionar teste para collection raw, wildcard e generic type não materializado em `findFilterField`, esperando `DynamicFilterConfigurationException` explícita.
- 🟡 **INFERIDO** — Testes de caracterização devem ser criados antes de alterar comportamento de sub-statements vazios.
