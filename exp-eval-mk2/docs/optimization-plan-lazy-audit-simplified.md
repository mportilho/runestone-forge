# Plano de Otimização: Auditoria Preguiçosa Simplificada (Lazy Audit)

## Contexto e Problema
O método `computeWithAudit()` é essencial para rastreabilidade, mas atualmente apresenta um overhead significativo de performance e alocação de memória. Para cada nó avaliado (leitura de variável, chamada de função, atribuição), um novo objeto `AuditEvent` (como `VariableRead` ou `FunctionCall`) é instanciado e adicionado a uma lista. Em expressões complexas ou execuções em lote, isso gera:
1.  **Pressão no GC:** Milhares de objetos temporários de vida curta.
2.  **Degradação de Latência:** O custo de alocação e gerenciamento da `ArrayList` no "hot path" de execução.

## Objetivo
Reduzir o overhead da auditoria para níveis mínimos, focando na visualização simplificada de **variáveis** e **funções** (valores utilizados e retornados), postergando a criação de objetos ricos para fora do ciclo de execução da fórmula.

## Estratégia de Implementação

### 1. Refatoração do `AuditCollector` (Captura Bruta)
O `AuditCollector` deixará de armazenar `List<AuditEvent>` e passará a usar buffers pré-alocados baseados no `maxAuditEvents` (valor já calculado durante a compilação no `ExpressionRuntimeSupport`).

- **Estrutura Interna:** Utilização de arrays paralelos para evitar boxing e alocação excessiva:
    - `byte[] eventTypes`: Identificadores curtos (ex: 1 para VAR, 2 para FUNC, 3 para ASSIGN).
    - `String[] names`: Nomes de variáveis ou funções.
    - `Object[] values`: Valores lidos ou resultados de funções.
    - `Object[] extra`: Buffers de argumentos (`Object[]`) para funções.
- **Interface de Registro:** Métodos especializados que apenas preenchem os arrays por índice:
    - `recordVariable(String name, Object value)`
    - `recordFunction(String name, Object[] args, Object result)`
    - `recordAssignment(String name, Object value)`

### 2. Desacoplamento no `AbstractObjectEvaluator`
O avaliador de expressões será alterado para remover todas as instanciações de `new AuditEvent.XXX(...)`.

- **Mudança:** O código passará a fazer chamadas diretas ao `AuditCollector` passando apenas os dados brutos já disponíveis na pilha de execução.
- **Ganho:** O "hot path" de avaliação torna-se livre de alocações de objetos relacionadas à auditoria.

### 3. Instanciação Tardia (Lazy) no `buildTrace()`
A conversão dos dados brutos dos arrays para os objetos `AuditEvent` exigidos pela API pública ocorrerá apenas no método `buildTrace()`, no momento em que o rastro é finalizado.

- **Comportamento:** Se o rastro não for consumido programaticamente de forma intensiva após a execução, os objetos `AuditEvent` nunca chegarão a ser criados no Heap durante a fase de cálculo matemático.

## Benefícios Esperados
- **Alocação de Objetos:** Redução drástica nas alocações durante o `computeWithAudit()`.
- **Performance:** Ganho expressivo na velocidade de execução do modo auditado em expressões de média/alta complexidade.
- **Foco:** Manutenção da visualização clara dos dados que realmente importam (Inputs/Outputs de variáveis e funções).

---
*Este plano endereça o item "2. Sobrecarga de Eventos de Auditoria" do documento de análise de performance do Internal Runtime.*
