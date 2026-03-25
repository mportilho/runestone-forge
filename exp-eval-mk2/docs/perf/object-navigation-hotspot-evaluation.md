# exp-eval-mk2 — Avaliacao de Hot Spots de `compute()` com Objetos Navegaveis

**Data:** 2026-03-25  
**Modulo:** `exp-eval-mk2`  
**Escopo:** fase de execucao apenas  
**Commit analisado:** `60788704` (`Object navigation WIP`)

## 1. Objetivo

Avaliar se a implementacao de objetos navegaveis introduzida no commit `60788704`
apresenta gargalos relevantes no caminho de execucao do metodo `compute()` da API publica:

- `MathExpression.compute(Map<String, Object>)`
- `LogicalExpression.compute(Map<String, Object>)`

O foco desta avaliacao foi o custo observado pelo chamador em tempo de execucao:

```text
MathExpression.compute(Map)
  -> ExpressionRuntimeSupport.computeMath(Map)
  -> buildValues(...)
  -> createExecutionScope(...)
  -> MathEvaluator / LogicalEvaluator
  -> AbstractObjectEvaluator.evaluatePropertyChain(...)
```

Ficaram explicitamente fora do escopo:

- construcao de `ExpressionEnvironment`
- compilacao da expressao
- resolucao semantica
- construcao de plano de execucao

## 2. Arquivos e pontos de codigo avaliados

- `exp-eval-mk2/src/main/java/com/runestone/expeval2/api/MathExpression.java`
- `exp-eval-mk2/src/main/java/com/runestone/expeval2/internal/runtime/ExpressionRuntimeSupport.java`
- `exp-eval-mk2/src/main/java/com/runestone/expeval2/internal/runtime/AbstractObjectEvaluator.java`
- `exp-eval-mk2/src/test/java/com/runestone/expeval2/perf/jmh/ObjectNavigationBenchmark.java`
- `exp-eval-mk2/src/test/java/com/runestone/expeval2/perf/jmh/ObjectNavigationBenchmarkRunner.java`
- `exp-eval-mk2/src/test/java/com/runestone/expeval2/perf/ObjectNavigationBenchmarkSupport.java`

Pontos principais do runtime:

- `MathExpression.compute(Map)` apenas delega para `runtime.computeMath(values)`.
- O custo real de cada chamada passa por `ExpressionRuntimeSupport.buildValues(...)`.
- A navegacao tipada cai em `invokeGetter(...)` e `invokeMethod(...)`.
- O fallback reflexivo cai em `resolvePropertyReflective(...)` e `invokeMethodReflective(...)`.

## 3. Metodologia

Foram usados dois tipos de medicao.

### 3.1 Benchmark JMH ja existente

O benchmark de referencia foi:

- `ObjectNavigationBenchmark`

Ele foi usado para comparar cenarios tipados e reflexivos com expressoes ja compiladas em
`@Setup(Level.Trial)`, deixando o custo da compilacao fora da medicao dos cenarios de runtime.

Os cenarios de runtime relevantes sao:

- `typedNestedProperty`
- `typedMethodNoArg`
- `typedMethodWithArgument`
- `reflectiveNestedProperty`
- `reflectiveMethodWithArgument`

Os cenarios de build/compile presentes na mesma classe nao fizeram parte desta avaliacao.

### 3.2 Profiling com `jcmd` + JFR

Para descoberta de hot spots reais da fase de execucao, foram feitas rodadas exploratorias com:

- `jcmd <pid> JFR.start ... settings=profile`

Perfis separados foram capturados para:

- `typedMethodWithArgument`
- `reflectiveMethodWithArgument`
- `reflectiveNestedProperty`

Artefatos gerados em `/tmp/performance-benchmark/`:

- `object-navigation-typed-method.jfr`
- `object-navigation-reflective-method.jfr`
- `object-navigation-reflective-property.jfr`
- `object-navigation-typed-method.out`
- `object-navigation-reflective-method.out`
- `object-navigation-reflective-property.out`

Observacao importante:

- O attach via `jcmd` precisou ser executado fora do sandbox.
- As rodadas com JFR usaram `forks(0)` e `-Djmh.ignoreLock=true`, portanto os numeros absolutos
  dessas rodadas sao exploratorios. Para comparacao mais confiavel entre cenarios, a referencia
  principal continua sendo o JSON do benchmark existente:
  `/tmp/performance-benchmark/object-navigation-20260324-after-refactor.json`.

## 4. Avaliacao do benchmark existente

## 4.1 O que esta bom

O benchmark atual modela bem o custo publico de `compute(Map)`:

- as expressoes sao compiladas uma vez por trial
- os mapas de entrada sao preconstruidos e reutilizados
- os objetos navegados sao variados por indice circular
- existe separacao entre cenarios tipados e reflexivos

Isso torna o benchmark adequado para medir o custo percebido por quem chama a API.

## 4.2 O que limita a descoberta de hot spots de execucao

