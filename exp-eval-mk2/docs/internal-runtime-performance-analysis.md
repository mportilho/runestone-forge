# Análise de Oportunidades de Performance e Memória: Internal Runtime (MK2)

Esta análise identifica gargalos de desempenho e oportunidades de redução de alocação de memória no pacote `com.runestone.expeval2.internal.runtime` do módulo `exp-eval-mk2`, baseada na revisão do motor de execução e histórico de benchmarks.

## 1. Alocação de Vetores em Chamadas de Função (`AbstractObjectEvaluator.java`)
**Problema:** Cada chamada de função (`evaluateFunctionCall`) aloca um novo `Object[] args` baseado na sua aridade para passar ao descritor da função.
**Oportunidade:** Implementar caminhos especializados de `evaluate` para aridades comuns (1, 2 e 3) para eliminar a alocação de arrays. Para aridades maiores, considerar o uso de um pool de arrays via `ThreadLocal` ou reaproveitamento de buffers em execuções sequenciais do mesmo plano no mesmo thread.

## 2. Sobrecarga de Eventos de Auditoria (`AbstractObjectEvaluator.java` & `AuditCollector.java`)
**Problema:** O método `computeWithAudit()` aloca múltiplos objetos `AuditEvent` (ex: `VariableRead`, `FunctionCall`, `AssignmentEvent`) para cada nó no plano de execução. Em expressões complexas, isso aumenta significativamente a pressão no Garbage Collector e o tempo de execução.
**Oportunidade:** Introduzir um modo de "Auditoria Preguiçosa" (Lazy Audit) ou uma representação interna primitiva (ex: array de longs/objects) para armazenar os dados brutos da auditoria, instanciando os objetos `AuditEvent` finais apenas quando `buildTrace()` for chamado.

## 3. Coerção Ineficiente de BigDecimal (`RuntimeCoercionService.java`)
**Problema:** A implementação atual em `asNumber` utiliza `new BigDecimal(n.toString())` para tipos `Number` que não são `BigDecimal`. Embora seguro, o custo de `toString()` e o parsing subsequente é alto.
**Oportunidade:** Utilizar caminhos rápidos para tipos comuns:
- `Integer`/`Long`: `BigDecimal.valueOf(n.longValue())`.
- `Double`/`Float`: `BigDecimal.valueOf(n.doubleValue())`.
- Adicionar cache para constantes comuns (0, 1, 10, 100) além das já providas pelo JDK.

## 4. Cálculos Redundantes de Logaritmo (`MathFunctions.java`)
**Problema:** A função `log(base, value)` é implementada como `log(value) / log(base)`. Se a base for um literal (ex: `log(10, x)`), o `log(10)` é recomputado em cada chamada de `compute()`.
**Oportunidade:** Otimizar o `ExpressionCompiler` para detectar argumentos constantes em funções conhecidas e transformar a chamada em uma operação com o valor pré-computado (ex: `log(x) * 0.43429...`).

## 5. Alocações de Literais de Vetor (`AbstractObjectEvaluator.java`)
**Problema:** A avaliação de um literal de vetor (ex: `[1, 2, a]`) sempre aloca um novo `ArrayList` via `evaluateVector`.
**Oportunidade:** 
- Para literais de vetor que contêm apenas constantes, garantir que o `ArrayList` seja instanciado e "congelado" uma única vez durante a compilação ou na primeira execução (lazy initialization no nó executável).
- Avaliar o uso de `List.of()` ou estruturas imutáveis de tamanho fixo para reduzir o overhead do `ArrayList`.

## 6. Gestão de Precisão em Funções Transcendentais
**Observação:** Análises prévias confirmam que o uso de `DECIMAL128` (34 dígitos) em funções como `pow`, `root` e `log` da biblioteca `BigDecimalMath` consome até 90% do tempo de execução em cenários matemáticos intensos.
**Oportunidade:** 
- Expor configurações de precisão mais granulares no `ExpressionEnvironment`, permitindo que o usuário escolha `DECIMAL64` (16 dígitos) para cálculos onde a performance é prioritária sobre a precisão extrema.
- O ganho de performance ao mudar de `DECIMAL128` para `DECIMAL64` é tipicamente de 2x a 4x em funções transcendentais.
