# Relatório de Análise de Hotspots - Módulo `expression-evaluator`

Data da Análise: 03 de Abril de 2026
Metodologia: Benchmarks JMH com perfilamento JFR (Java Flight Recorder)

## 1. Visão Geral
A análise focou no motor de execução do `expression-evaluator` e identificou gargalos de CPU e alocação de memória durante a avaliação de expressões matemáticas e lógicas.

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

## 5. Resultados Consolidados

Os benchmarks JMH mostram ganhos relevantes quando o motor evita alocações desnecessárias e reduz cópias intermediárias.

### Ganhos de Latência e Alocação

| Cenário de Teste | Antes (ns/op) | Depois (ns/op) | Ganho (%) | Δ Alocação (B/op) |
| :--- | :--- | :--- | :--- | :--- |
| **Variáveis Simples** | 1101.7 | 860.8 | **+21.8%** | **-552 bytes** |
| **Fluxo Condicional** | 871.2 | 702.9 | **+19.3%** | **-448 bytes** |
| **Funções de Usuário** | 1176.6 | 957.6 | **+18.6%** | **-448 bytes** |
| **Cadeia de Variáveis** | 1159.4 | 930.0 | **+19.8%** | **-448 bytes** |

### Conclusão Técnica
A substituição de `HashMap` por **Arrays (Bindings Posicionais)** é a otimização mais impactante neste caminho de execução, porque elimina o custo de lookup por string e reduz a pressão de alocação em cenários com muitas variáveis.

Os números de throughput e alocação mostram que o motor se beneficia de caminhos mais lineares e de menos cópias intermediárias em chamadas repetidas.

---
*Relatório finalizado em 03/04/2026 com evidências de benchmark JMH.*