O benchmark mistura, na mesma classe:

- cenarios de runtime
- `buildTypedEnvironment`
- cenarios de compilacao

Isso nao invalida o benchmark, mas atrapalha o uso direto do runner para investigacao de hot spots
de execucao.

O `ObjectNavigationBenchmarkRunner` tambem nao esta ideal para este objetivo porque:

- inclui a classe inteira, sem filtrar apenas runtime
- habilita `StackProfiler` sempre
- roda com `forks(0)`

Para descobrir hot spots do runtime, o uso recomendado e:

- filtrar apenas os metodos de runtime
- usar `GCProfiler` para `B/op`
- usar `jcmd` + JFR para CPU/alocacao
- evitar `StackProfiler` na rodada principal de medicao

## 4.3 Conclusao sobre o cenario

O cenario do benchmark e bom para a avaliacao funcional de custo de execucao de objetos navegaveis,
mas o runner atual nao esta ajustado para investigacao de hot spots. O benchmark pode ser mantido;
o que precisa mudar no uso e o protocolo da coleta.

## 5. Resultados de referencia do benchmark

Os numeros abaixo vieram da rodada consolidada em:

- `/tmp/performance-benchmark/object-navigation-20260324-after-refactor.json`

### 5.1 Cenarios tipados

| Cenario | Score (ns/op) | B/op |
|---|---:|---:|
| `typedMethodNoArg` | 53.84 | 176.00 |
| `typedMethodWithArgument` | 94.39 | 216.00 |
| `typedNestedProperty` | 93.95 | 136.00 |

### 5.2 Cenarios reflexivos

| Cenario | Score (ns/op) | B/op |
|---|---:|---:|
| `reflectiveMethodWithArgument` | 2565.46 | 1784.04 |
| `reflectiveNestedProperty` | 5636.20 | 3458.48 |

### 5.3 Leitura dos numeros

- O caminho tipado esta em ordem de dezenas de nanossegundos.
- O fallback reflexivo esta em ordem de microssegundos.
- O custo reflexivo e muito maior tanto em latencia quanto em alocacao.

Comparacoes aproximadas:

- `reflectiveMethodWithArgument` e cerca de **27x** mais lento que `typedMethodWithArgument`
- `reflectiveNestedProperty` e cerca de **60x** mais lento que `typedNestedProperty`

## 6. Resultados exploratorios com `jcmd` / JFR

Rodadas exploratorias com JFR anexado:

| Cenario | Score (ns/op) | B/op | Observacao |
|---|---:|---:|---|
| `typedMethodWithArgument` | 183.31 | 304.00 | overhead do profiling |
| `reflectiveMethodWithArgument` | 4852.44 | 1976.04 | overhead do profiling |
| `reflectiveNestedProperty` | 8783.44 | 3792.15 | overhead do profiling |

Esses numeros confirmam a mesma ordem relativa da rodada de referencia:

- tipado continua barato
- reflexivo continua caro

O valor principal do JFR nesta avaliacao nao foi o numero absoluto, e sim a localizacao dos hot spots.

## 7. Hot spots encontrados

## 7.1 `buildValues(...)` domina o caminho tipado

No caminho tipado, os samples de CPU e alocacao ficaram concentrados em:

- copia de `defaultValues` para novo `HashMap`
- iteracao de `userValues.entrySet()`
- lookup de simbolos externos
- `result.put(...)`

Em termos de codigo:

- `ExpressionRuntimeSupport.buildValues(...)`

Leitura:

- a navegacao tipada em si nao apareceu como gargalo principal
- o custo de preparar o mapa por chamada ainda pesa mais do que o acesso navegavel tipado

Em outras palavras: para o caso tipado, o custo de `compute(Map)` e mais influenciado por
materializacao de bindings do que pela feature de objetos navegaveis.

## 7.2 Caminho tipado: sem evidencia de gargalo relevante na navegacao

Os frames de navegacao tipada aparecem, mas abaixo de `buildValues(...)`:

- `invokeGetter(...)`
- `invokeMethod(...)`

Isso indica que a implementacao tipada do commit `60788704` nao trouxe, nesta avaliacao,
um gargalo dominante no caminho de execucao.

## 7.3 Caminho reflexivo: gargalo claro em introspeccao repetida

No fallback reflexivo, os principais alocadores e samples apontaram para:

- `Class.getRecordComponents0()`
- `Method.copy()`
- `Method.invoke(...)`
- plumbing de `MethodHandleAccessorFactory`
- varredura repetida de `getDeclaredMethods()`

Em termos de codigo:

- `resolvePropertyReflective(...)`
- `invokeMethodReflective(...)`

O problema estrutural e que a resolucao reflexiva e refeita em cada acesso:

- para propriedades de `record`, chama `cls.getRecordComponents()` toda vez
- recupera `comp.getAccessor()` toda vez
- chama `setAccessible(true)` toda vez
- invoca via `Method.invoke(...)`
- para metodos, percorre `getDeclaredMethods()` a cada chamada

