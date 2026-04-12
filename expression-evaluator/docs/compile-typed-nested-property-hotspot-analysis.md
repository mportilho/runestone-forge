# Análise de Hotspots - `compileTypedNestedProperty`

**Data:** 12 de abril de 2026  
**Módulo:** `expression-evaluator`  
**Cenário:** `ObjectNavigationBenchmark.compileTypedNestedProperty`

## Objetivo

Identificar, no `HEAD` atual, quais trechos do pipeline de compilação ainda explicam a diferença de desempenho observada no cenário `compileTypedNestedProperty`.

## Metodologia

- Benchmark alvo: `ObjectNavigationBenchmark.compileTypedNestedProperty`
- Ferramenta de benchmark: JMH
- Ferramenta de profiling: JFR via `jcmd`
- Estratégia de coleta:
  - execução dedicada do benchmark com `forks(0)` apenas para descoberta
  - JVM iniciada com `-XX:+StartAttachListener`
  - gravação JFR com `settings=profile`
  - arquivo coletado durante a investigação: `/tmp/compileTypedNestedProperty-head.jfr`

## Resumo Executivo

O hotspot mais forte não está mais no `ExecutionPlanBuilder`. O principal custo residual está em `SemanticResolver.inferLiteralType(...)`, que tenta interpretar literais de texto como `date`, `time` e `datetime` antes de reconhecer que são strings entre aspas.

No cenário benchmarkado, a string `"BAT-100"` passa por três tentativas de parse temporal que falham por exceção. O JFR mostrou esse caminho como o maior consumidor de CPU e o maior gerador de alocação incidental.

## Principais Achados

| Área | Evidência | Interpretação |
|---|---|---|
| Inferência de literal em `SemanticResolver` | `DateTimeFormatter.parseResolved0` apareceu em 31 samples de CPU | Há uso de exceção como fluxo normal durante a classificação de literais |
| Parsing temporal por exceção | `jdk.JavaExceptionThrow = 5934` no resumo do JFR | O compile path está pagando custo repetido de exception construction |
| Alocação ligada a parse temporal | `DateTimeParseContext` apareceu 418 vezes em amostras de alocação | O custo não é só CPU; há churn relevante de memória |
| AST / parse tree do ANTLR | `ParserRuleContext.addAnyChild` apareceu 432 vezes e `CommonTokenFactory.create` 514 vezes | O parser e a parse tree ainda carregam custo relevante |
| `ExecutionPlanBuilder` | `buildLegacyPropertyChain` apareceu 8 vezes em CPU samples | Ainda participa do custo, mas deixou de ser o principal suspeito |
| Materialização de `SemanticModel` | `SemanticModel.<init>` apareceu 11 vezes em CPU samples e 245 vezes em allocation samples | As cópias imutáveis do modelo ainda geram churn secundário |
| Construção de `ExpressionCompiler` / Caffeine | `ExpressionCompiler.<init>` e `Caffeine.build()` apareceram em alocação | Isto é ruído do run de descoberta com `forks(0)` e `@Setup(Level.Invocation)`, não evidência suficiente de regressão no benchmark oficial |

## Hotspot Principal

### 1. Inferência de string passando por parse temporal

Trecho relevante:

- `SemanticResolver.inferLiteralType(...)`
- `SemanticResolver.canParseDate(...)`
- `SemanticResolver.canParseTime(...)`
- `SemanticResolver.canParseDateTime(...)`

Ordem atual:

1. tenta `date`
2. tenta `time`
3. tenta `datetime`
4. só depois reconhece string com aspas

Para um literal como `"BAT-100"`, isso cria um caminho caro e desnecessário. A evidência do JFR aponta que esta é hoje a causa mais provável do gap residual de `compileTypedNestedProperty`.

### Recomendação

Adicionar um fast path sintático para string literal antes de qualquer tentativa de parse temporal, ou ao menos guards baratos que evitem chamar `LocalDate.parse`, `LocalTime.parse` e `LocalDateTime.parse` para valores obviamente incompatíveis.

## Hotspots Secundários

### 2. Custo de parse tree do ANTLR

O JFR mostrou atividade relevante em:

- `ExpressionEvaluatorParser.logicalComparisonExpression(...)`
- `ParserRuleContext.addAnyChild(...)`
- `CommonTokenFactory.create(...)`

Isto sugere que parte do custo residual vem da própria expansão da gramática e da construção da árvore de parse, não apenas das fases semânticas posteriores.

### 3. Cópias e estruturas intermediárias do `SemanticModel`

Também apareceram sinais em:

- `SemanticModel.<init>(...)`
- `Map.copyOf(...)`
- cópias de listas imutáveis

Esse custo parece secundário, mas é recorrente e pode virar alvo depois que o hotspot de inferência de literal for atacado.

## O Que Não Parece Ser o Principal Problema

- `ExecutionPlanBuilder` ainda custa alguma CPU, mas não domina mais o profile
- `buildMemberChain(...)` e `buildMemberAccess(...)` do `SemanticAstBuilder` aparecem, mas com peso menor
- a construção de cache Caffeine apareceu no JFR, porém o run de descoberta foi intencionalmente não-forkado e com setup por invocação; isso não deve ser tratado como prova principal de regressão do caminho compilado

## Próximos Passos Recomendados

1. Ajustar `SemanticResolver.inferLiteralType(...)` para reconhecer strings antes do parse temporal por exceção.
2. Rerodar `ObjectNavigationBenchmark.compileTypedNestedProperty` com JMH normal para validar ganho real em `ns/op`.
3. Se o gap residual continuar relevante, investigar em seguida:
   - redução de churn em `SemanticModel`
   - redução de custo de parse tree no caminho lógico simples

## Conclusão

O profiling do commit atual indica que a diferença de desempenho em `compileTypedNestedProperty` é explicada principalmente por inferência de literal com parse temporal baseado em exceção, seguida por custo de parser/parse tree do ANTLR e, em nível menor, pela materialização do `SemanticModel`.

O próximo experimento com melhor relação risco/retorno é atacar o fast path de string literal em `SemanticResolver`.

## Resolução

O fast path de string literal foi aplicado em `SemanticResolver.inferLiteralType(...)`, movendo literais entre aspas para fora do caminho de parse temporal.

Validação executada no mesmo benchmark:

- `compileTypedNestedProperty`: `23394.905 ns/op` -> `13667.451 ns/op`
- `B/op`: `16400.862` -> `11356.168`

Isso confirma que o caminho de exceção era o principal custo residual no compile path desse cenário.
