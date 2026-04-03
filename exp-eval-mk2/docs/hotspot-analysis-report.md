# Relatório de Análise de Hotspots - Módulo `exp-eval-mk2`

Data da Análise: 03 de Abril de 2026
Metodologia: Benchmarks JMH com perfilamento JFR (Java Flight Recorder)

## 1. Visão Geral
A análise focou no motor de execução do `exp-eval-mk2`, comparando-o com a versão legada e identificando gargalos de CPU e alocação de memória durante a avaliação de expressões matemáticas e lógicas.

## 2. Hotspots de Alocação (Memory Churn)

| Componente / Método | Descrição do Impacto | Recomendação de Melhoria |
| :--- | :--- | :--- |
| `ExpressionRuntimeSupport.buildOverrides(Map)` | Cria um novo `HashMap` e realiza coerção de tipos em **cada** chamada de `compute()`. | Implementar um sistema de *bindings* posicional (baseado em array) para evitar o custo de mapas e lookups por string. |
| `MathExpression.compute(Map)` | O uso de `Map.of()` ou `HashMap` na API pública para passar argumentos gera churn constante de objetos. | Permitir o reuso de um objeto `Bindings` ou similar que possa ser populado sem novas alocações. |
| `ExecutionScope.createExecutionScope` | Instanciação frequente de escopos de execução, mesmo para expressões simples sem atribuições. | Implementar um *pool* de objetos de escopo ou especializar escopos *read-only* extremamente leves. |

## 3. Hotspots de CPU (Execution Speed)

| Componente / Método | Descrição do Impacto | Recomendação de Melhoria |
| :--- | :--- | :--- |
| `BigDecimal.pow(int, MathContext)` | Operação extremamente custosa na JVM, especialmente para precisões altas. | Avaliar o uso de `double` onde a precisão de `BigDecimal` não for necessária ou otimizar casos de potências inteiras pequenas. |
| `RuntimeCoercionService.asNumber(Object)` | Uso de `new BigDecimal(n.toString())` para tipos `Number` genéricos (como `Long` ou `Double`). | Adicionar caminhos rápidos (*fast-paths*) para tipos numéricos comuns, evitando a conversão intermediária para String. |
| `AbstractObjectEvaluator.evaluateBinary` | O `switch` de operadores e as chamadas repetitivas a `asBigDecimal()` em cada nó da árvore. | O compilador poderia realizar *type specialization* se os tipos dos operandos forem conhecidos em tempo de compilação. |

## 4. Auditoria e Overhead

A coleta de traços de auditoria (`computeWithAudit`) adiciona entre **20% e 40%** de latência por avaliação. 
- **Causa:** Criação de múltiplos objetos `AuditEvent` e o custo de instrumentação no `AbstractObjectEvaluator`.
- **Impacto:** Inviabiliza o uso de auditoria em ambientes de altíssima vazão sem uma degradação perceptível.

## 5. Conclusões e Próximos Passos

O `exp-eval-mk2` apresenta uma evolução arquitetural sólida com o uso de `ExecutionPlan`, eliminando o custo de parsing no caminho crítico. No entanto, para atingir o próximo nível de performance, o projeto deve focar em:

1. **Zero-Allocation Bindings:** Reduzir a dependência de `java.util.Map` durante a avaliação.
2. **Type Specialization:** Evitar coerções dinâmicas para `BigDecimal` quando os tipos já estão estabilizados.
3. **Auditoria Lazy:** Avaliar se parte dos dados de auditoria pode ser gerada apenas sob demanda ou de forma mais compacta.

---
*Relatório gerado automaticamente via Gemini CLI Performance Tools.*