Esse custo explica o salto de latencia e alocacao nos cenarios reflexivos.

## 7.4 O gargalo do commit `60788704`

Conclusao objetiva:

- **nao ha evidencia de gargalo importante no caminho tipado**
- **ha evidencia forte de gargalo no fallback reflexivo**

Portanto, o gargalo ajustavel introduzido ou tornado visivel com a feature de objetos navegaveis
esta concentrado no modo reflexivo, nao no modo tipado.

## 8. Causa tecnica dos custos

### 8.1 Tipado

O custo tipado vem principalmente de:

- `buildValues(...)`
- criacao/copia de `HashMap`
- lookup de simbolos
- coercao por chamada

O acesso navegavel propriamente dito usa o plano compilado e `MethodHandle` ja resolvido, entao
o custo marginal da navegacao permanece baixo.

### 8.2 Reflexivo

O custo reflexivo vem principalmente de:

- introspeccao repetida de `record`
- procura repetida de metodos declarados
- criacao/adaptacao de acessores reflexivos
- invocacao reflexiva sem cache

Esse caminho transforma cada acesso navegavel em trabalho de descoberta, nao apenas de execucao.

## 9. Recomendacoes

## 9.1 Prioridade alta: cache de resolucao reflexiva

Adicionar cache por:

- `Class + propertyName`
- `Class + methodName + arity`

O cache deve armazenar o resolvedor pronto para uso:

- `Method`
- `Field`
- idealmente `MethodHandle`

Objetivo:

- eliminar chamadas repetidas a `getRecordComponents()`
- eliminar varredura repetida de `getDeclaredMethods()`
- reduzir `Method.copy()` e custos de adaptacao reflexiva

## 9.2 Prioridade alta: especializar acesso reflexivo de `record`

Para `record`, o fallback atual paga descoberta completa em toda avaliacao.

Uma melhoria direta e resolver o accessor uma vez e reusar:

- `RecordComponent` nao deve ser revarrido por chamada
- o accessor deve ser cacheado por classe e nome da propriedade

## 9.3 Prioridade media: trocar `Method.invoke(...)` por `MethodHandle` cacheado

Mesmo no fallback reflexivo, apos a primeira descoberta o ideal e:

- resolver uma vez
- invocar muitas vezes

Se o cache armazenar `MethodHandle` adaptado ao formato esperado do runtime, o custo de execucao
deve cair de forma importante.

## 9.4 Prioridade media: reduzir custo de `buildValues(...)`

Se o objetivo for melhorar `compute(Map)` no caminho tipado, o principal candidato nao e a
navegacao, e sim `buildValues(...)`.

Direcoes possiveis:

- fast path para mapas pequenos
- evitar `new HashMap<>(defaultValues)` quando nao ha overrides
- usar estrutura overlay em vez de copiar o mapa inteiro
- reduzir lookup duplo de simbolo e descriptor por entrada

## 10. Decisao desta avaliacao

### 10.1 Sobre gargalo da nova implementacao

**Resultado:** existe gargalo ajustavel, mas ele esta no fallback reflexivo.

### 10.2 Sobre o caminho tipado

**Resultado:** o caminho tipado esta saudavel para a feature de navegacao. O custo dominante da
chamada continua sendo preparacao de bindings por execucao.

### 10.3 Sobre o benchmark

**Resultado:** o benchmark e adequado, mas o runner atual nao e o melhor protocolo para analise de
hot spots de execucao.

## 11. Proximo passo recomendado

Se a proxima iteracao for de otimizacao, a ordem recomendada e:

1. Implementar cache de acesso reflexivo para propriedades e metodos.
2. Medir novamente `reflectiveMethodWithArgument` e `reflectiveNestedProperty`.
3. So depois disso atacar `buildValues(...)` se a meta passar a ser reduzir o custo total de
   `compute(Map)` no caminho tipado.

## 12. Artefatos desta avaliacao

Arquivos usados nesta analise:

- `/tmp/performance-benchmark/object-navigation-20260324-after-refactor.json`
- `/tmp/performance-benchmark/object-navigation-typed-method.json`
- `/tmp/performance-benchmark/object-navigation-reflective-method.json`
- `/tmp/performance-benchmark/object-navigation-reflective-property.json`
- `/tmp/performance-benchmark/object-navigation-typed-method.jfr`
- `/tmp/performance-benchmark/object-navigation-reflective-method.jfr`
- `/tmp/performance-benchmark/object-navigation-reflective-property.jfr`

Comandos auxiliares usados:

```bash
jcmd <pid> JFR.start name=<name> settings=profile filename=<file>.jfr duration=24s
jfr summary <file>.jfr
jfr print --events jdk.ExecutionSample <file>.jfr
jfr print --events jdk.ObjectAllocationSample <file>.jfr
```

